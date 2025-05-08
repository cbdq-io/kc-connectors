#!/usr/bin/env python
"""
A Python script it initialise a Kafka Connect connector via the REST URL.

Configuration is passed to the script via the environment.  The endpoint is
taken from the environment variable KAFKA_CONNECT_ENDPOINT.  If that is not
available, it will default to "http://localhost:8083".

Environment variables will be searched that that have a name that follows
the naming convention of CONNECTOR_${NAME}_SOME_CONFIG_VALUE.  For example
an environment variabled called CONNECTOR_AzureServiceBusSink_CONNECTOR_CLASS
will configure a connector.class property for a connector called
AzureServiceBusSink.  When the connector config is gathered, it will be
posted to the connectors endpoint of Kafka Connect to initialise the
connector.
"""
import argparse
import json
import logging
import os
import signal
import sys
import time
import types

import requests
from prometheus_client import start_http_server, Counter

logging.basicConfig()
logger = logging.getLogger('kccinit')
logger.setLevel(os.environ.get('LOG_LEVEL', 'WARN'))
parser = argparse.ArgumentParser()
group = parser.add_mutually_exclusive_group()
group.add_argument(
    '-d', '--debug',
    help='Set logging to DEBUG.',
    action='store_true'
)
group.add_argument(
    '-v', '--verbose',
    help='Set logging to verbose (INFO).',
    action='store_true'
)
parser.add_argument(
    '-s', '--sidecar',
    help='Run in sidecar mode.',
    action='store_true'
)


class SidecarMode:
    def __init__(self, endpoint: str) -> None:
        logger.debug(f'Starting sidecar mode (endpoint={endpoint}).')
        signal.signal(signal.SIGINT, self.signal_handler)
        signal.signal(signal.SIGTERM, self.signal_handler)
        self.endpoint = endpoint
        self.error_occurences = 0
        start_http_server(8000)
        self.prom = Counter(
            'kafka_connect_task_restart_count',
            'The number of task restarts made.'
        )

        while(True):
            status_details = self.get_status_details(endpoint)
            failed_tasks_count = self.restart_any_failed_tasks(status_details)
            self.error_occurences = self.report_status(failed_tasks_count)
            time.sleep(60)

    def get_status_details(self, endpoint: str) -> dict:
        """
        Get the status of all connectors and tasks from the Kafka Connect endpoint.

        Parameters
        ----------
        endpoint : str
            The endpoint for the Kafka Connect API.

        Returns
        -------
        dict
            The details returned from the endpoint having been decoded from JSON.
        """
        url = f'{endpoint}/connectors?expand=status'
        response = requests.get(url).json()
        return response

    def report_status(self, failed_task_count: int) -> int:
        """Report the status of the connectors."""
        if failed_task_count:
            self.prom.inc(failed_task_count)
        elif not self.error_occurences and not failed_task_count:
            logger.debug(f'All tasks in all connectors are running.')
            return 0
        elif self.error_occurences:
            logger.info(f'Tasks have recovered.')
            return 0

        message = f'Restarted {failed_task_count} tasks.'

        if self.error_occurences >= 3:
            logger.error(message)
        else:
            logger.warning(message)

        return self.error_occurences + 1

    def restart_any_failed_tasks(self, status_details: dict) -> int:
        """
        Restart any failed tasks for any connector.

        Parameters
        ----------
        status_details : dict
            The data as returned from the status endpoint.

        Returns
        -------
        int
            The number of tasks that needed to be restarted.
        """
        failed_tasks_count = 0

        for connector_name, connector_details in status_details.items():
            connector_status = connector_details['status']
            connector_state = connector_status['connector']['state']
            logger.debug(f'{connector_name} ({connector_state})')

            if connector_state != 'RUNNING':
                logger.error(f'The state of connector {connector_name} is "{connector_state}".')

            tasks = connector_status['tasks']

            for task in tasks:
                task_id = task['id']
                task_state = task['state']

                if task_state == 'RUNNING':
                    continue

                failed_tasks_count += 1
                url = f'{self.endpoint}/connectors/{connector_name}/tasks/'
                url += f'{task_id}/restart'
                response = requests.post(url)
                message = f'Response code from restart request of task {connector_name}/'
                message += f'{task_id} ("{task_state}") was {response.status_code}.'
                logger.warning(message)

        return failed_tasks_count


    def signal_handler(self, sig: int, frame: types.FrameType) -> None:
        """
        Catch signals.

        Parameters
        ----------
        sig : int
            The signal received that needs to be handled.
        frame : types.FrameType
            This is the current stack frame when the signal was received.
        """
        logger.warning(f'Received signal ({sig}), shutting down.')
        sys.exit(self.error_occurences)

class ConnectorInitialiser:
    """
    A class for initialising Kafka Connect connectors.

    Parameters
    ----------
    environ : dict, default os.environ
        The variables to be parsed to get the config.
    """

    def __init__(self, environ: dict = os.environ):
        self.connectors = {}
        self.environ = environ
        self.parse_environment_config()
        self.wait_for_endpoint_to_be_ready()
        self.initialise_connectors()
        self.status = 0

    def endpoint(self, endpoint: str = None) -> str:
        """
        Get or set the endpoint for Kafka Connect.

        Parameters
        ----------
        endpoint : str, optional
            The endpoint value that should be set.

        Returns
        -------
        str
            The endpoint value that is set.
        """
        if endpoint is not None:
            self._endpoint = endpoint
            logger.debug(f'Kafka Connect endpoint set to "{endpoint}".')
        return self._endpoint

    def initialise_connector(self, name: str, data: dict) -> None:
        """
        Initialise a single connector.

        Parameters
        ----------
        name : str
            The name of the connector.
        data : dict
            The data payload to be sent to the Kafka Connect endpoint.
        """
        url = f'{self.endpoint()}/connectors/{name}/config'
        req = requests.put(
            url=url,
            data=json.dumps(data),
            headers={
                'Content-Type': 'application/json'
            },
            timeout=30
        )

        if req.status_code >= 200 and req.status_code < 300:
            logger.info(f'Got response {req.status_code} when initialising {data["name"]}.')
        else:
            logger.error(f'Got response {req.status_code} when initialising {data["name"]}.')
            self.status = 1

    def initialise_connectors(self) -> None:
        """Initialise any connector that has been configured."""
        for connector_name, config_items in self.connectors.items():
            data = {
                'name': connector_name,
            }

            for item in config_items:
                key, value = item
                data[key] = value

            self.initialise_connector(connector_name, data)

    def parse_environment_config(self) -> None:
        """Parse the configuration from the environment."""
        endpoint = self.environ.get(
            'KAFKA_CONNECT_ENDPOINT',
            'http://localhost:8083'
        )
        self.endpoint(endpoint)

        for key in self.environ.keys():
            if key.startswith('CONNECTOR_') and len(key.split('_')) > 2:
                self.parse_environment_variable(key)

    def parse_environment_variable(self, key: str) -> None:
        """
        Parse an individual environment variable.

        Parameters
        ----------
        key : str
            The name of the environment variable.
        """
        connector_name = key.split('_')[1]
        key_components = key.lower().split('_')
        config_name = '.'.join(key_components[2:])
        value = str(self.environ[key])

        if connector_name not in self.connectors:
            self.connectors[connector_name] = []

        logger.debug(f'{connector_name} "{config_name}"')
        self.connectors[connector_name].append((config_name, value))

    def wait_for_endpoint_to_be_ready(self) -> None:
        """Wait for the endpoint to return a 2XX code."""
        is_ready = False

        while not is_ready:
            time.sleep(5)

            try:
                r = requests.get(self.endpoint(), timeout=10)
                code = r.status_code

                if code and code >= 200 and code <= 299:
                    is_ready = True
            except (requests.exceptions.ConnectTimeout, Exception):
                logger.warning(f'Waiting for "{self.endpoint()}" to return a 2XX code.')

        logger.info(f'The Kafka Connect endpoint ("{self.endpoint()}") is ready.')


if __name__ == '__main__':
    args = parser.parse_args()

    if args.debug:
        logger.setLevel(logging.DEBUG)
    elif args.verbose:
        logger.setLevel(logging.INFO)

    initialiser = ConnectorInitialiser()
    status = initialiser.status

    if args.sidecar and status == 0:
        SidecarMode(initialiser.endpoint())

    sys.exit(status)

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
import json
import logging
import os
import time

import requests

logging.basicConfig()
logger = logging.getLogger('kccinit')
logger.setLevel(os.environ.get('LOG_LEVEL', 'WARN'))


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

    def initialise_connector(self, data) -> None:
        """
        Initialise a single connector.

        Parameters
        ----------
        data : dict
            The data payload to be sent to the Kafka Connect endpoint.
        """
        url = f'{self.endpoint()}/connectors'
        req = requests.post(
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

    def initialise_connectors(self) -> None:
        """Initialise any connector that has been configured."""
        for connector_name, config_items in self.connectors.items():
            data = {
                'name': connector_name,
                'config': {}
            }

            for item in config_items:
                key, value = item
                data['config'][key] = value

            self.initialise_connector(data)

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
            except requests.exceptions.ConnectTimeout:
                logger.warning(f'Waiting for "{self.endpoint()}" to return a 2XX code.')

        logger.info(f'The Kafka Connect endpoint ("{self.endpoint()}") is ready.')


if __name__ == '__main__':
    ConnectorInitialiser()

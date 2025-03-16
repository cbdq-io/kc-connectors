#!/usr/bin/env python
"""
Generate test data for injecting into a Kafka topic.
"""
import argparse
import datetime
import json
import logging
import os
import sys
from random import choice
from string import ascii_uppercase

MIN_MESSAGE_SIZE = 96
PROG = os.path.basename(sys.argv[0]).removesuffix('.py')
logging.basicConfig()
logger = logging.getLogger(PROG)


class DataGenerator:
    """
    A class to generate random data as JSON records.

    Parameters
    ----------
    count : int
        The number of records to be generated.
    size : int
        The size of reach record.
    large_message_size : int
        If a value other than zero is provided, then this will be the
        size of a message that will be generated randomly as one of the
        "count" messages.  This is to excercise checking that larger
        messages can be handled.
    """
    def __init__(self, count: int, size: int, large_message_size=0) -> None:
        """Create a DataGenerator object."""
        self.count = count
        self.size = size
        self.large_message_size = large_message_size

    def print(self) -> None:
        """Print JSON records."""
        instance = 0

        if self.large_message_size:
            random_large_message = choice(range(1, self.count))  # nosec B311
        else:
            random_large_message = 0

        while instance < self.count:
            instance += 1
            record = {
                'instance': instance,
                'timestamp': datetime.datetime.utcnow().isoformat(),
                'payload': ''
            }

            json_record = json.dumps(record)

            if instance == random_large_message:
                payload_size = self.large_message_size
            else:
                payload_size = self.size - len(json_record)

            payload = ''.join(
                choice(ascii_uppercase) for i in range(payload_size)  # nosec B311
            )
            record['payload'] = payload
            print(f'{instance}:{json.dumps(record)}')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(
        prog=PROG,
        description='Generate test data for injecting into a Kafka topic.'
    )
    parser.add_argument('-c', '--count',
                        help='The number of records to create.',
                        required=True,
                        type=int)
    parser.add_argument('-s', '--size',
                        help='The size (in bytes) for each record.',
                        required=True,
                        type=int)
    parser.add_argument('-r', '--random-large-message-size',
                        help='The size of a randomly large message.',
                        default=0,
                        type=int)

    args = parser.parse_args()

    if args.count <= 0:
        message = f'Count ({args.count}) must be a positive integer.'
        logger.error(message)
        sys.exit(2)

    if args.size < MIN_MESSAGE_SIZE:
        message = f'Size ({args.size}) must be at least {MIN_MESSAGE_SIZE}.'
        logger.error(message)
        sys.exit(2)

    if args.random_large_message_size and args.random_large_message_size <= args.size:
        logger.error('Large message size must be larger than the default message size.')
        sys.exit(2)

    dg = DataGenerator(args.count, args.size, args.random_large_message_size)
    dg.print()

#!/usr/bin/env python3
# Start droplets, usage ./startDroplets.py <numDroplets> 'store-config'

import os
import sys
import json
import requests
import time
from os import path


def get_user_config(store_args):
    root_dir = path.dirname(path.dirname(path.abspath(__file__)))
    config_file = path.join(root_dir, 'cloud-config.yml')
    assert path.isfile(config_file)
    with open(config_file, 'r') as f:
        cloud_config = ''.join([line for line in f])
        return cloud_config.replace('{{store-args}}', store_args)


def get_header(token):
    return {'Content-Type': 'application/json',
            'Authorization': 'Bearer {}'.format(token)}


def get_body(names, store_args):
    return {'names': names, 'region': 'nyc1', 'size': 's-1vcpu-1gb',
            'ssh_keys': [21000986], 'image': 'ubuntu-16-04-x64',
            'backups': False, 'ipv6': False, 'private_networking': True,
            'tags': ['store-node'], 'user_data': get_user_config(store_args)}


def create_droplets(names, store_args):
    body = get_body(names, store_args)
    headers = get_header(token)
    response = requests.post('https://api.digitalocean.com/v2/droplets',
                             data=json.dumps(body), headers=headers)
    print(response.json())


def main(token, num_nodes, store_args, batch_size=10):
    print('Creating {} nodes'.format(num_nodes))
    names = []
    for i in range(num_nodes):
        names.append('store-node-{}'.format(i + 1))
        if len(names) == batch_size:
            print('\n=== NEXT BATCH ===\n')
            create_droplets(names, store_args)
            names = []
    if len(names) > 0:
        create_droplets(names, store_args)


if __name__ == '__main__':
    assert len(sys.argv) == 3
    token = os.environ.get('TOKEN')
    num_nodes = int(sys.argv[1])
    store_args = sys.argv[2]
    assert num_nodes > 0
    assert token
    assert store_args
    main(token, num_nodes, store_args)

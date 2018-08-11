# Usage

1. Export your [API token](https://cloud.digitalocean.com/settings/applications): `export TOKEN=your_token`
2. Start a seed node: `./addDroplets.py 1 '-s $IP:2552'`
3. Start the desired number of droplets: `./addDroplets.py <num nodes> '-s <seed ip>:<seed port>'`
4. Delete all droplets: `./deleteDroplets.sh`

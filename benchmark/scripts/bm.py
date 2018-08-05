import subprocess
import time

# mut    = 172.16.64.14
# thor01 = 172.16.64.55
# thor02 = 172.16.64.56
# thor03 = 172.16.64.57
# thor04 = 172.16.64.58

# STORE_IPS = ["172.16.64.55"]
STORE_IPS = ["127.0.0.1"]
SSH_USERS = ["seminar1807"]

NUM_STORES = len(STORE_IPS)

assert(len(SSH_USERS) == NUM_STORES)
assert(NUM_STORES > 0)

SEED_IP = STORE_IPS[0]

STORE_CMD = ("java", "-jar", "store-0.1.0-SNAPSHOT-all.jar")
STORE_CMD_ARGS = ("-b", "-w", "1", "-r", "1", "-s", SEED_IP)

BM_LOAD_CMD = ("sh", "load.sh")
BM_READ_CMD = ("sh", "run.sh")
BM_PARAMS = ("-P", "10M.dat", "-p", f"storeIp={SEED_IP}")

CONFIGS = [
    ("-c", "1"),
    ("-c", "10"),
    ("-c", "100"),
    ("-c", "1000"),
    ("-c", "10000"),
    ("-c", "50000"),
    ("-c", "100000"),
    ("-c", "250000"),
    ("-c", "500000"),
    ("-c", "1000000"),
    ("-c", "10000000")
]


def bm_run(cmd, out_file_name):
    out_file = open(out_file_name, "w")
    subprocess.call(cmd, shell=False, stdout=out_file, stderr=out_file)
    out_file.close()


def main():
    for config in CONFIGS:
        capacity = config[1]
        store_log_file = f"store_capacity_{capacity}-num_stores_{NUM_STORES}.txt"
        remote_ssh_cmd = (*STORE_CMD, *STORE_CMD_ARGS, *config, ">", store_log_file)

        store_ssh_sessions = []
        for i, (user, ip) in enumerate(zip(SSH_USERS, STORE_IPS)):
            # ssh = ("ssh", f"{user}@{ip}", "-t")
            # ssh_cmd = (*ssh, '"', *remote_ssh_cmd, '"')
            ssh_cmd = remote_ssh_cmd
            print(f"STARTING DATASTORE ON {ip} WITH CMD: {' '.join(STORE_CMD_ARGS) + ' ' + ' '.join(config)}")
            store_ssh = subprocess.Popen(ssh_cmd, shell=False)
            store_ssh_sessions.append(store_ssh)

            # Wait for seed server to start before other servers can connect
            if i == 0:
                time.sleep(1)

        # Wait for all non-seed servers to start
        if len(STORE_IPS) > 1:
            time.sleep(10)

        bm_file_template = f"capacity_{capacity}-num_stores_{NUM_STORES}.txt"

        print(f"Running LOAD: capacity={capacity}, #stores={NUM_STORES}")
        load_file = f"load_{bm_file_template}"
        load_cmd = (*BM_LOAD_CMD, *BM_PARAMS)
        bm_run(load_cmd, load_file)

        print(f"Running READ: capacity={capacity}, #stores={NUM_STORES}")
        read_file = f"read_{bm_file_template}"
        read_cmd = (*BM_READ_CMD, *BM_PARAMS)
        bm_run(read_cmd, read_file)

        for store_ssh in store_ssh_sessions:
            store_ssh.kill()


if __name__ == "__main__":
    main()

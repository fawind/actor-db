# YCSB Benchmark

## Getting Started

1. Download the latest [YCSB release](https://github.com/brianfrankcooper/YCSB) (tested with YCSB `0.14.0`)
2. Build the ActorDB benchmark interface using `gradle :benchmark:shadowJar`


## Running a Benchmark

1. Start the ActorDB

2. Create table (`usertable` for core workflows)

3. Load the data:

```bash
java \
  -cp ./lib/*:<actorDB-repo-path>/benchmark/build/libs/benchmark-0.1.0-SNAPSHOT-all.jar \
  com.yahoo.ycsb.Client -s -load \
  -db ActorDbClient \
  -P ./workloads/workloadc \
  -p clientPort=2554 \
  -p storePort=2552
```

4. Run the workflow:

```bash
java \
  -cp ./lib/*:<actorDB-repo-path>/benchmark/build/libs/benchmark-0.1.0-SNAPSHOT-all.jar \
  com.yahoo.ycsb.Client -s -t \
  -db ActorDbClient \
  -P ./workloads/workloadc \
  -p clientPort=2554 \
  -p storePort=2552
```

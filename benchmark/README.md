# YCSB Benchmark

## Getting Started

1. Download the latest [YCSB release](https://github.com/brianfrankcooper/YCSB) (tested with YCSB `0.14.0`).
2. Build the ActorDB benchmark interface using `gradle :benchmark:shadowJar`.
3. Start the ActorDB.

## Running a Benchmark

```bash
java \
  -cp ycmb/lib/*:<path-to-actordb>/actor-db/benchmark/build/libs/benchmark-0.1.0-SNAPSHOT-all.jar \
  com.yahoo.ycsb.Client -t \
  -db ActorDbClient \
  -P ../workloads/workloadc \
  -p tableName=usertable \
  -p clientPort=2554 \
  -p storePort=2552
```

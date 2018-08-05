gradle -p /Users/law/repos/actor-db :benchmark:shadowJar

java \
  -cp ./lib/*:/Users/law/repos/actor-db/benchmark/build/libs/benchmark-0.1.0-SNAPSHOT-all.jar \
  com.yahoo.ycsb.Client -s -t \
  -threads 8 \
  -db ActorDbClient \
  -P ./workloads/workloadc \
  -p clientPort=0 \
  -p storePort=2552 \
  $@
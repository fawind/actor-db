CUR_DIR=`pwd`

cd ../..
./gradlew :benchmark:shadowJar

cd $CUR_DIR

java \
  -cp ./lib/*:../build/libs/benchmark-0.1.0-SNAPSHOT-all.jar \
  com.yahoo.ycsb.Client -s -t \
  -threads 16 \
  -db ActorDbClient \
  -P ./workloads/workloadc \
  -p clientPort=0 \
  -p storePort=2552 \
  $@
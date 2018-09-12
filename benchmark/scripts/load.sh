#!/usr/bin/env bash
CUR_DIR=`pwd`

cd ../..
./gradlew :benchmark:shadowJar

cd $CUR_DIR

java \
  -cp ./lib/*:../build/libs/benchmark-0.1.0-SNAPSHOT-all.jar \
  com.yahoo.ycsb.Client -s -load \
  -threads 200 \
  -db ActorDbClient \
  -P ./workloads/workloadc \
  -P ./100k.dat \
  -p clientPort=0 \
  -p storeIp="159.65.236.142" \
  -p storePort=2552 \
  $@

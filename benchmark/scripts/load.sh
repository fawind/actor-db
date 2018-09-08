#!/usr/bin/env bash
CUR_DIR=`pwd`

cd ../..
./gradlew :benchmark:shadowJar

cd $CUR_DIR

java \
  -cp ./lib/*:../build/libs/benchmark-0.1.0-SNAPSHOT-all.jar \
  com.yahoo.ycsb.Client -s -load \
  -threads 16 \
  -db ActorDbClient \
  -P ./workloads/workloadc \
  -P ./300k.dat \
  -p clientPort=0 \
  -p storeIp="206.189.238.25" \
  -p storePort=2552 \
  $@

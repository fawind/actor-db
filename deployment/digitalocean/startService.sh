#!/bin/sh

JAR_ARGS=$1
IP="$(curl http://169.254.169.254/metadata/v1/interfaces/public/0/ipv4/address)"

echo "-h $IP $JAR_ARGS" > storeArgs.log

/home/store/actor-db/deployment/ActorDbService.sh start \
  /home/store/actor-db/store/build/libs/store-0.1.0-SNAPSHOT-all.jar \
  "-h $IP $JAR_ARGS"

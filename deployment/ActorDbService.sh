#!/bin/sh
#
# Usage: ./ActorDbService.sh start /path/to/java '--argA --argB'
#

SERVICE_NAME=ActorDb
PID_PATH_NAME=/tmp/ActorDbPID
LOG_FILE="$SERVICE_NAME.log"

PATH_TO_JAR=$2
JAR_ARGS=$3

case $1 in
  start)
      echo "Starting $SERVICE_NAME ..."
      if [ ! -f $PID_PATH_NAME ]; then
          nohup java -Xmx7g -Xms2g -jar $PATH_TO_JAR $JAR_ARGS >> $LOG_FILE 2>&1&
          echo $! > $PID_PATH_NAME
          echo "$SERVICE_NAME started ..."
      else
          echo "$SERVICE_NAME is already running ..."
      fi
  ;;
  stop)
      if [ -f $PID_PATH_NAME ]; then
          PID=$(cat $PID_PATH_NAME);
          echo "$SERVICE_NAME stoping ..."
          kill $PID;
          echo "$SERVICE_NAME stopped ..."
          rm $PID_PATH_NAME
      else
          echo "$SERVICE_NAME is not running ..."
      fi
  ;;
  restart)
      if [ -f $PID_PATH_NAME ]; then
          PID=$(cat $PID_PATH_NAME);
          echo "$SERVICE_NAME stopping ...";
          kill $PID;
          echo "$SERVICE_NAME stopped ...";
          rm $PID_PATH_NAME
          echo "$SERVICE_NAME starting ..."
          nohup java -jar $PATH_TO_JAR >> $LOG_FILE 2>&1&
          echo $! > $PID_PATH_NAME
          echo "$SERVICE_NAME started ..."
      else
          echo "$SERVICE_NAME is not running ..."
      fi
  ;;
esac

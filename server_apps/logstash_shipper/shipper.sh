#!/bin/bash

NAME="Logstash Shipper"

SHIPPER_HOME="$TARGETROOT/server_apps/logstash_shipper"

cd $SHIPPER_HOME

PID_FILE=$SHIPPER_HOME/shipper.pid

start_shipper() {
echo "Starting $NAME"
    nohup java -jar $SHIPPER_HOME/logstash-1.1.13-flatjar.jar agent -f $SHIPPER_HOME/shipper.conf > $SHIPPER_HOME/shipper.log 2>&1 & echo $! > $SHIPPER_HOME/shipper.pid
    echo pid: $(cat $PID_FILE)
}

stop_shipper() {
	kill $(cat $PID_FILE)
	if [ -e $PID_FILE ]
		then rm $PID_FILE
	fi
}

kill_shipper() {
	kill -9 $(cat $PID_FILE)
	if [ -e $PID_FILE ]
		then rm $PID_FILE
	fi
}

case $1 in
	start)
	start_shipper
	;;
	stop)
	echo "Stopping $NAME"
	stop_shipper
	;;
	kill)
	echo "Killing $NAME"
	kill_shipper
	;;
	restart)
	echo "Restarting $NAME"
	echo "Stopping shipper..."
	stop_shipper
	sleep 1
	start_shipper
	;;
	*)
	echo "usage: $NAME {start|stop|restart|kill}"
	exit 1
	;;
esac

exit 0

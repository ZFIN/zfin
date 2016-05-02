#!/bin/bash

NAME="Logstash Forwarder"

FORWARDER_HOME="$TARGETROOT/server_apps/logstash-forwarder"

cd $FORWARDER_HOME

PID_FILE=$FORWARDER_HOME/forwarder.pid

start_forwarder() {
echo "Starting $NAME"
    cd /tmp
    nohup /opt/zfin/logstash/logstash-forwarder -config $FORWARDER_HOME/logstash.json.conf > $FORWARDER_HOME/forwarder.log 2>&1 & echo $! > $FORWARDER_HOME/forwarder.pid
    echo pid: $(cat $PID_FILE)
}

stop_forwarder() {
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
	start_forwarder
	;;
	stop)
	echo "Stopping $NAME"
	stop_forwarder
	;;
	kill)
	echo "Killing $NAME"
	kill_shipper
	;;
	restart)
	echo "Restarting $NAME"
	echo "Stopping shipper..."
	stop_forwarder
	sleep 1
	start_forwarder
	;;
	*)
	echo "usage: $NAME {start|stop|restart|kill}"
	exit 1
	;;
esac

exit 0

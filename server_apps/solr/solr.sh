#!/bin/bash

#Not used right now, can be turned back on by appending to the solr command: -a "$SOLR_GC_LOGGING_OPTS"
SOLR_GC_LOGGING_OPTS="-verbose:gc -verbose:sizes -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintTenuringDistribution -Xloggc:$SOLR_HOME/solr-gc.log"
export SOLR_LOGS_DIR="$SOLR_HOME/server"

# solr seems to need to be a subdir of the working directory
cd $SOLR_HOME/..

echo SOURCEROOT  $SOURCEROOT
echo SOLR_HOME   $SOLR_HOME
echo SOLR_PORT   $SOLR_PORT
echo SOLR_MEMORY $SOLR_MEMORY

start_solr() {
echo 'Starting Solr...'
	$SOLR_HOME/bin/solr start -s $SOLR_HOME -p $SOLR_PORT -m $SOLR_MEMORY -noprompt
}

stop_solr() {
	$SOLR_HOME/bin/solr stop -s $SOLR_HOME -p $SOLR_PORT -m $SOLR_MEMORY -noprompt
}

kill_solr() {
	$SOLR_HOME/bin/solr stop -s $SOLR_HOME -p $SOLR_PORT -m $SOLR_MEMORY -noprompt
}

case $1 in
	start)
	start_solr
	;;
	stop)
	echo "Stopping $DESC: $NAME"
	stop_solr
	;;
	kill)
	echo "Killing $DESC: $NAME"
	kill_solr
	;;
	restart)
	echo "Restarting $DESC: $NAME"
	echo "Stopping Solr..."
	stop_solr
	sleep 1
	start_solr
	;;
	*)
	echo "usage: $NAME {start|stop|restart|kill}"
	exit 1
	;;
esac

exit 0

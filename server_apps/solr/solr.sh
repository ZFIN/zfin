#!/bin/bash

#SOLR_GC_LOGGING_OPTS="-verbose:gc -verbose:sizes -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintTenuringDistribution -Xloggc:$SOLR_HOME/solr-gc.log"
#JETTY_OPTS="-Djetty.port=$SOLR_PORT -Djetty.home=$SOLR_HOME/jetty -Djetty.base=$SOLR_HOME/jetty"
#JVM_OPTS="-Xms4g -Xmx5g "
#SOLR_OPTS="-Dsolr.home=$SOLR_HOME -Denable.slave=false -Denable.master=true"

# solr seems to need to be a subdir of the working directory
cd $SOLR_HOME/..

PID_FILE=$SOLR_HOME/solr.pid

echo SOURCEROOT  $SOURCEROOT
echo SOLR_HOME   $SOLR_HOME
echo SOLR_PORT   $SOLR_PORT
echo PID_FILE    $PID_FILE

start_solr() {
echo 'Starting Solr...'
#    nohup java $SOLR_GC_LOGGING_OPTS $JVM_OPTS -jar $SOLR_HOME/jetty/start.jar $JETTY_OPTS $SOLR_OPTS > $SOLR_HOME/solr.log 2>&1 & echo $! > $SOLR_HOME/solr#.pid
#    echo pid: $(cat $PID_FILE)
}

stop_solr() {
#	kill $(cat $PID_FILE)
#	if [ -e $PID_FILE ]
#		then rm $PID_FILE
#	fi
}

kill_solr() {
#	kill -9 $(cat $PID_FILE)
#	if [ -e $PID_FILE ]
#		then rm $PID_FILE
#	fi
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

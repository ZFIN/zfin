#!/bin/bash

Name=Jenkins
DESC="Jenkins CI Server"

PID_FILE=$JENKINS_HOME/jenkins.pid
COMMAND="java -jar $SOURCEROOT/server_apps/jenkins/jenkins.war --httpPort=$JENKINS_PORT --prefix=/jobs"

echo SOURCEROOT    $SOURCEROOT
echo JENKINS_HOME  $JENKINS_HOME
echo JENKINS_PORT  $JENKINS_PORT
echo PID_FILE      $PID_FILE

start_jenkins() {
echo 'Starting Jenkins...'
    nohup java -Dinstance=$INSTANCE -Djavax.net.ssl.trustStore=$JENKINS_HOME/bouncer.zfin.org-cert -jar $SOURCEROOT/server_apps/jenkins/jenkins.war --httpPort=$JENKINS_PORT --sessionTimeout=10080 --prefix=/jobs  > $JENKINS_HOME/logs/jenkins.log 2>&1 & echo $! > $JENKINS_HOME/jenkins.pid
    echo pid: $(cat $PID_FILE)
#	start-stop-daemon --start --quiet --background --make-pidfile --pidfile $PIDFILE --chuid $RUN_AS --exec $COMMAND
}

stop_jenkins() {
	kill $(cat $PID_FILE)
	if [ -e $PID_FILE ]
		then rm $PID_FILE
	fi
}

kill_jenkins() {
	kill -9 $(cat $PID_FILE)
	if [ -e $PID_FILE ]
		then rm $PID_FILE
	fi
}

case $1 in
	start)
	start_jenkins
	;;
	stop)
	echo "Stopping $DESC: $NAME"
	stop_jenkins
	;;
	kill)
	echo "Killing $DESC: $NAME"
	kill_jenkins
	;;
	restart)
	echo "Restarting $DESC: $NAME"
	echo "Stopping Jenkins..."
	stop_jenkins
	sleep 1
	start_jenkins
	;;
	*)
	echo "usage: $NAME {start|stop|restart|kill}"
	exit 1
	;;
esac

exit 0

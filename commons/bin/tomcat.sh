#!/bin/tcsh

# if an env file name is passed in as the second argument, source it
if ($# == 2 ) then
  echo "sourcing $2.env"
  source /private/ZfinLinks/Commons/env/$2.env
endif


switch ($1)

case "start":
$CATALINA_HOME/bin/startStop.sh start
chmod 644 $CATALINA_BASE/logs/*
chmod 644 $CATALINA_PID
;breaksw;

case "stop":
$CATALINA_HOME/bin/startStop.sh stop
;breaksw;

case "restart":
$CATALINA_HOME/bin/startStop.sh stop
$CATALINA_HOME/bin/startStop.sh start
chmod 644 $CATALINA_BASE/logs/*
chmod 644 $CATALINA_PID

;breaksw;

case "debug-start":
$CATALINA_HOME/bin/catalina.sh jpda start
chmod 644 $CATALINA_BASE/logs/*
chmod 644 $CATALINA_PID
;breaksw;

case "debug-stop":
$CATALINA_HOME/bin/catalina.sh stop
;breaksw;

case "debug-restart":
$CATALINA_HOME/bin/catalina.sh stop
$CATALINA_HOME/bin/catalina.sh start
chmod 644 $CATALINA_BASE/logs/*
chmod 644 $CATALINA_PID

default:
 echo "Usage tomcat.sh start / stop / restart / debug-start / debug-stop / debug-restart "
 echo "      [optional env to source]"
endsw


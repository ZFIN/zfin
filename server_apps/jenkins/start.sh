echo SOURCEROOT    $SOURCEROOT
echo JENKINS_HOME  $JENKINS_HOME
echo JENKINS_PORT  $JENKINS_PORT
nohup java java -Djavax.net.ssl.trustStore=bouncer.zfin.org-cert -jar $SOURCEROOT/server_apps/jenkins/jenkins.war --httpPort=$JENKINS_PORT --prefix=/jobs > $JENKINS_HOME/logs/jenkins.log 2>&1 & echo $! > $JENKINS_HOME/jenkins.pid
echo pid  $JENKINS_HOME/jenkins.pid

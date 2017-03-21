echo SOURCEROOT    $SOURCEROOT
echo JENKINS_HOME  $JENKINS_HOME
echo JENKINS_PORT  $JENKINS_PORT
# session timeout: one week = 10080 minutes
nohup java java -Djavax.net.ssl.trustStore=bouncer.zfin.org-cert -jar $SOURCEROOT/server_apps/jenkins/jenkins.war --sessionTimeout=10080 --httpPort=$JENKINS_PORT --prefix=/jobs > $JENKINS_HOME/logs/jenkins.log 2>&1 & echo $! > $JENKINS_HOME/jenkins.pid
echo pid  $JENKINS_HOME/jenkins.pid

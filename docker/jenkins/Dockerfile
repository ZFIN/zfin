FROM base:latest

USER root

#Add root certificate for auth servers to java keystore
COPY ./rhel_ipa.cer /tmp
RUN /opt/java/openjdk/bin/keytool -importcert -file /tmp/rhel_ipa.cer -cacerts -storepass changeit -noprompt -alias "RHEL IPA" 

USER gradle

# Disable nested search, see: https://github.com/jenkinsci/nested-view-plugin/compare/nested-view-1.23...nested-view-1.24
# http://plugins.jenkins.io/nested-view/releases/#version_1.24
RUN touch /home/gradle/.nestedViewsSearch

CMD /bin/bash -l -c "/opt/java/openjdk/bin/java \
	--add-opens java.base/java.lang=ALL-UNNAMED \
	--add-opens java.base/java.lang.reflect=ALL-UNNAMED \
	--add-opens java.desktop/java.awt=ALL-UNNAMED \
	-Dinstance=docker \
	-Dhudson.model.DirectoryBrowserSupport.CSP= \
	-jar /opt/zfin/source_roots/zfin.org/server_apps/jenkins/jenkins-2.504.3.war \
	 --httpPort=9499 \
	--sessionTimeout=604800 \
	--sessionEviction=604800 \
	--prefix=/jobs"

ARG ZFIN_RELEASE=main
FROM ghcr.io/zfin/zfin-base:${ZFIN_RELEASE}

USER root

#Add root certificate for auth servers to java keystore
COPY ./rhel_ipa.cer /tmp
RUN /opt/java/openjdk/bin/keytool -importcert -file /tmp/rhel_ipa.cer -cacerts -storepass changeit -noprompt -alias "RHEL IPA" && \
	mkdir /opt/jenkins && \
	chown gradle /opt/jenkins

USER gradle

# Disable nested search, see: https://github.com/jenkinsci/nested-view-plugin/compare/nested-view-1.23...nested-view-1.24
# http://plugins.jenkins.io/nested-view/releases/#version_1.24
RUN touch /home/gradle/.nestedViewsSearch

ADD --chown=gradle:gradle \
    --checksum=sha256:0000addfa8722963164137a4e0e60abc227d9d60710c4deb19481f9a751cf512 \
     https://updates.jenkins.io/download/war/2.541.3/jenkins.war /opt/jenkins/jenkins-2.541.3.war

CMD /bin/bash -l -c "/opt/java/openjdk/bin/java \
	--add-opens java.base/java.lang=ALL-UNNAMED \
	--add-opens java.base/java.lang.reflect=ALL-UNNAMED \
	--add-opens java.desktop/java.awt=ALL-UNNAMED \
	-Dinstance=docker \
	-Dhudson.model.DirectoryBrowserSupport.CSP= \
	-jar /opt/jenkins/jenkins-2.541.3.war \
	 --httpPort=9499 \
	--sessionTimeout=604800 \
	--sessionEviction=604800 \
	--prefix=/jobs"


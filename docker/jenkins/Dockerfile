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

# Jenkins WAR. The checksum is the official one published alongside the release
# at https://get.jenkins.io/war-stable/${JENKINS_VERSION}/jenkins.war.sha256 (it
# really does start with 0000). When bumping JENKINS_VERSION, update the checksum.
#
# Declared as build ARGs (build-scoped, not baked into the image like ENV would be)
# so the version and its checksum are named and easy to find/update together.
#
# Fetched via `curl + sha256sum` because BuildKit is currently disabled. We run as
# USER gradle and /opt/jenkins is gradle-owned, so the file lands gradle-owned; the
# gradle group was renamed to zfin in the base image, so this matches the old
# --chown=gradle:zfin. To re-enable BuildKit, this RUN can be replaced by:
#   ADD --chown=gradle:zfin --checksum=sha256:${JENKINS_WAR_SHA256} \
#       https://updates.jenkins.io/download/war/${JENKINS_VERSION}/jenkins.war /opt/jenkins/jenkins.war
ARG JENKINS_VERSION=2.541.3
ARG JENKINS_WAR_SHA256=0000addfa8722963164137a4e0e60abc227d9d60710c4deb19481f9a751cf512
RUN curl -fsSL "https://updates.jenkins.io/download/war/${JENKINS_VERSION}/jenkins.war" \
        -o /opt/jenkins/jenkins.war \
    && echo "${JENKINS_WAR_SHA256}  /opt/jenkins/jenkins.war" \
        | sha256sum -c -

ADD --chown=gradle:zfin ./jenkins-cli-shim-wrapper.sh /usr/local/bin/jenkins-cli

CMD /bin/bash -l -c "/opt/java/openjdk/bin/java \
	--add-opens java.base/java.lang=ALL-UNNAMED \
	--add-opens java.base/java.lang.reflect=ALL-UNNAMED \
	--add-opens java.desktop/java.awt=ALL-UNNAMED \
	-Dinstance=docker \
	-Dhudson.model.DirectoryBrowserSupport.CSP= \
	-jar /opt/jenkins/jenkins.war \
	 --httpPort=9499 \
	--sessionTimeout=604800 \
	--sessionEviction=604800 \
	--prefix=/jobs"


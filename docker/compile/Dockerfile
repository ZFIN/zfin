FROM gradle:jdk14

RUN apt update && apt install -y --no-install-recommends \
       ant groovy nodejs npm postgresql-client tcsh sudo \
       vim nano emacs iputils-ping

RUN mkdir /opt/misc 
COPY ./apache-groovy-binary-3.0.7.zip.sha256 /opt/misc
RUN cd /opt/misc && wget https://groovy.jfrog.io/artifactory/dist-release-local/groovy-zips/apache-groovy-binary-3.0.7.zip && \
        sha256sum -c apache-groovy-binary-3.0.7.zip.sha256 && unzip apache-groovy-binary-3.0.7.zip
RUN ln -s /opt/misc/groovy-3.0.7 /opt/misc/groovy
RUN chsh -s /usr/bin/tcsh gradle
RUN mkdir -p /opt/zfin/www_homes/zfin.org
RUN mkdir -p /opt/zfin/tls/certs
RUN mkdir -p /opt/zfin/tls/private
RUN mkdir -p /opt/zfin/catalina_bases/zfin.org
RUN chown -R gradle:gradle /opt/zfin/

#Compatibility links because /usr/bin and /bin are different in jdk14 based images
RUN ln -s /usr/bin/sudo /bin/sudo

RUN groupmod -n zfin gradle
RUN groupadd -g 8983 solr 
RUN adduser gradle root
RUN adduser gradle solr
RUN mkdir -p /var/solr/data/site_index && chgrp -R solr /var/solr && chmod -R g+ws /var/solr

COPY ./ssh-agent.sudoers /etc/sudoers.d/01-ssh-agent
RUN chmod 0440 /etc/sudoers.d/01-ssh-agent
USER gradle
COPY ./cshrc /home/gradle/.cshrc

CMD ["/usr/bin/tcsh"]

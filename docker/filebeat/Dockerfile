ARG ELK_VERSION

FROM docker.elastic.co/beats/filebeat:${ELK_VERSION}

USER root
COPY ./filebeat.yml /usr/share/filebeat/filebeat.yml
COPY ./apache.yml /usr/share/filebeat/modules.d/apache.yml
#COPY ./tomcat.yml /usr/share/filebeat/modules.d/tomcat.yml
COPY ./postgresql.yml /usr/share/filebeat/modules.d/postgresql.yml

RUN chmod go-w /usr/share/filebeat/filebeat.yml
RUN chmod go-w /usr/share/filebeat/modules.d/apache.yml
#RUN chmod go-w /usr/share/filebeat/modules.d/tomcat.yml
RUN chmod go-w /usr/share/filebeat/modules.d/postgresql.yml

RUN groupadd -g 999 postgres
RUN usermod  -aG postgres filebeat
RUN groupadd -g 8983 solr
RUN usermod  -aG solr filebeat
USER filebeat

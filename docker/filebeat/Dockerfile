ARG ELK_VERSION

FROM docker.elastic.co/beats/filebeat:${ELK_VERSION}

COPY ./filebeat.yml /usr/share/filebeat/filebeat.yml
COPY ./apache.yml /usr/share/filebeat/modules.d/apache.yml
#COPY ./tomcat.yml /usr/share/filebeat/modules.d/tomcat.yml

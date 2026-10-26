ARG ELK_VERSION

FROM docker.elastic.co/beats/filebeat:${ELK_VERSION}

USER root
COPY ./filebeat.yml /usr/share/filebeat/filebeat.yml
COPY ./apache.yml /usr/share/filebeat/modules.d/apache.yml
#COPY ./tomcat.yml /usr/share/filebeat/modules.d/tomcat.yml

RUN chmod go-w /usr/share/filebeat/filebeat.yml
RUN chmod go-w /usr/share/filebeat/modules.d/apache.yml
#RUN chmod go-w /usr/share/filebeat/modules.d/tomcat.yml

#eMudhra CA certificate compromise fix
RUN sed -i \
  -e 's|^mozilla/emSign_ECC_Root_CA_-_G3.crt|!&|' \
  -e 's|^mozilla/Comodo_AAA_Services_root.crt|!&|' \
  /etc/ca-certificates.conf \
  && update-ca-certificates

USER filebeat

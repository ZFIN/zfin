ARG ELK_VERSION

FROM docker.elastic.co/elasticsearch/elasticsearch:${ELK_VERSION}

#eMudhra CA certificate compromise fix
USER root
RUN sed -i \
  -e 's|^mozilla/emSign_ECC_Root_CA_-_G3.crt|!&|' \
  -e 's|^mozilla/Comodo_AAA_Services_root.crt|!&|' \
  /etc/ca-certificates.conf \
  && update-ca-certificates
USER elasticsearch

COPY ./elasticsearch.yml /usr/share/elasticsearch/config/elasticsearch.yml

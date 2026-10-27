ARG ELK_VERSION

FROM docker.elastic.co/kibana/kibana:${ELK_VERSION}

#eMudhra CA certificate compromise fix
USER root
RUN sed -i \
  -e 's|^mozilla/emSign_ECC_Root_CA_-_G3.crt|!&|' \
  -e 's|^mozilla/Comodo_AAA_Services_root.crt|!&|' \
  /etc/ca-certificates.conf \
  && update-ca-certificates
USER kibana

COPY ./kibana.yml /usr/share/kibana/config/kibana.yml

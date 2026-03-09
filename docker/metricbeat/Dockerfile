ARG ELK_VERSION

FROM docker.elastic.co/beats/metricbeat:${ELK_VERSION}

USER root
COPY ./metricbeat.yml /usr/share/metricbeat/metricbeat.yml
COPY ./modules.d/ /usr/share/metricbeat/modules.d/

RUN chmod go-w /usr/share/metricbeat/metricbeat.yml
RUN chmod go-w /usr/share/metricbeat/modules.d/*.yml

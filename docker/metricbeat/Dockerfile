ARG ELK_VERSION

FROM docker.elastic.co/beats/metricbeat:${ELK_VERSION}

COPY ./metricbeat.yml /usr/share/metricbeat/metricbeat.yml
COPY ./modules.d/ /usr/share/metricbeat/modules.d/

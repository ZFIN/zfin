ARG ELK_VERSION

FROM docker.elastic.co/beats/metricbeat:${ELK_VERSION}

COPY --chmod=0644 ./metricbeat.yml /usr/share/metricbeat/metricbeat.yml
COPY --chmod=0644 ./modules.d/ /usr/share/metricbeat/modules.d/

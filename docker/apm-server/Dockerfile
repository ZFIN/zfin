ARG ELK_VERSION

FROM docker.elastic.co/apm/apm-server:${ELK_VERSION}

COPY ./apm-server.yml /usr/share/apm-server/apm-server.yml
COPY ./traces-apm-template.json /usr/share/apm-server/traces-apm-template.json

# Install curl for the setup script (run as root, then switch back)
USER root
RUN apt-get update -qq && apt-get install -y -qq curl > /dev/null 2>&1 && rm -rf /var/lib/apt/lists/*
USER apm-server

COPY --chmod=755 ./docker-entrypoint-wrapper.sh /usr/local/bin/docker-entrypoint-wrapper.sh

ENTRYPOINT ["/usr/bin/tini", "--", "docker-entrypoint-wrapper.sh"]
CMD ["--strict.perms=false"]

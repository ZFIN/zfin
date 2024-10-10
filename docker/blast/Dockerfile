FROM fedora:latest

RUN groupadd -g 1000 zfin && \
    useradd -d /opt/zfin/blastdb -M -N -g zfin blast

USER blast

CMD ["/bin/bash","-l"]

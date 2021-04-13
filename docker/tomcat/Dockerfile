FROM tomcat:9-jdk15

#TODO: figure out how to use tomcat instead of zfishweb (needs postgresql permission)
RUN addgroup --gid 1000 zfin && \
    adduser --home /usr/local/tomcat --no-create-home --ingroup zfin --disabled-password --gecos '' zfishweb

USER zfishweb

COPY ./postgresql-42.2.18.jar  /usr/local/tomcat/lib

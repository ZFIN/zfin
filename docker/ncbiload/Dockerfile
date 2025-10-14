FROM base:latest

# Dockerfile for NCBI gene load test environment
# It hosts its own PostgreSQL database so it doesn't interfere with the db container
# Temporary container for testing only
# Needs a dbdump (named zfindbdump.bak in place in order to build)
# Copy the db dump to the same directory as this Dockerfile
USER root
RUN apt update && apt install -y postgresql-15
RUN /etc/init.d/postgresql start && \
    su - postgres -c "createuser --superuser gradle" && \
    su - postgres -c "createuser --superuser informix" && \
    su - postgres -c "createuser --superuser apache" && \
    su - postgres -c "createdb zfindb"

RUN perl -i -pe 's/peer/trust/' /etc/postgresql/15/main/pg_hba.conf && \
    perl -i -pe 's/scram-sha-256/trust/' /etc/postgresql/15/main/pg_hba.conf

ADD zfindbdump.bak /tmp/zfindbdump.bak
RUN /etc/init.d/postgresql start && \
    su - postgres -c "pg_restore -d zfindb -j 8 /tmp/zfindbdump.bak"

#Modify sudoers to allow gradle to run sudo without a password
RUN echo "gradle ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers

RUN mkdir /tmp/inputs

USER gradle
RUN echo "export PGHOST=localhost" >> /home/gradle/.profile
RUN echo "sudo /etc/init.d/postgresql start" >> /home/gradle/.profile

#Override the db host to point to localhost so we don't accidentally connect to another db
RUN echo 'echo 127.0.0.1 db | sudo tee -a /etc/hosts' >> /home/gradle/.profile

CMD /bin/bash -l -c "/etc/init.d/postgresql start ; while true; do sleep 100; done"


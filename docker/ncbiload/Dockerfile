FROM ghcr.io/zfin/zfin-base:main

# Dockerfile for NCBI gene load test environment
# It hosts its own PostgreSQL database so it doesn't interfere with the db container
# Temporary container for testing only
# Needs a dbdump (named zfindbdump.bak in place in order to build)
# Copy the db dump to the same directory as this Dockerfile
USER root
RUN apt update && apt install -y postgresql-18
RUN /etc/init.d/postgresql start && \
    su - postgres -c "createuser --superuser gradle" && \
    su - postgres -c "createuser --superuser informix" && \
    su - postgres -c "createuser --superuser apache" && \
    su - postgres -c "createdb zfindb"

RUN perl -i -pe 's/peer/trust/' /etc/postgresql/18/main/pg_hba.conf && \
    perl -i -pe 's/scram-sha-256/trust/' /etc/postgresql/18/main/pg_hba.conf

# If needed, create a new zfindbdump.bak file with: pg_dump -Fc zfindb -f zfindbdump.bak
ADD zfindbdump.bak /tmp/zfindbdump.bak
RUN /etc/init.d/postgresql start && \
    su - postgres -c "pg_restore -d zfindb -j 8 /tmp/zfindbdump.bak"

# Schema patches that landed after the slim dump was last refreshed.
# When zfindbdump.bak is regenerated past the corresponding changesets,
# delete the matching files from post-dump-patches/.
COPY post-dump-patches /tmp/post-dump-patches
RUN /etc/init.d/postgresql start && \
    for f in /tmp/post-dump-patches/*.sql; do \
        echo "Applying $f"; \
        su - postgres -c "psql -d zfindb -v ON_ERROR_STOP=1 -f $f" || exit 1; \
    done

#Modify sudoers to allow gradle to run sudo without a password
RUN echo "gradle ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers

RUN mkdir /tmp/inputs

USER gradle
RUN echo "export PGHOST=localhost" >> /home/gradle/.profile
RUN echo "sudo /etc/init.d/postgresql start" >> /home/gradle/.profile

#Override the db host to point to localhost so we don't accidentally connect to another db
RUN echo 'echo 127.0.0.1 db | sudo tee -a /etc/hosts' >> /home/gradle/.profile

CMD /bin/bash -l -c "/etc/init.d/postgresql start ; while true; do sleep 100; done"


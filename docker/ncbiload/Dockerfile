FROM base:latest
USER root
RUN apt install -y postgresql-15
RUN /etc/init.d/postgresql start && \
    su - postgres -c "createuser --superuser gradle" && \
    su - postgres -c "createuser --superuser informix" && \
    su - postgres -c "createdb zfindb"

RUN perl -i -pe 's/peer/trust/' /etc/postgresql/15/main/pg_hba.conf && \
    perl -i -pe 's/scram-sha-256/trust/' /etc/postgresql/15/main/pg_hba.conf

ADD 2025.05.08.1.bak /tmp/2025.05.08.1.bak
RUN /etc/init.d/postgresql start && \
    su - postgres -c "pg_restore -d zfindb -j 8 /tmp/2025.05.08.1.bak"

#Modify sudoers to allow gradle to run sudo without a password
RUN echo "gradle ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers

RUN mkdir /tmp/inputs
# ADD ncbi-load-characterization-inputs.tar.gz /tmp/inputs

USER gradle
RUN echo "export PGHOST=localhost" >> /home/gradle/.profile
RUN echo "sudo /etc/init.d/postgresql start" >> /home/gradle/.profile
RUN echo 'echo try this command: "OVERRIDE_JAVA_HOME=$JAVA_HOME NO_SLEEP=1 SKIP_DOWNLOADS=1 LOAD_NCBI_ONE_WAY_GENES=true DB_NAME=zfindb perl NCBI_gene_load.pl"' >> /home/gradle/.profile
RUN echo 'echo 127.0.0.1 db | sudo tee -a /etc/hosts' >> /home/gradle/.profile

CMD /bin/bash -l -c "/etc/init.d/postgresql start ; while true; do sleep 100; done"


FROM tomcat:9-jdk17

#Add imagemagick for generating thumbnails
RUN apt-get update && apt-get install -y \
  imagemagick \
  && rm -rf /var/lib/apt/lists/*

#TODO: figure out how to use tomcat instead of zfishweb (needs postgresql permission)
RUN addgroup --gid 1000 zfin && \
    adduser --home /usr/local/tomcat --no-create-home --ingroup zfin --disabled-password --gecos '' zfishweb

USER zfishweb

COPY ./postgresql-42.2.18.jar  /usr/local/tomcat/lib

RUN mkdir /research
RUN mkdir /mnt/research
RUN ln -s /mnt/research/vol/archive /research/zarchive
RUN ln -s /mnt/research/vol/blast /research/zblast
RUN ln -s /mnt/research/vol/blast /research/zblastfiles
RUN ln -s /mnt/research/vol/central /research/zcentral
RUN ln -s /mnt/research/vol/prod /research/zprod
RUN ln -s /mnt/research/vol/prod /research/zprodmore
RUN ln -s /mnt/research/vol/unloads /research/zunloads
RUN ln -s /mnt/research/vol/users /research/zusers
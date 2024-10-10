#!/bin/bash

source .env

#Usage
if [ $# -ne 1 ] ; then
    echo "Usage:"
    echo "  setup_blast.sh <BLASTDIR>"
    echo ""
    echo "BLASTDIR is the location you would like ab-blast binaries and blast databases stored in locally."
    exit
else
    BLASTDIR=$1
fi

mkdir -p $BLASTDIR

#scp -r ${DOCKER_SSH_USER}@watson.zfin.org:/opt/ab-blast $BLASTDIR
rsync -rlv ${DOCKER_SSH_USER}@watson.zfin.org:/opt/ab-blast $BLASTDIR

#scp -r ${DOCKER_SSH_USER}@watson.zfin.org:/opt/zfin/blastdb/Current $BLASTDIR
rsync -rlv ${DOCKER_SSH_USER}@watson.zfin.org:/opt/zfin/blastdb/Current $BLASTDIR

#User instructions
echo "Please modify your docker .env file as shown:"
echo ""
echo "DOCKER_ABBLAST_PATH=${BLASTDIR}/ab-blast"
echo "DOCKER_BLASTSERVER_BLAST_DATABASE_PATH=${BLASTDIR}/blast"
echo ""
echo "To run the blast container:"
echo "  docker compose build blast"
echo "  docker compose run --rm blast"
echo ""
echo "Example blast commands:"
echo "  /opt/ab-blast/xdget -n /opt/zfin/blastdb/Current/zfin_cdna_seq EH495222"
echo "  /opt/ab-blast/xdformat -n -i /opt/zfin/blastdb/Current/ensembl_zf"

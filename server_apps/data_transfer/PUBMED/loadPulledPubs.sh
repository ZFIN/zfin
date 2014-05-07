#!/bin/bash -e

if [ -e <!--|LOAD_PUBS_DIR|-->/parsePubs.log ] ;
  then 
  <!--|INFORMIX_DIR|-->/bin/dbaccess -a <!--|DB_NAME|--> <!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/loadNewPubs.sql ;
fi


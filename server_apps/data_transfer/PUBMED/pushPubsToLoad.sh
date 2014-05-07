#!/bin/bash -e

if [ -e <!--|LOAD_PUBS_DIR|-->/parsePubs.log ];
 then
    echo "old load file found: <!--|LOAD_PUBS_DIR|-->/<!--|DB_NAME|-->/pubsToLoad.txt, removing..."
    /bin/rm <!--|LOAD_PUBS_DIR|-->/parsePubs.log ;
fi

if [ -e <!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/parsePubs.log ];
 then 
    echo "pubs to load file found: <!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/parsePubs.log";
    /bin/cp <!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/parsePubs.log <!--|LOAD_PUBS_DIR|-->/parsePubs.log ;
 else
    echo "no file to load found, missing: <!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/parsePubs.log";
    exit 1;
fi

exit 0;

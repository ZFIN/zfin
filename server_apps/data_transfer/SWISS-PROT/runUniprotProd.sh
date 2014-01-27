#!/bin/bash -e

# $1 is the location on dev system where we should copy
# the *2go and ok* files to do the run on almost and production
# this script only runs on almost and production (automates the prod steps
# of the UniProt load.  See case 2799 for details.

if [ -z "$1" ]      
then
    echo "ERROR on usage: no file location to copy from dev to prod";
    exit;
fi

$copyFrom=$1

echo $copyFrom;

cp $copyFrom/ok* <!--|TARGETROOT|-->/server_apps/data_transfer/SWISS-PROT/;
cp $copyFrom/*2go <!--|TARGETROOT|-->/server_apps/data_transfer/SWISS-PROT/;

cd <!--|SOURCEROOT|-->/server_apps/data_transfer/SWISS-PROT/;
/local/bin/gmake run ;

cd <!--|SOURCEROOT|-->/server_apps/data_transfer/GO;
/local/bin/gmake run ;


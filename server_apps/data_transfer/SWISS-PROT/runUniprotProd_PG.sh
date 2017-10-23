#!/bin/bash -e

# $1 is the location on dev system where we should copy
# the *2go and ok* files to do the run on almost and production
# this script only runs on almost and production (automates the prod steps
# of the UniProt load.  See case 2799 for details.

echo "#########################################################################"

echo "Remove old files: okfile, *2go" 
/bin/rm -f <!--|TARGETROOT|-->/server_apps/data_transfer/SWISS-PROT/okfile
/bin/rm -f <!--|TARGETROOT|-->/server_apps/data_transfer/SWISS-PROT/ok2file
/bin/rm -f <!--|TARGETROOT|-->/server_apps/data_transfer/SWISS-PROT/ec2go
/bin/rm -f <!--|TARGETROOT|-->/server_apps/data_transfer/SWISS-PROT/interpro2go
/bin/rm -f <!--|TARGETROOT|-->/server_apps/data_transfer/SWISS-PROT/spkw2go
/bin/rm -f <!--|TARGETROOT|-->/server_apps/data_transfer/SWISS-PROT/*.txt

echo "#########################################################################"

echo "Copy new files from /research/zarchive/load_files/UniProt/" 
/usr/bin/scp /research/zarchive/load_files/UniProt/* <!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/

echo "#########################################################################"

cd <!--|SOURCEROOT|-->/server_apps/data_transfer/SWISS-PROT/;
/local/bin/gmake runPG ;

cd <!--|SOURCEROOT|-->/server_apps/data_transfer/GO;
/local/bin/gmake runPG ;


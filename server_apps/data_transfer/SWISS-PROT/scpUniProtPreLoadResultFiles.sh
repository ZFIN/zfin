#! /bin/sh
#  name: scpUniProtPreLoadResultFiles.sh
#  copy okfile, *2go files from /nfs/zygotix/zarchive/load_files/UniProt/
#  to <!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/
# These files will be serving as input for the UniProt load on almost

echo "#########################################################################"

echo "Remove old files: okfile, *2go" 
/bin/rm -f <!--|TARGETROOT|-->/server_apps/data_transfer/SWISS-PROT/okfile
/bin/rm -f <!--|TARGETROOT|-->/server_apps/data_transfer/SWISS-PROT/ec2go
/bin/rm -f <!--|TARGETROOT|-->/server_apps/data_transfer/SWISS-PROT/interpro2go
/bin/rm -f <!--|TARGETROOT|-->/server_apps/data_transfer/SWISS-PROT/spkw2go
echo "#########################################################################"

echo "Copy new files from /nfs/zygotix/zarchive/load_files/UniProt/"
/private/bin/scp /nfs/zygotix/zarchive/load_files/UniProt/* <!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/
echo "#########################################################################"


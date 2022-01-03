#!/bin/bash -e

# $1 is the location on dev system where we should copy
# the *2go and ok* files to do the run on almost and production
# this script only runs on almost and production (automates the prod steps
# of the UniProt load.  See case 2799 for details.

# Use environment variable "SKIP_CLEANUP" to tell script not to remove old files (and copy over server files) -- for troubleshooting
#
# Can run with those env vars in tcsh like so: (https://stackoverflow.com/questions/5946736/)
#     (  setenv SKIP_CLEANUP 1; ./runUniprotProd.sh )

main() {
if [ -z "$SKIP_CLEANUP" ]
then

	echo "#########################################################################"

	echo "Remove old files: okfile, *2go " $(date "+%Y-%m-%d %H:%M:%S")
	/bin/rm -f <!--|TARGETROOT|-->/server_apps/data_transfer/SWISS-PROT/okfile
	/bin/rm -f <!--|TARGETROOT|-->/server_apps/data_transfer/SWISS-PROT/ok2file
	/bin/rm -f <!--|TARGETROOT|-->/server_apps/data_transfer/SWISS-PROT/ec2go
	/bin/rm -f <!--|TARGETROOT|-->/server_apps/data_transfer/SWISS-PROT/interpro2go
	/bin/rm -f <!--|TARGETROOT|-->/server_apps/data_transfer/SWISS-PROT/spkw2go
	/bin/rm -f <!--|TARGETROOT|-->/server_apps/data_transfer/SWISS-PROT/*.txt

	echo "#########################################################################"

	echo "Copy new files from /research/zarchive/load_files/UniProt/" 
	/usr/bin/scp /research/zarchive/load_files/UniProt/* <!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/
else
	echo "Skipping copying files from server"
fi

echo "#########################################################################"

echo "MD5 sums of data input files"

md5sum /research/zarchive/load_files/UniProt/*

echo "#########################################################################"
echo "MD5 sums of data files after copy (should match)"
ls /research/zarchive/load_files/UniProt/ | xargs md5sum 
echo "#########################################################################"

echo "running loadsp.pl " $(date "+%Y-%m-%d %H:%M:%S")
./loadsp.pl ;

echo "running protein_domain_info_load.pl " $(date "+%Y-%m-%d %H:%M:%S")
./protein_domain_info_load.pl ;

echo "running go.pl " $(date "+%Y-%m-%d %H:%M:%S")
cd <!--|TARGETROOT|-->/server_apps/data_transfer/GO;
./go.pl ;

echo "#########################################################################"
echo "## FINISHED runUniprotProd.sh "      $(date "+%Y-%m-%d %H:%M:%S")
echo "#########################################################################"
}

main 2>&1 | tee load_log_$(date "+%Y-%m-%d_%H-%M-%S").txt

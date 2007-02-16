#! /bin/sh
#  name: pull.sh
#  to: call various data Pull scripts in a particular sequence
#
# ZGC-MEOW tightly coupled pull-push 
# want to avoid curators being able to rename a zgc clone before
# ncbi has had a chance to attach our zdb_id to their zgc-clone-name

echo "ZGC Pull: ### IS DISABLED ###"
#<!--|ROOT_PATH|-->/server_apps/data_transfer/ZGC/zgc.pl 
echo "#########################################################################"
echo "MEOW Push:"   #(whether the ZGC failed or not)
<!--|ROOT_PATH|-->/server_apps/data_transfer/MEOW/meow.pl
echo "#########################################################################"
echo "RefSeq (Entrez) Pull:" 
echo "Log File: <!--|ROOT_PATH|-->/server_apps/data_transfer/RefSeq/refseq.log"
<!--|ROOT_PATH|-->/server_apps/data_transfer/RefSeq/refseq.pl> <!--|ROOT_PATH|-->/server_apps/data_transfer/RefSeq/refseq.log 2>&1
echo "#########################################################################"
#echo "Swiss-Prot Pull:"# -- expected ~ 2004-Jun
#<!--|ROOT_PATH|-->/server_apps/data_transfer/ ... SP
#echo "#########################################################################"
echo "Get GenBank daily update :" 
<!--|ROOT_PATH|-->/server_apps/data_transfer/Genbank/gbaccession.pl
echo "#########################################################################"

echo "Unload production: "
<!--|ROOT_PATH|-->/server_apps/DB_maintenance/unload_production.sh
echo "#########################################################################"

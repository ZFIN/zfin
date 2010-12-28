#! /bin/sh
#  name: pull.sh
#  to: call various data Pull scripts in a particular sequence
#
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

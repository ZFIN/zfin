#! /bin/sh
#  name: pull.sh
#  to: call various data Pull scripts in a particular sequence
#
echo "#########################################################################"
echo "RefSeq (Entrez) Pull:" 
echo "Log File 1: <!--|SOURCEROOT|-->/server_apps/data_transfer/EntrezGene/log1"
echo "Log File 2: <!--|SOURCEROOT|-->/server_apps/data_transfer/EntrezGene/log2"
<!--|ROOT_PATH|-->/server_apps/data_transfer/EntrezGene/entrezGene.pl
echo "#########################################################################"

echo "Get GenBank daily update :" 
<!--|ROOT_PATH|-->/server_apps/data_transfer/Genbank/gbaccession.pl
echo "#########################################################################"

echo "Unload production: "
<!--|ROOT_PATH|-->/server_apps/DB_maintenance/unload_production.sh
echo "#########################################################################"



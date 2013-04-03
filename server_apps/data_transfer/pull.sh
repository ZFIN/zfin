#! /bin/sh
#  name: pull.sh
#  to: call various data Pull scripts in a particular sequence
#

echo "#########################################################################"

echo "Get GenBank daily update :" 
<!--|ROOT_PATH|-->/server_apps/data_transfer/Genbank/gbaccession.pl
echo "#########################################################################"

echo "Unload production: "
<!--|ROOT_PATH|-->/server_apps/DB_maintenance/unload_production.sh
echo "#########################################################################"



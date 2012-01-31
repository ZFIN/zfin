#! /bin/tcsh -e

#$1 db name
setenv INFORMIXSERVER waldo
setenv INFROMIXDIR /private/apps/Informix/informix/

echo "ready to start dropTables.sql" ;
/private/apps/Informix/informix/bin/dbaccess -a $DBNAME <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/fishMartAutomated.sql

exit 0;

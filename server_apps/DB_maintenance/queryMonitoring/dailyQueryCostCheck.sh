#!/bin/tcsh

# rm old reports

setenv INSTANCE <!--|INSTANCE|-->;

if ( -e /tmp/queryCostCheck.txt ) then
 /bin/rm /tmp/queryCostCheck.txt;
 /bin/touch /tmp/queryCostCheck.txt;

endif

echo "unloading daily RED alerts from sysadmin";

echo 'unload to /tmp/queryCostCheck.txt select * from mon_long_queries;' | /private/apps/Informix/informix/bin/dbaccess sysadmin ;

echo "sending RED alert (query cost exceeds 10000) email." ;

/local/bin/mutt -a /tmp/queryCostCheck.txt -s "query cost check for waldo" -- <!--|DB_OWNER|-->@zygotix.zfin.org < <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/char ; 

echo "delete from mon_long_queries" | /private/apps/Informix/informix/bin/dbaccess sysadmin;

exit

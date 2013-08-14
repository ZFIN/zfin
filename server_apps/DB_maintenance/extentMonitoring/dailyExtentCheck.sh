#!/bin/tcsh

# rm old reports

setenv INSTANCE <!--|INSTANCE|-->;

if ( -e /tmp/extentCheck.txt ) then
 /bin/rm /tmp/extentCheck.txt;
 /bin/touch /tmp/extentCheck.txt;

endif

echo "unloading daily RED alerts from sysadmin";

echo 'unload to /tmp/extentCheck.txt select * from mon_extents;' | /private/apps/Informix/informix/bin/dbaccess sysadmin ;

echo "sending RED alert (extents exceed 10) email." ;

/local/bin/mutt -a /tmp/extentCheck.txt -s "extent check for almdb" -- <!--|DB_OWNER|-->@zfin.org < <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/char ; 

echo "delete from mon_extents" | /private/apps/Informix/informix/bin/dbaccess sysadmin;

exit

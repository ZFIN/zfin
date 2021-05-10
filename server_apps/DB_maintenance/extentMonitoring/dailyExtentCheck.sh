#!/bin/tcsh

# rm old reports

setenv INSTANCE <!--|INSTANCE|-->;
cd <!--|TARGETROOT|-->/server_apps/DB_maintenance/extentMonitoring

if ( -e extentCheck.txt ) then
 /bin/rm extentCheck.txt;
 /bin/touch extentCheck.txt;

endif

echo "unloading daily RED alerts from sysadmin";

echo 'unload to extentCheck.txt select * from mon_extents;' | /private/apps/Informix/informix/bin/dbaccess -a sysadmin ;

echo "sending RED alert (extents exceed 10) email." ;

echo "delete from mon_extents" | /private/apps/Informix/informix/bin/dbaccess -a sysadmin;

exit

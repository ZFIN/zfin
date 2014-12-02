#!/bin/tcsh

# rm old reports

setenv INSTANCE <!--|INSTANCE|-->;
cd <!--|TARGETROOT|-->/server_apps/DB_maintenance/extentMonitoring

if ( -e labAddressCheck.txt ) then
 /bin/rm labAddressCheck.txt;
 /bin/touch labAddressCheck.txt;

endif

echo 'unload to labAddressCheck.txt select * from lab_address_update_tracking' | /private/apps/Informix/informix/bin/dbaccess <!--|DB_NAME|--> ;

if ( -s labAddressCheck.txt ) then
    echo 'delete from lab_address_update_tracking' | /private/apps/Informix/informix/bin/dbaccess -a <!--|DB_NAME|--> ;
endif


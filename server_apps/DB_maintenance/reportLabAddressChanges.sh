#!/bin/tcsh

# rm old reports

setenv INSTANCE <!--|INSTANCE|-->;

if ( -e /tmp/labAddressCheck.txt ) then
 /bin/rm /tmp/labAddressCheck.txt;
 /bin/touch /tmp/labAddressCheck.txt;

endif

echo 'unload to /tmp/labAddressCheck.txt select * from lab_address_update_tracking' | /private/apps/Informix/informix/bin/dbaccess <!--|DB_NAME|--> ;

if ( -s /tmp/labAddressCheck.txt ) then

 
    /local/bin/mutt -a /tmp/labAddressCheck.txt -s "lab address has changed on <!--|DB_NAME|-->" -- <!--|DB_OWNER|-->@cs.uoregon.edu < <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/char ; 

    echo 'delete from lab_address_update_tracking' | /private/apps/Informix/informix/bin/dbaccess <!--|DB_NAME|--> ;

endif


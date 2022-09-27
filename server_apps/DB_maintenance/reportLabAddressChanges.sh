#!/bin/tcsh

# rm old reports
cd ${TARGETROOT}/server_apps/DB_maintenance/extentMonitoring

if ( -e labAddressCheck.txt ) then
 /bin/rm labAddressCheck.txt;
 /bin/touch labAddressCheck.txt;
endif

echo '\COPY lab_address_update_tracking TO labAddressCheck.txt' | ${PGBINDIR}/psql -v ON_ERROR_STOP=1 ${DBNAME};

if ( -s labAddressCheck.txt ) then
    echo 'DELETE FROM lab_address_update_tracking' | ${PGBINDIR}/psql -v ON_ERROR_STOP=1 ${DBNAME};
endif


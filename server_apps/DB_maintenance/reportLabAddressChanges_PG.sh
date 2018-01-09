#!/bin/tcsh

# rm old reports
cd ${TARGETROOT}/server_apps/DB_maintenance/extentMonitoring

if ( -e labAddressCheck.txt ) then
 /bin/rm labAddressCheck.txt;
 /bin/touch labAddressCheck.txt;
endif

echo '\COPY lab_address_update_tracking TO labAddressCheck.txt' | ${PGBINDIR}/psql ${DBNAME};

if ( -s labAddressCheck.txt ) then
    echo 'DELETE FROM lab_address_update_tracking' | ${PGBINDIR}/psql ${DBNAME};
endif


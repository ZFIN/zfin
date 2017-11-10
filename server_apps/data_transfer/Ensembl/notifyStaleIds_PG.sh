#!/bin/tcsh

# rm old reports

setenv INSTANCE <!--|INSTANCE|-->;


if ( -s <!--|TARGETROOT|-->/server_apps/data_transfer/Ensembl/changedSangerEnsdargs.txt ) then

    /local/bin/mutt -a <!--|TARGETROOT|-->/server_apps/data_transfer/Ensembl/changedSangerEnsdargs.txt -s "ids from SangerMutantLoad no longer found in Ensembl release on <!--|DB_NAME|-->" -- <!--|DEFAULT_EMAIL|--> < <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/char ;

endif


exit

#!/bin/tcsh -e 

<!--|ROOT_PATH|-->/server_apps/DB_maintenance/unload_production.sh;

if ($? != "0") then
    echo "unload_production.sh failed.";
endif

## add bit to check the last unload.

<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/runWarehouse.pl

if ($? != "0") then
    echo "runWarehouse.pl failed.";
endif


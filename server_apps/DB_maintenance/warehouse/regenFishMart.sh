#!/bin/tcsh

# rm old reports

if ( -e <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/runFishMartReport.txt) then
 /bin/rm <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/runFishMartReport.txt

endif

if ( -e <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/regenFishMartReport.txt) then
 /bin/rm <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/regenFishMartReport.txt

endif

if ( -e <!--|ROOT_PATH|-->/reports/fishMartUnitTests.txt) then
 /bin/rm <!--|ROOT_PATH|-->/reports/fishMartUnitTests.txt
endif


echo "done with file delete" ;
# build up the warehouse
<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/runFishMart.sh >&! <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/runFishMartReport.txt

if ($? != 0) then
 echo "trying to send notification runFishMart";
 /local/bin/mutt -a <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/runFishMartReport.txt -s "regen fish mart (the building tables, not the public tables) failed" -- staylor@cs.uoregon.edu; 
exit 1
endif

echo "done with runfishmart";
# run the validation tests via ant.

cd <!--|SOURCEROOT|-->

echo "cd'd to <!--|SOURCEROOT|-->" ;

/private/bin/ant run-fishmart-unittests >&! reports/fishmartUnitTests.txt

if ($? != 0) then
   echo "trying to send notification unit tests";  
 /local/bin/mutt -a <!--|ROOT_PATH|-->/reports/fishMartUnitTests.txt -s "regen fish mart (the building tables, not the public tables) failed" -- staylor@cs.uoregon.edu; 
exit 1
endif

echo "done with ant tests" ;

# move the current table data to backup, move the new data to current.

<!--|INFORMIX_DIR|-->/bin/dbaccess -a <!--|DB_NAME|--> <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/fishMartRegen.sql >&! <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/regenFishMartReport.txt

if ($? != 0) then
   echo "trying to send notification regenFishMartReport";  
 /local/bin/mutt -a <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/regenFishMartReport.txt -s "refresh fish mart (the public tables) failed and was rolled back" -- staylor@cs.uoregon.edu; exit 1
endif



exit 0;

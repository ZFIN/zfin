#!/bin/tcsh

# rm old reports

if ( -e <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/runFishMartReport.txt) then
 /bin/rm <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/runFishMartReport.txt

endif

if ( -e <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/regenFishMartReport.txt) then
 /bin/rm <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/regenFishMartReport.txt

endif

if ( -e <!--|SOURCEROOT|-->/reports/tests/fishMartUnitTests.txt) then
 /bin/rm <!--|SOURCEROOT|-->/reports/tests/fishMartUnitTests.txt
endif


echo "done with file delete" ;
# build up the warehouse
<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/runFishMart.sh <!--|DB_NAME|--> >&! <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/runFishMartReport.txt

if ($? != 0) then
 echo "trying to send notification runFishMart";
 /local/bin/mutt -a <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/runFishMartReport.txt -s "regen fish mart (the building tables, not the public tables) failed" -- <!--|DB_OWNER|-->@cs.uoregon.edu < <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/char; 
exit 1;
endif

echo "done with runfishmart";
# run the validation tests via ant.

cd <!--|SOURCEROOT|-->

echo "cd'd to <!--|SOURCEROOT|-->" ;

/private/bin/ant run-fishmart-unittests >&! reports/tests/fishMartUnitTests.txt

if ($? != 0) then
   echo "trying to send notification unit tests";  
 /local/bin/mutt -a <!--|SOURCEROOT|-->/reports/tests/fishMartUnitTests.txt -s "regen fish mart (the building tables, not the public tables) failed" -- <!--|DB_OWNER|-->@cs.uoregon.edu < <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/char ; 
exit 1;
endif

echo "done with ant tests" ;

# move the current table data to backup, move the new data to current.

<!--|INFORMIX_DIR|-->/bin/dbaccess -a <!--|DB_NAME|--> <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/fishMartRegen.sql >&! <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/regenFishMartReport.txt

if ($? != 0) then
   echo "trying to send notification regenFishMartReport";  
 /local/bin/mutt -a <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/regenFishMartReport.txt -s "refresh fish mart (the public tables) failed and was rolled back" -- <!--|DB_OWNER|-->@cs.uoregon.edu < <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/char; 
exit 1;
endif

echo "sending success email." ;

/local/bin/mutt -a <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/regenFishMartReport.txt -s "regen fishmart successful." -- <!--|DB_OWNER|-->@cs.uoregon.edu < <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/char ; 

exit 0;

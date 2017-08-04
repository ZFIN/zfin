#!/bin/tcsh

# rm old reports

setenv INSTANCE <!--|INSTANCE|-->;

if ( -e <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/constructMart/runConstructMartReport.txt) then
 /bin/rm <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/constructMart/runConstructMartReport.txt

endif

if ( -e <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/constructMart/regenConstructMartReport.txt) then
 /bin/rm <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/constructMart/regenConstructMartReport.txt

endif

if ( -e <!--|SOURCEROOT|-->/reports/tests/constructMartUnitTests.txt) then
 /bin/rm <!--|SOURCEROOT|-->/reports/tests/constructMartUnitTests.txt
endif

if ( -e <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/constructMart/constructMartUnitTests.txt) then
 /bin/rm <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/constructMart/constructMartUnitTests.txt
endif


echo "done with file delete" ;
# build up the warehouse
<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/constructMart/runConstructMart.sh <!--|DB_NAME|--> >&! <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/constructMart/runConstructMartReport.txt

if ($? != 0) then
 echo "egen construct mart (the building tables, not the public tables) failed on";
exit 1;
endif

echo "done with runconstructmart on <!--|DB_NAME|-->";
# run the validation tests via ant.

cd <!--|SOURCEROOT|-->
echo "cd'd to <!--|SOURCEROOT|-->" ;

/private/bin/ant run-constructmart-unittests >&! reports/tests/constructMartUnitTests.txt
cp reports/tests/constructMartUnitTests.txt <!--|TARGETROOT|-->/server_apps/DB_maintenance/warehouse/constructMart/.

if ($? != 0) then
   echo "regen construct mart (the building tables, not the public tables) failed on unit tests";  
exit 1;
endif

#echo "done with ant tests" ;

# move the current table data to backup, move the new data to current.

<!--|INFORMIX_DIR|-->/bin/dbaccess -a <!--|DB_NAME|--> <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/constructMart/constructMartRegen.sql >&! <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/constructMart/regenConstructMartReport.txt

if ($? != 0) then
   echo "refresh construct mart (the public tables) failed and was rolled back";
exit 1;
endif

echo "success" ;

exit 0;

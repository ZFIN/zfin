#!/bin/tcsh

# rm old reports

setenv INSTANCE <!--|INSTANCE|-->;

if ( -e <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/runChromosomeMartReport.txt) then
 /bin/rm <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/runChromosomeMartReport.txt

endif

if ( -e <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/regenChromosomeMartReport.txt) then
 /bin/rm <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/regenChromosomeMartReport.txt

endif

if ( -e <!--|SOURCEROOT|-->/reports/tests/chromosomeMartUnitTests.txt) then
 /bin/rm <!--|SOURCEROOT|-->/reports/tests/chromosomeMartUnitTests.txt
endif


echo "done with file delete" ;
# build up the warehouse
<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/chromosomeMart/runChromosomeMart.sh <!--|DB_NAME|--> >&! <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/runChromosomeMartReport.txt

if ($? != 0) then
 echo "trying to send notification runChromosomeMart";
 /local/bin/mutt -a <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/runChromosomeMartReport.txt -s "regen chromosome mart (the building tables, not the public tables) failed on <!--|DB_NAME|-->" -- <!--|DB_OWNER|-->@cs.uoregon.edu < <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/char; 
exit 1;
endif

echo "done with runchromosomemart on <!--|DB_NAME|--> ";
# run the validation tests via ant.

cd <!--|SOURCEROOT|-->
echo "cd'd to <!--|SOURCEROOT|-->" ;

/private/bin/ant run-chromosomemart-unittests >&! reports/tests/chromosomeMartUnitTests.txt

if ($? != 0) then
   echo "trying to send notification unit tests on <!--|DB_NAME|--> ";  
 /local/bin/mutt -a <!--|SOURCEROOT|-->/reports/tests/chromosomeMartUnitTests.txt -s "regen chromosome mart (the building tables, not the public tables) failed on <!--|DB_NAME|--> " -- <!--|DB_OWNER|-->@cs.uoregon.edu < <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/char ; 
exit 1;
endif

#echo "done with ant tests" ;

# move the current table data to backup, move the new data to current.

<!--|INFORMIX_DIR|-->/bin/dbaccess -a <!--|DB_NAME|--> <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/chromosomeMart/chromosomeMartRegen.sql >&! <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/regenChromosomeMartReport.txt

if ($? != 0) then
   echo "trying to send notification regenChromosomeMartReport on <!--|DB_NAME|-->";  
 /local/bin/mutt -a <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/regenChromosomeMartReport.txt -s "refresh chromosome mart (the public tables) failed and was rolled back on <!--|DB_NAME|-->" -- <!--|DB_OWNER|-->@cs.uoregon.edu < <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/char; 
exit 1;
endif

echo "sending success email." ;

/local/bin/mutt -a <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/regenChromosomeMartReport.txt -s "regen chromosomemart successful on <!--|DB_NAME|-->." -- <!--|DB_OWNER|-->@cs.uoregon.edu < <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/char ; 

exit 0;

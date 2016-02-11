#!/bin/tcsh

# rm old reports
date;
setenv INSTANCE <!--|INSTANCE|-->;

if ( -e <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/phenotypeMart/runPhenotypeMartReport.txt) then
 /bin/rm <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/phenotypeMart/runPhenotypeMartReport.txt

endif

if ( -e <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/phenotypeMart/regenPhenotypeMartReport.txt) then
 /bin/rm <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/phenotypeMart/regenPhenotypeMartReport.txt

endif

if ( -e <!--|SOURCEROOT|-->/reports/tests/phenotypeMartUnitTests.txt) then
 /bin/rm <!--|SOURCEROOT|-->/reports/tests/phenotypeMartUnitTests.txt
endif

if ( -e <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/phenotypeMart/phenotypeMartUnitTests.txt) then
 /bin/rm <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/phenotypeMart/phenotypeMartUnitTests.txt
endif


echo "done with file delete" ;
# build up the warehouse
<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/phenotypeMart/runPhenotypeMart.sh <!--|DB_NAME|--> >&! <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/phenotypeMart/runPhenotypeMartReport.txt

if ($? != 0) then
 echo "regen phenotype mart (the building tables, not the public tables) failed on";
exit 1;
endif

echo "done with runphenotypemart on temp tables <!--|DB_NAME|-->";
# run the validation tests via ant.
date;
cd <!--|SOURCEROOT|-->
echo "cd'd to <!--|SOURCEROOT|-->" ;

/private/bin/ant run-phenotypemart-unittests >&! reports/tests/phenotypeMartUnitTests.txt
cp reports/tests/phenotypeMartUnitTests.txt <!--|TARGETROOT|-->/server_apps/DB_maintenance/warehouse/phenotypeMart/.

if ($? != 0) then
   echo "regen phenotype mart (the building tables, not the public tables) failed on unit tests";  
exit 1;
endif

date;
echo "done with phenotype mart building public" ;


# move the current table data to backup, move the new data to current.

<!--|INFORMIX_DIR|-->/bin/dbaccess -a <!--|DB_NAME|--> <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/phenotypeMart/phenotypeMartRegen.sql >&! <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/phenotypeMart/regenPhenotypeMartReport.txt

if ($? != 0) then
   echo "refresh phenotype mart (the public tables) failed and was rolled back";
exit 1;
endif

echo "done regen public phenotype tables" ;
date;

echo "start regen_genox()";

echo "execute procedure regen_genox()" | /private/apps/Informix/informix/bin/dbaccess $DBNAME;

date;
echo "done with regen_genox()";

echo "start regen_anatomy_counts()";
echo "execute procedure regen_anatomy_counts()" | /private/apps/Informix/informix/bin/dbaccess $DBNAME;
date;
echo "done with regen_anatomy_counts()";


echo "start regen_pheno_term_regen()";
echo "$TARGETROOT/server_apps/DB_maintenance/pheno/pheno_term_regen.sql" | dbaccess $DBNAME
date;
echo "done with pheno_term_regen()";




echo "success" ;

exit 0;

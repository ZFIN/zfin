#! /bin/tcsh -e

#$1 db name
setenv INFORMIXSERVER waldo
setenv INFROMIXDIR /private/apps/Informix/informix/

/bin/rm -rf <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/fishMartAutomated.sql 

/bin/cat begin.sql dropTables.sql schemaTables.sql geneGroup.sql environmentGroup.sql featureGroup.sql morpholinoGroup.sql constructGroup.sql populateFunctionalAnnotation.sql phenoFigs.sql xpatFigs.sql phenoTermGroup.sql addGroupsToFunctionalAnnotation.sql addAliasesToFunctionalAnnotation.sql addCountsToFunctionalAnnotation.sql populateFishAnnotationSearch.sql populateFigureTermFishSearch.sql populateFigureTermFishSearch.sql translateFeatureTypeForTGs.sql addBackgroundsToFish.sql populateNullOrderingColumns.sql createBtsIndexes.sql commit.sql > <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/fishMartAutomated.sql

echo "ready to start dropTables.sql" ;
/private/apps/Informix/informix/bin/dbaccess -a $DBNAME <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/fishMartAutomated.sql


exit 0;

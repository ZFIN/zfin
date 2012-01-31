#! /bin/tcsh -e

#$1 db name
setenv INFORMIXSERVER waldo
setenv INFROMIXDIR /private/apps/Informix/informix/

echo "ready to start dropTables.sql" ;
/private/apps/Informix/informix/bin/dbaccess -a $1 <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/dropTables.sql
echo "ready to start schemaTables.sql" ;
/private/apps/Informix/informix/bin/dbaccess -a $1 <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/schemaTables.sql
echo "ready to start geneGroup.sql" ;
/private/apps/Informix/informix/bin/dbaccess -a $1 <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/geneGroup.sql
echo "ready to start environmentGroup.sql" ;
/private/apps/Informix/informix/bin/dbaccess -a $1 <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/environmentGroup.sql
echo "ready to start featureGroup.sql" ;
/private/apps/Informix/informix/bin/dbaccess -a $1 <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/featureGroup.sql
echo "ready to start morpholinoGroup.sql" ;
/private/apps/Informix/informix/bin/dbaccess -a $1 <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/morpholinoGroup.sql
echo "ready to start constructGroup.sql" ;
/private/apps/Informix/informix/bin/dbaccess -a $1 <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/constructGroup.sql
echo "ready to start populateFunctionalAnnotation.sql" ;
/private/apps/Informix/informix/bin/dbaccess -a $1 <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/populateFunctionalAnnotation.sql
echo "ready to start phenoFigs.sql" ;
/private/apps/Informix/informix/bin/dbaccess -a $1 <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/phenoFigs.sql
echo "ready to start xpatFigs.sql" ;
/private/apps/Informix/informix/bin/dbaccess -a $1 <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/xpatFigs.sql
echo "ready to start phenoTermGroup.sql" ;
/private/apps/Informix/informix/bin/dbaccess -a $1 <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/phenoTermGroup.sql
echo "ready to start addGroupsToFunctionalAnnotation.sql" ;
/private/apps/Informix/informix/bin/dbaccess -a $1 <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/addGroupsToFunctionalAnnotation.sql
echo "ready to start addAliasesToFunctionalAnnotation.sql" ;
/private/apps/Informix/informix/bin/dbaccess -a $1 <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/addAliasesToFunctionalAnnotation.sql
echo "ready to start addCountsToFunctionalAnnotation.sql" ;
/private/apps/Informix/informix/bin/dbaccess -a $1 <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/addCountsToFunctionalAnnotation.sql
echo "ready to start populateFishAnnotationSearch.sql";
/private/apps/Informix/informix/bin/dbaccess -a $1 <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/populateFishAnnotationSearch.sql
echo "ready to start populateGeneFeatureFastSearch.sql";
/private/apps/Informix/informix/bin/dbaccess -a $1 <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/populateGeneFeatureResultView.sql
echo "ready to start populateFigureTermFishSearch.sql";
/private/apps/Informix/informix/bin/dbaccess -a $1 <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/populateFigureTermFishSearch.sql
echo "ready to start translateFeatureTypeForTGs.sql";
/private/apps/Informix/informix/bin/dbaccess -a $1 <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/translateFeatureTypeForTGs.sql
echo "ready to start addBackgroundsToFish.sql";
/private/apps/Informix/informix/bin/dbaccess -a $1 <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/addBackgroundsToFish.sql
echo "ready to start populateNullOrderingColumns.sql";
/private/apps/Informix/informix/bin/dbaccess -a $1 <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/populateNullOrderingColumns.sql
echo "create bts indexes";
/private/apps/Informix/informix/bin/dbaccess -a $1 <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/createBtsIndexes.sql

exit 0;

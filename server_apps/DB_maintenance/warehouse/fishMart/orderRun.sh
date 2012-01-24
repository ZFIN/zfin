#! /bin/tcsh

#$1 db name
setenv INFORMIXSERVER waldo
setenv INFROMIXDIR /private/apps/Informix/informix/

echo "ready to start dropTables.sql" ;
/private/apps/Informix/informix/bin/dbaccess -a $1 dropTables.sql
echo "ready to start schemaTables.sql" ;
/private/apps/Informix/informix/bin/dbaccess -a $1 schemaTables.sql
echo "ready to start geneGroup.sql" ;
/private/apps/Informix/informix/bin/dbaccess -a $1 geneGroup.sql
echo "ready to start environmentGroup.sql" ;
/private/apps/Informix/informix/bin/dbaccess -a $1 environmentGroup.sql
echo "ready to start featureGroup.sql" ;
/private/apps/Informix/informix/bin/dbaccess -a $1 featureGroup.sql
echo "ready to start morpholinoGroup.sql" ;
/private/apps/Informix/informix/bin/dbaccess -a $1 morpholinoGroup.sql
echo "ready to start constructGroup.sql" ;
/private/apps/Informix/informix/bin/dbaccess -a $1 constructGroup.sql
echo "ready to start populateFunctionalAnnotation.sql" ;
/private/apps/Informix/informix/bin/dbaccess -a $1 populateFunctionalAnnotation.sql
echo "ready to start phenoFigs.sql" ;
/private/apps/Informix/informix/bin/dbaccess -a $1 phenoFigs.sql
echo "ready to start xpatFigs.sql" ;
/private/apps/Informix/informix/bin/dbaccess -a $1 xpatFigs.sql
echo "ready to start phenoTermGroup.sql" ;
/private/apps/Informix/informix/bin/dbaccess -a $1 phenoTermGroup.sql
echo "ready to start addGroupsToFunctionalAnnotation.sql" ;
/private/apps/Informix/informix/bin/dbaccess -a $1 addGroupsToFunctionalAnnotation.sql
echo "ready to start addAliasesToFunctionalAnnotation.sql" ;
/private/apps/Informix/informix/bin/dbaccess -a $1 addAliasesToFunctionalAnnotation.sql
echo "ready to start addCountsToFunctionalAnnotation.sql" ;
/private/apps/Informix/informix/bin/dbaccess -a $1 addCountsToFunctionalAnnotation.sql
echo "ready to start populateFishAnnotationSearch.sql";
/private/apps/Informix/informix/bin/dbaccess -a $1 populateFishAnnotationSearch.sql
echo "ready to start populateGeneFeatureFastSearch.sql";
/private/apps/Informix/informix/bin/dbaccess -a $1 populateGeneFeatureResultView.sql
echo "ready to start populateFigureTermFishSearch.sql";
/private/apps/Informix/informix/bin/dbaccess -a $1 populateFigureTermFishSearch.sql
echo "ready to start translateFeatureTypeForTGs.sql";
/private/apps/Informix/informix/bin/dbaccess -a $1 translateFeatureTypeForTGs.sql
echo "ready to start addBackgroundsToFish.sql";
/private/apps/Informix/informix/bin/dbaccess -a $1 addBackgroundsToFish.sql
echo "ready to start populateNullOrderingColumns.sql";
/private/apps/Informix/informix/bin/dbaccess -a $1 populateNullOrderingColumns.sql
echo "ready to start addGenoTriggers.sql";
/private/apps/Informix/informix/bin/dbaccess -a $1 addGenoTriggers.sql
echo "create bts indexes";
/private/apps/Informix/informix/bin/dbaccess -a $1 createBtsIndexes.sql

#! /bin/tcsh -e

#$1 db name
setenv INFORMIXSERVER <!--|INFORMIX_SERVER|-->
setenv INFROMIXDIR <!--|INFORMIX_DIR|-->
set FISHMARTDIR=<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart 
set FULL_SCRIPT_FILE=$FISHMARTDIR/fishMartAutomated.sql 
set CONVERT_FISHMART_FILE=$FISHMARTDIR/fishMartRegen.sql
set ALL_FISHMART_SCRIPTS=$FISHMARTDIR/allFishMart.sql

/bin/rm -rf $FULL_SCRIPT_FILE 
/bin/rm -rf $CONVERT_FISHMART_FILE 
/bin/rm -rf $ALL_FISHMART_SCRIPTS

set fishMartScripts=(begin.sql \
	     dropTables.sql \
	     schemaTables.sql \
	     commit.sql \
	     begin.sql \
	     geneGroup.sql \
	     environmentGroup.sql \
	     featureGroup.sql \
	     morpholinoGroup.sql \
	     constructGroup.sql \
	     commit.sql \
	     begin.sql \
	     populateFunctionalAnnotation.sql \
	     commit.sql \
	     begin.sql \
	     phenoFigs.sql \
	     xpatFigs.sql \
	     commit.sql \
	     begin.sql \
	     phenoTermGroup.sql \
	     commit.sql \
	     begin.sql \
	     addGroupsToFunctionalAnnotation.sql \
	     commit.sql \
	     begin.sql \
	     addAliasesToFunctionalAnnotation.sql \
	     commit.sql \
	     begin.sql \
	     addCountsToFunctionalAnnotation.sql \
	     commit.sql \
	     begin.sql \
	     populateFishAnnotationSearch.sql \
	     commit.sql \
	     begin.sql \
	     addXpatCounts.sql \
	     commit.sql \
	     begin.sql \
	     populateGeneFeatureResultView.sql \
	     commit.sql \
	     begin.sql \
	     populateFigureTermFishSearch.sql \
	     commit.sql \
	     begin.sql \
	     translateFeatureTypeForTGs.sql \
	     commit.sql \
	     begin.sql \
	     addBackgroundsToFish.sql \
	     commit.sql \
	     begin.sql \
	     populateNullOrderingColumns.sql \
	     commit.sql \
	     begin.sql \
	     createTempBtsIndexes.sql \
	     commit.sql \
	     )


set regenFishMartScripts=(begin.sql \
	     dropBtsIndexes.sql \
	     commit.sql \
	     begin.sql \
	     refreshFishMart.sql \
	     createBtsIndexes.sql \
	     commit.sql \
	     )

set fullFishMartScripts=(begin.sql \
	     dropTables.sql \
	     schemaTables.sql \
	     commit.sql \
	     begin.sql \
	     geneGroup.sql \
	     environmentGroup.sql \
	     featureGroup.sql \
	     morpholinoGroup.sql \
	     constructGroup.sql \
	     commit.sql \
	     begin.sql \
	     populateFunctionalAnnotation.sql \
	     commit.sql \
	     begin.sql \
	     phenoFigs.sql \
	     xpatFigs.sql \
	     commit.sql \
	     begin.sql \
	     phenoTermGroup.sql \
	     commit.sql \
	     begin.sql \
	     addGroupsToFunctionalAnnotation.sql \
	     commit.sql \
	     begin.sql \
	     addAliasesToFunctionalAnnotation.sql \
	     commit.sql \
	     begin.sql \
	     addCountsToFunctionalAnnotation.sql \
	     commit.sql \
	     begin.sql \
	     populateFishAnnotationSearch.sql \
	     commit.sql \
	     begin.sql \
	     populateGeneFeatureResultView.sql \
	     commit.sql \
	     begin.sql \
	     populateFigureTermFishSearch.sql \
	     commit.sql \
	     begin.sql \
	     translateFeatureTypeForTGs.sql \
	     commit.sql \
	     begin.sql \
	     addBackgroundsToFish.sql \
	     commit.sql \
	     begin.sql \
	     populateNullOrderingColumns.sql \
	     commit.sql \
	     begin.sql \
	     createTempBtsIndexes.sql \
	     commit.sql \
	     begin.sql \
	     dropBtsIndexes.sql \
	     commit.sql \
	     begin.sql \
	     refreshFishMart.sql \
	     createBtsIndexes.sql \
	     commit.sql \
	     )

touch $FULL_SCRIPT_FILE
touch $CONVERT_FISHMART_FILE
touch $ALL_FISHMART_SCRIPTS

foreach name ($fishMartScripts)
   #echo $FISHMARTDIR$name
   cat $FISHMARTDIR/$name >> $FULL_SCRIPT_FILE
end

foreach name ($regenFishMartScripts)
   #echo $FISHMARTDIR$name
   cat $FISHMARTDIR/$name >> $CONVERT_FISHMART_FILE
end

foreach name ($fullFishMartScripts)
   #echo $FISHMARTDIR$name
   cat $FISHMARTDIR/$name >> $ALL_FISHMART_SCRIPTS
end

if ("X$1" == "X") then
echo "ready to start dropTables.sql DBNAME from environment." ;
/private/apps/Informix/informix/bin/dbaccess -a $DBNAME $FULL_SCRIPT_FILE
else
echo "ready to start dropTables.sql DBNAME provided from script call." ;
/private/apps/Informix/informix/bin/dbaccess -a $1 $FULL_SCRIPT_FILE

endif 


exit 0;

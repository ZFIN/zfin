#! /bin/tcsh -e

#$1 db name
setenv INFORMIXSERVER <!--|INFORMIX_SERVER|-->
setenv INFROMIXDIR <!--|INFORMIX_DIR|-->
set FISHMARTDIR=<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart 
set FULL_SCRIPT_FILE=$FISHMARTDIR/fishMartAutomated.sql 

/bin/rm -rf $FULL_SCRIPT_FILE 

set scripts=(begin.sql \
	     dropTables.sql \
	     schemaTables.sql \
	     geneGroup.sql \
	     environmentGroup.sql \
	     featureGroup.sql \
	     morpholinoGroup.sql \
	     constructGroup.sql \
	     populateFunctionalAnnotation.sql \
	     phenoFigs.sql \
	     xpatFigs.sql \
	     phenoTermGroup.sql \
	     addGroupsToFunctionalAnnotation.sql \
	     addAliasesToFunctionalAnnotation.sql \
	     addCountsToFunctionalAnnotation.sql \
	     populateFishAnnotationSearch.sql \
	     populateGeneFeatureResultView.sql \
	     populateFigureTermFishSearch.sql \
	     translateFeatureTypeForTGs.sql \
	     addBackgroundsToFish.sql \
	     populateNullOrderingColumns.sql \
	     createBtsIndexes.sql \
	     commit.sql \
	     )

set fullList=

touch $FULL_SCRIPT_FILE

foreach name ($scripts)
   #echo $FISHMARTDIR$name
   cat $FISHMARTDIR/$name >> $FULL_SCRIPT_FILE
end

if ("X$1" == "X") then
echo "ready to start dropTables.sql DBNAME from environment." ;
/private/apps/Informix/informix/bin/dbaccess -a $DBNAME $FULL_SCRIPT_FILE
else
echo "ready to start dropTables.sql DBNAME provided from script call." ;
/private/apps/Informix/informix/bin/dbaccess -a $1 $FULL_SCRIPT_FILE

endif 


exit 0;

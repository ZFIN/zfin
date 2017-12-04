#! /bin/tcsh -e

#$1 db name
set PHENOTYPEMARTDIR=<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/phenotypeMart
set FULL_SCRIPT_FILE=$PHENOTYPEMARTDIR/phenotypeMartAutomated_PG.sql 
set CONVERT_PHENOTYPEMART_FILE=$PHENOTYPEMARTDIR/phenotypeMartRegen_PG.sql
set ALL_PHENOTYPEMART_SCRIPTS=$PHENOTYPEMARTDIR/allPhenotypeMart_PG.sql

/bin/rm -rf $FULL_SCRIPT_FILE 
/bin/rm -rf $CONVERT_PHENOTYPEMART_FILE 
/bin/rm -rf $ALL_PHENOTYPEMART_SCRIPTS

set phenotypeMartScripts=( begin_PG.sql \
	     truncatePGTable_PG.sql \
	     commit_PG.sql \
	     begin_PG.sql \
	     truncatePGSTable_PG.sql \
	     commit_PG.sql \
	     begin_PG.sql \
	     truncateTempMappingTable_PG.sql \
	     commit_PG.sql \
	     begin_PG.sql \
	     populateTables_PG.sql \
	     commit_PG.sql \
	    );
 
set regenPhenotypeMartScripts=( begin_PG.sql \
	     refreshPhenotypeMart_PG.sql \
	     commit_PG.sql \
	     );

touch $FULL_SCRIPT_FILE
touch $CONVERT_PHENOTYPEMART_FILE
touch $ALL_PHENOTYPEMART_SCRIPTS

foreach name ($phenotypeMartScripts)
   cat $PHENOTYPEMARTDIR/$name >> $FULL_SCRIPT_FILE
end

foreach name ($regenPhenotypeMartScripts)
   #echo $FISHMARTDIR$name
   cat $PHENOTYPEMARTDIR/$name >> $CONVERT_PHENOTYPEMART_FILE
end

if ("X$1" == "X") then
echo "ready to start dropTables_PG.sql DBNAME from environment." ;
${PGBINDIR}/psql $DBNAME < $FULL_SCRIPT_FILE
else
echo "ready to start dropTables_PG.sql DBNAME provided from script call." ;
${PGBINDIR}/psql $1 < $FULL_SCRIPT_FILE



endif 

exit 0;

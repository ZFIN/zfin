#! /bin/tcsh -e

#$1 db name
set PHENOTYPEMARTDIR=<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/phenotypeMartPostgres 
set FULL_SCRIPT_FILE=$PHENOTYPEMARTDIR/phenotypeMartAutomated.sql 
set CONVERT_PHENOTYPEMART_FILE=$PHENOTYPEMARTDIR/phenotypeMartRegen.sql
set ALL_PHENOTYPEMART_SCRIPTS=$PHENOTYPEMARTDIR/allPhenotypeMart.sql

/bin/rm -rf $FULL_SCRIPT_FILE 
/bin/rm -rf $CONVERT_PHENOTYPEMART_FILE 
/bin/rm -rf $ALL_PHENOTYPEMART_SCRIPTS

set phenotypeMartScripts=( begin.sql \
	     truncatePGTable.sql \
	     commit.sql \
	     begin.sql \
	     truncatePGSTable.sql \
	     commit.sql \
	     begin.sql \
	     truncateTempMappingTable.sql \
	     commit.sql \
	     begin.sql \
	     populateTables.sql \
	     commit.sql \
	    );
 
set regenPhenotypeMartScripts=( begin.sql \
	     refreshPhenotypeMart.sql \
	     commit.sql \
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
echo "ready to start dropTables.sql DBNAME from environment." ;
${PGBINDIR}/psql $DBNAME < $FULL_SCRIPT_FILE
else
echo "ready to start dropTables.sql DBNAME provided from script call." ;
${PGBINDIR}/psql $1 < $FULL_SCRIPT_FILE



endif 

exit 0;

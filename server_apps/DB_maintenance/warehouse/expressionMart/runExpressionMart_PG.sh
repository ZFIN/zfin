#! /bin/tcsh -e

#$1 db name
set EXPRESSIONMARTDIR=<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/expressionMart
set FULL_SCRIPT_FILE=$EXPRESSIONMARTDIR/expressionMartAutomated_PG.sql 
set CONVERT_EXPRESSIONMART_FILE=$EXPRESSIONMARTDIR/expressionMartRegen_PG.sql
set ALL_EXPRESSIONMART_SCRIPTS=$EXPRESSIONMARTDIR/allExpressionMart_PG.sql

/bin/rm -rf $FULL_SCRIPT_FILE 
/bin/rm -rf $CONVERT_EXPRESSIONMART_FILE 
/bin/rm -rf $ALL_EXPRESSIONMART_SCRIPTS

set expressionMartScripts=( begin_PG.sql \
	     dropTables_PG.sql \
	     commit_PG.sql \
	     begin_PG.sql \
	     populateTables_PG.sql \
	     commit_PG.sql \
	    );
 
set regenExpressionMartScripts=( begin_PG.sql \
	     refreshExpressionMart_PG.sql \
	     commit_PG.sql \
	     );

touch $FULL_SCRIPT_FILE
touch $CONVERT_EXPRESSIONMART_FILE
touch $ALL_EXPRESSIONMART_SCRIPTS

foreach name ($expressionMartScripts)
   #echo $EXPRESSIONMARTDIR$name
   cat $EXPRESSIONMARTDIR/$name >> $FULL_SCRIPT_FILE
end

foreach name ($regenExpressionMartScripts)
   #echo $FISHMARTDIR$name
   cat $EXPRESSIONMARTDIR/$name >> $CONVERT_EXPRESSIONMART_FILE
end

if ("X$1" == "X") then
echo "ready to start dropTables_PG.sql DBNAME from environment." ;
${PGBINDIR}/psql $DBNAME < $FULL_SCRIPT_FILE
else
echo "ready to start dropTables_PG.sql DBNAME provided from script call." ;
${PGBINDIR}/psql $1 < $FULL_SCRIPT_FILE



endif 

exit 0;

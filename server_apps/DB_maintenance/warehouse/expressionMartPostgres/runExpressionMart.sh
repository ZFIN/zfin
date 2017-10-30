#! /bin/tcsh -e

#$1 db name
set EXPRESSIONMARTDIR=<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/expressionMartPostgres 
set FULL_SCRIPT_FILE=$EXPRESSIONMARTDIR/expressionMartAutomated.sql 
set CONVERT_EXPRESSIONMART_FILE=$EXPRESSIONMARTDIR/expressionMartRegen.sql
set ALL_EXPRESSIONMART_SCRIPTS=$EXPRESSIONMARTDIR/allExpressionMart.sql

/bin/rm -rf $FULL_SCRIPT_FILE 
/bin/rm -rf $CONVERT_EXPRESSIONMART_FILE 
/bin/rm -rf $ALL_EXPRESSIONMART_SCRIPTS

set expressionMartScripts=( begin.sql \
	     dropTables.sql \
	     commit.sql \
	     begin.sql \
	     populateTables.sql \
	     commit.sql \
	    );
 
set regenExpressionMartScripts=( begin.sql \
	     refreshExpressionMart.sql \
	     commit.sql \
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
echo "ready to start dropTables.sql DBNAME from environment." ;
${PGBINDIR}/psql $DBNAME < $FULL_SCRIPT_FILE
else
echo "ready to start dropTables.sql DBNAME provided from script call." ;
${PGBINDIR}/psql $1 < $FULL_SCRIPT_FILE



endif 

exit 0;

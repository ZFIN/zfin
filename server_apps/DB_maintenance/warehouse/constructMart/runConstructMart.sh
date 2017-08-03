#! /bin/tcsh -e

#$1 db name
setenv INFORMIXSERVER <!--|INFORMIX_SERVER|-->
setenv INFROMIXDIR <!--|INFORMIX_DIR|-->
set CONSTRUCTMARTDIR=<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/constructMart 
set FULL_SCRIPT_FILE=$CONSTRUCTMARTDIR/constructMartAutomated.sql 
set CONVERT_CONSTRUCTMART_FILE=$CONSTRUCTMARTDIR/constructMartRegen.sql
set ALL_CONSTRUCTMART_SCRIPTS=$CONSTRUCTMARTDIR/allConstructMart.sql

/bin/rm -rf $FULL_SCRIPT_FILE 
/bin/rm -rf $CONVERT_CONSTRUCTMART_FILE 
/bin/rm -rf $ALL_CONSTRUCTMART_SCRIPTS

set constructMartScripts=( begin.sql \
	     dropTables.sql \
	     commit.sql \
	     begin.sql \
	     populateTables.sql \
	     commit.sql \
	     begin.sql \
	     createTempBtsIndexes.sql \
	     commit.sql \
	    );
 
set regenConstructMartScripts=( begin.sql \
	     refreshConstructMart.sql \
	     commit.sql \
	     begin.sql \
	     createBtsIndexes.sql \
	     commit.sql \
	     );

touch $FULL_SCRIPT_FILE
touch $CONVERT_CONSTRUCTMART_FILE
touch $ALL_CONSTRUCTMART_SCRIPTS

foreach name ($constructMartScripts)
   #echo $CONSTRUCTMARTDIR$name
   cat $CONSTRUCTMARTDIR/$name >> $FULL_SCRIPT_FILE
end

foreach name ($regenConstructMartScripts)
   #echo $FISHMARTDIR$name
   cat $CONSTRUCTMARTDIR/$name >> $CONVERT_CONSTRUCTMART_FILE
end

if ("X$1" == "X") then
echo "ready to start dropTables.sql DBNAME from environment." ;
/private/apps/Informix/informix/bin/dbaccess -a $DBNAME $FULL_SCRIPT_FILE
else
echo "ready to start dropTables.sql DBNAME provided from script call." ;
/private/apps/Informix/informix/bin/dbaccess -a $1 $FULL_SCRIPT_FILE

endif 

exit 0;

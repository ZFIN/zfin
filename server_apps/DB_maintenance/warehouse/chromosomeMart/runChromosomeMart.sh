#! /bin/tcsh -e

#$1 db name
setenv INFORMIXSERVER <!--|INFORMIX_SERVER|-->
setenv INFROMIXDIR <!--|INFORMIX_DIR|-->
set CHROMOSOMEMARTDIR=<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/chromosomeMart/ 
set FULL_SCRIPT_FILE=$CHROMOSOMEMARTDIR/chromosomeMartAutomated.sql 
set CONVERT_CHROMOSOMEMART_FILE=$CHROMOSOMEMARTDIR/chromosomeMartRegen.sql
set ALL_CHROMOSOMEMART_SCRIPTS=$CHROMOSOMEMARTDIR/allChromosomeMart.sql

/bin/rm -rf $FULL_SCRIPT_FILE 
/bin/rm -rf $CONVERT_CHROMOSOMEMART_FILE 
/bin/rm -rf $ALL_CHROMOSOMEMART_SCRIPTS

set chromosomeMartScripts=(begin.sql \
	     schemaTables.sql\
	     commit.sql \
	     begin.sql \
	     populateTables.sql \
	     updateUniqueLocationTable.sql \
	     commit.sql \
	    );
 
set regenChromosomeMartScripts=( begin.sql \
	     refreshChromosomeMart.sql \
	     commit.sql \
	     );

touch $FULL_SCRIPT_FILE
touch $CONVERT_CHROMOSOMEMART_FILE
touch $ALL_CHROMOSOMEMART_SCRIPTS

foreach name ($chromosomeMartScripts)
   echo $CHROMOSOMEMARTDIR$name
   cat $CHROMOSOMEMARTDIR/$name >> $FULL_SCRIPT_FILE
end

cat <!--|ROOT_PATH|-->/server_apps/data_transfer/Ensembl/updateSequenceFeatureChromosomeLocation.sql >> $FULL_SCRIPT_FILE

foreach name ($regenChromosomeMartScripts)
   echo $CHROMOSOMEMARTDIR$name
   cat $CHROMOSOMEMARTDIR/$name >> $CONVERT_CHROMOSOMEMART_FILE
end

if ("X$1" == "X") then
echo "ready to start dropTables.sql DBNAME from environment." ;
${INFORMIXDIR}/bin/dbaccess $DBNAME $FULL_SCRIPT_FILE
else
echo "ready to start dropTables.sql DBNAME provided from script call." ;
${INFORMIXDIR}/bin/dbaccess $1 $FULL_SCRIPT_FILE

endif 

exit 0;

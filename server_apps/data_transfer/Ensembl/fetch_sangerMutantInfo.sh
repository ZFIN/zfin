#!/bin/tcsh

/bin/rm -rf <!--|TARGETROOT|-->/server_apps/data_transfer/Ensembl/sangerMutantData*.unl;

# find the name of the most current core database
/local/bin/curl -slo cur_ens_db.txt ftp://ftp.ensembl.org/pub/current_mysql/

# strip/convert non-unix line endings
/private/ZfinLinks/Commons/bin/reline cur_ens_db.txt

# pick the most recent release
set cur=`/bin/sed -n 's/^\(danio_rerio_core_.*\)/\1/gp' < cur_ens_db.txt`

# what is being used as the most current release
echo "Using Ensembl release: $cur"

echo " fetch_sangerMutantInfo.sql vs ensembldb.ensembl.org"

/bin/cat fetch_sangerMutantInfo.mysql | \
/local/bin/mysql -A -P5306 -u anonymous -h ensembldb.ensembl.org -si -D $cur >!  sangerMutantData.unl;
/bin/expand -t 2 sangerMutantData.unl > sangerMutantData1.unl
/bin/sed 's/  /|/g' sangerMutantData1.unl > sangerMutantData2.unl
/bin/sed 's/$/|/' sangerMutantData2.unl > sangerMutantData3.unl

# load the file from Ensembl mysql into the local database
# rollback if not called with the (first) argument "commit"


echo "*** loading SangerMutantData into <!--|DB_NAME|--> ***"
    ${PGBINDIR}/psql <!--|DB_NAME|--> < <!--|TARGETROOT|-->/server_apps/data_transfer/Ensembl/loadSangerMutantData.sql
    # Log what is being used as the most current release
    if (! -f fetch_sangerMutantData.log) then
	    touch fetch_sangerMutantData.log
    endif  
    

/bin/rm -rf <!--|TARGETROOT|-->/server_apps/data_transfer/Ensembl/ensdarPMapping*.unl;

/bin/cat fetch_ensdarpInfo.mysql | \
/local/bin/mysql -A -P5306 -u anonymous -h ensembldb.ensembl.org -si -D $cur >!  ensdarPMapping.unl;
/bin/expand -t 2 ensdarPMapping.unl > ensdarPMapping1.unl
/bin/sed 's/  /|/g' ensdarPMapping1.unl > ensdarPMapping2.unl
/bin/sed 's/$/|/' ensdarPMapping2.unl > ensdarPMapping3.unl

# load the file from Ensembl mysql into the local database
# rollback if not called with the (first) argument "commit"


echo "*** loading SangerMutantData into <!--|DB_NAME|--> ***"
     ${PGBINDIR}/psql <!--|DB_NAME|--> < <!--|TARGETROOT|-->/server_apps/data_transfer/Ensembl/loadEnsdarPMapping.sql
    # Log what is being used as the most current release
    if (! -f fetch_ensdarPMapping.log) then
	    touch fetch_ensdarPMapping.log
    endif      
echo "Using Ensembl release: $cur   `date`" >> fetch_ensdarPMapping.log



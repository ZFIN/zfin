#!/bin/tcsh


# find the name of the most current core database
/local/bin/curl -slo cur_ens_db.txt ftp://ftp.ensembl.org/pub/current_mysql/

# strip/convert non-unix line endings
/private/ZfinLinks/Commons/bin/reline cur_ens_db.txt

# pick the most recent release
set cur=`/bin/sed -n 's/^\(danio_rerio_core_.*\)/\1/gp' < cur_ens_db.txt`

# what is being used as the most current release
echo "Using Ensembl release: $cur"

# send a query to the current database returning 1:1 ensdargs and zdbids
# return in informix's load file format.
echo " fetch_ensdargOttdarg.sql vs ensembldb.ensembl.org"

/bin/cat fetch_ensdargOttdargTable_PG.mysql | \
/local/bin/mysql -A -P5306 -u anonymous -h ensembldb.ensembl.org -si -D $cur >!  ensdargOttdarg.unl;

/bin/expand -t 2 ensdargOttdarg.unl > ensdargOttdarg1.unl
/bin/sed 's/  /|/g' ensdargOttdarg1.unl > ensdargOttdarg2.unl
/bin/sed 's/$/|/' ensdargOttdarg2.unl > ensdargOttdarg3.unl

# load the file from Ensembl mysql into the local database
# rollback if not called with the (first) argument "commit"


echo "*** loading ensdargOttdargs into <!--|DB_NAME|--> ***"
    ${PGBINDIR}/psql <!--|DB_NAME|--> < <!--|TARGETROOT|-->/server_apps/data_transfer/Ensembl/loadEnsdargOttdarg_PG.sql
    # Log what is being used as the most current release
    if (! -f fetch_ensdargOttdargTable.log) then
	    touch fetch_ensdargOttdargTable.log
    endif      
echo "Using Ensembl release: $cur   `date`" >> fetch_ensdargOttdargTable.log

/bin/rm -rf <!--|TARGETROOT|-->/server_apps/data_transfer/Ensembl/ensdargOttdarg*.unl;


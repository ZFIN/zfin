#! /bin/tcsh


# find the name of the most current core database
/local/bin/curl -slo cur_ens_db.txt ftp://ftp.ensembl.org/pub/current_mysql/

# strip/convert non-unix line endings
/opt/zfin/bin/reline cur_ens_db.txt

# pick the most recent release
set cur=`/bin/sed -n 's/^\(danio_rerio_core_.*\)/\1/gp' < cur_ens_db.txt`

# what is being used as the most current release
echo "Using Ensembl release: $cur"

# send a query to the current database returning 1:1 ensdargs and zdbids

/opt/misc/groovy/bin/groovy fetchEnsdarg.groovy >! ensdarg.csv;

# load the file from Ensembl mysql into the local database
# rollback if not called with the (first) argument "commit"

echo "*** COMMITING load_ensdarg.sql into <!--|DB_NAME|--> ***"
cat load_ensdarG.sql commit.sql | ${PGBINDIR}/psql <!--|DB_NAME|-->
# Log what is being used as the most current release
if (! -f fetch_ensembl.log) then
	touch fetch_ensembl.log
endif
echo "Using Ensembl release: $cur   `date`" >> fetch_ensembl.log
cat updateMarkerChromosomeLocation.sql commit.sql | ${PGBINDIR}/psql <!--|DB_NAME|-->
echo "Updated marker_chromosome_location" >> fetch_ensembl.log

endif



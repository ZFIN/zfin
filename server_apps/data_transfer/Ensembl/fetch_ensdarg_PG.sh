#! /bin/tcsh


# find the name of the most current core database
/local/bin/curl -slo cur_ens_db.txt ftp://ftp.ensembl.org/pub/current_mysql/

# strip/convert non-unix line endings
/private/ZfinLinks/Commons/bin/reline cur_ens_db.txt

# pick the most recent release
set cur=`/bin/sed -n 's/^\(danio_rerio_core_.*\)/\1/gp' < cur_ens_db.txt`

# what is being used as the most current release
echo "Using Ensembl release: $cur"

# send a query to the current database returning 1:1 ensdargs and zdbids


/bin/cat fetch_ensdarg_PG.sql | \
/local/bin/mysql -A -P5306 -u anonymous -h ensembldb.ensembl.org -si -D $cur |\
/bin/sed 's/\(ZDB-GENE-[0-9\-]*\).*\(ENSDARG[0-9]*\).*/\1|\2|/g' |\
/usr/bin/tr '\011' \| >!  ensdarg.unl;

# load the file from Ensembl mysql into the local database
# rollback if not called with the (first) argument "commit"

if ($1 == "commit") then
	echo "*** COMMITING load_ensdarg.sql into <!--|DB_NAME|--> ***"
	cat load_ensdarg_PG.sql commit_PG.sql | ${PGBINDIR}/psql <!--|DB_NAME|-->
	# Log what is being used as the most current release
	if (! -f fetch_ensembl.log) then
		touch fetch_ensembl.log
	endif
	echo "Using Ensembl release: $cur   `date`" >> fetch_ensembl.log
	cat updateMarkerChromosomeLocation_PG.sql commit.sql | ${PGBINDIR}/psql <!--|DB_NAME|-->
	echo "Updated marker_chromosome_location" >> fetch_ensembl.log

else
	echo ""
	echo "*** Just Testing load_ensdarg.sql into <!--|DB_NAME|--> .***  "
	echo "To load use:  gmake run_commit"
	echo ""
	cat load_ensdarg_PG.sql rollback_PG.sql | ${PGBINDIR}/psql <!--|DB_NAME|-->
	cat updateMarkerChromosomeLocation_PG.sql rollback.sql | ${PGBINDIR}/psql <!--|DB_NAME|-->
	echo "Updated marker_chromosome_location" >> fetch_ensembl.log

endif



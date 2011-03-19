#! /bin/tcsh


# find the name of the most current core database
/local/bin/curl -slo cur_ens_db.txt ftp://ftp.ensembl.org/pub/current_mysql/

# strip/convert non-unix line endings
/private/ZfinLinks/Commons/bin/reline cur_ens_db.txt

# pick the most recent release
set cur=`/usr/bin/sed -n 's/^\(danio_rerio_core_.*\)/\1/gp' < cur_ens_db.txt`

# what is being used as the most current release
echo "Using Ensembl release: $cur"

# send a query to the current database returning 1:1 ensdargs and zdbids
# return in informix's load file format.
echo " fetch_ensdarg.sql vs ensembldb.ensembl.org"

/usr/bin/cat fetch_ensdarg.sql | \
/local/bin/mysql -A -P5306 -u anonymous -h ensembldb.ensembl.org -si -D $cur |\
/usr/bin/sed 's/\(ZDB-GENE-[0-9\-]*\).*\(ENSDARG[0-9]*\).*/\1|\2|/g' |\
/usr/bin/tr '\011' \| >!  ensdarg.unl;

# load the file from Ensembl mysql into the local database
# rollback if not called with the (first) argument "commit"

if ($1 == "commit") then
	echo "*** COMMITING load_ensdarg.sql into <!--|DB_NAME|--> ***"
	cat load_ensdarg.sql commit.sql | <!--|INFORMIX_DIR|-->/bin/dbaccess <!--|DB_NAME|-->
	# Log what is being used as the most current release
	if (! -f fetch_ensembl.log) then
		touch fetch_ensembl.log
	endif
	echo "Using Ensembl release: $cur   `date`" >> fetch_ensembl.log
else
	echo ""
	echo "*** Just Testing load_ensdarg.sql into <!--|DB_NAME|--> .***  "
	echo "To load use:  gmake run_commit"
	echo ""
	cat load_ensdarg.sql rollback.sql | <!--|INFORMIX_DIR|-->/bin/dbaccess -a <!--|DB_NAME|-->
endif



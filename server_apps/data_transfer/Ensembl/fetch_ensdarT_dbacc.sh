#! /bin/tcsh

# find the name of the most current core database
/local/bin/curl -slo cur_ens_db.txt ftp://ftp.ensembl.org/pub/current_mysql/

# strip/convert non-unix line endings
/private/ZfinLinks/Commons/bin/reline cur_ens_db.txt

# pick the most recent release
set cur=`/usr/bin/sed -n 's/^\(danio_rerio_core_.*\)/\1/gp' < cur_ens_db.txt`

# what is being used as the most current release
echo "Using Ensembl release: $cur"

/usr/bin/cat fetch_ensdarT_dbacc.mysql | \
/local/bin/mysql -A -P5306 -u anonymous -h ensembldb.ensembl.org -si -D $cur |\
/usr/bin/nawk '{print $1 "|" $2 "|"}' >! ensdarT_dbacc.unl;


if ($1 == "commit") then
	echo "*** COMMITING load_ensdarT_dbacc.sql <!--|DB_NAME|--> ***"
	cat load_ensdarT_dbacc.sql commit.sql | <!--|INFORMIX_DIR|-->/bin/dbaccess -a <!--|DB_NAME|-->
	echo "Using Ensembl release: $cur   `date`" >> fetch_ensembl.log
else
	echo ""
	echo "*** Just Testing load_ensdarT_dbacc.sql into <!--|DB_NAME|--> .***  "
	echo "To load use:  gmake run_transcript_commit"
	echo ""
	cat load_ensdarT_dbacc.sql rollback.sql | <!--|INFORMIX_DIR|-->/bin/dbaccess -a <!--|DB_NAME|-->
endif

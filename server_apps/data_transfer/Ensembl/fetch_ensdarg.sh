#! /bin/tcsh

# find the name of the most current core database
/local/bin/curl -slo cur_ens_db.txt ftp://ftp.ensembl.org/pub/current_danio_rerio/data/mysql/

# strip/convert non-unix line endings
/private/ZfinLinks/Commons/bin/reline cur_ens_db.txt

# pick the most recent release
set cur=`/usr/bin/sed -n 's/^\(danio_rerio_core_.*\)/\1/gp' < cur_ens_db.txt`

# what is being used as the most current release
echo "Using Ensembl release: $cur"

# send a query to the current database returning 1:1 ensdargs and zdbids
# return in informix's load file format.
/usr/bin/cat fetch_ensdarg.sql | \
/local/bin/mysql -A -P3306 -u anonymous -h ensembldb.ensembl.org -si -D $cur |\
/usr/bin/sed 's/\(ZDB-GENE-[0-9\-]*\).*\(ENSDARG[0-9]*\).*/\1|\2|/g' |\
/usr/bin/tr '\011' \| >!  ensdarg.unl;

# load the file from Ensembl mysql into the database
<!--|INFORMIX_DIR|-->/bin/dbaccess <!--|DB_NAME|--> load_ensdarg.sql;




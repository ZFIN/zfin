#! /bin/tcsh

# where the first arg is like 'danio_rerio_core_19_2'
set database=$1;

/local/bin/mysql -P3306 -u anonymous -h -D$database ensembldb.ensembl.org -si < fetch_ensdarg.sql |\
sed 's/^\(ZDB-GENE-[0-9\-]*\).*\(ENSDARG[0-9]*\).*/\1|\2|/g' >!  ensdarg.unl;

dbaccess <!--|DB_NAME|--> load_ensdarg.sql;

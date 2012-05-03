#! /bin/bash

# tries to return the current ensembl genes which
# _They_ have mapped 1:1 with ZFIN zdbids

rm -f ensdarG.unl ensdarT_dbacc.unl

# find the name of the most current core database
/local/bin/curl -slo cur_ens_db.txt ftp://ftp.ensembl.org/pub/current_mysql/

# strip/convert non-unix line endings
/private/ZfinLinks/Commons/bin/reline cur_ens_db.txt

# pick the most recent release
export cur="`/usr/bin/sed -n 's/^\(danio_rerio_core_.*\)/\1/gp' < cur_ens_db.txt`"

# what is being used as the most current release
echo ""
echo "Using Ensembl release: $cur"
echo ""
# send a query to the current database returning 1:1 ensdargs and zdbids
# return in informix's load file format.

echo "running fetch_ensdarg.sql vs ensembldb.ensembl.org  ->  ensdarG.unl"

/usr/bin/cat fetch_ensdarG.mysql | \
 /local/bin/mysql -A -P5306 -u anonymous -h ensembldb.ensembl.org -si -D $cur|\
 /usr/bin/sed 's/\(ZDB-GENE-[0-9\-]*\).*\(ENSDARG[0-9]*\).*/\1|\2|/g'|\
 /usr/bin/tr '\011' \| >  ensdarG.unl;

# send a query to the current database returning ensdarTs associated with ottdarTs
# return in informix's load file format.

echo "running fetch_ensdarT_dbacc.mysql vs ensembldb.ensembl.org  -> ensdarT_dbacc.unl"
/usr/bin/cat fetch_ensdarT_dbacc.mysql | \
/local/bin/mysql -A -P5306 -u anonymous -h ensembldb.ensembl.org -si -D $cur |\
/usr/bin/nawk '{print $1 "|" $2 "|"}' > ensdarT_dbacc.unl;



#! /usr/bin/sh

# the Makefile has 'run' and 'force' targets
# the force target removes the any downloaded files
# forcing them to be refreshed.

# who and where are we?
echo ""
echo "`pwd`/$0"
echo ""

### the data files to fetch *IF* they are updated
human="Homo_sapiens.gene_info.gz"
mouse="MRK_List1.rpt"
fly="dmel-all-gene_extended2000-r*.fasta.gz"

### For testing, or incase of problems: delete downloaded data files
### use commandline option '-f' to force fetching new files

while getopts f o
  do case "$o" in
    f)  	rm -f $human $mouse $fly;;
    [?])	print >&2 "Usage: $0 [-f {to force reloading}]"
  esac
done

### main goal is to update the chromosome and locations of orthologues in ZFIN
### a secondary opportunity is to locate/correct stale ortholog symbols & accessions
### and identify potential nomenclature suggestions for zebrafish genes
### (any ZFIN nomenclature changes to be conducted by curators)

# set environment variables for cron
INFORMIXSQLHOSTS="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->"
INFORMIXSERVER="<!--|INFORMIX_SERVER|-->"
INFORMIXDIR="<!--|INFORMIX_DIR|-->"
ONCONFIG="<!--|ONCONFIG_FILE|-->"
DBNAME="<!--|DB_NAME|-->"
# not all orders of paths will work alike
PATH="/private/bin:/private/apps/Informix/informix/bin:/private/ZfinLinks/Commons/bin:/local/bin:/usr/bin:/bin:."

export INFORMIXSERVER INFORMIXSQLHOSTS INFORMIXDIR ONCONFIG DBNAME PATH

echo "###############################################################"
regen="false"
# get a sense of the data loaded 'last time'
prior="`ls -l ./$mouse`"
echo ""
echo "Begin Mouse update `date +'%Y-%m-%d %H:%M:%S'`"
wget -q --timestamping "ftp://ftp.informatics.jax.org/pub/reports/$mouse"
geterr=$?
post="`ls -l ./$mouse`"

if [ ! -f $mouse -o  $geterr -eq 0 -a "$prior" != "$post" ] ; then
	echo "Mouse has a new datafile"
	echo $prior
	echo $post
	echo ""
	# Withdrawn rows
	cut -f 1-5,7 $mouse |grep "	Gene" |grep "	W" |cut -f1-4 |tr -d ' ' |sort -u > mus_chr_loc_sym_W.tab
	# Offical & Interm rows
	cut -f 1-5,7 $mouse |grep "	Gene" |grep  "	[OI]" |cut -f1-4 |tr -d ' ' |sort -u >  mus_chr_loc_sym.tab
	if [ $? -eq 0 ] ; then
		echo "$mouse is fetched and parsed into 'mus_chr_loc_sym.tab' `date +'%Y-%m-%d %H:%M:%S'`"
		echo "Running 'update_mus_ortho_loc.sql'"
		dbaccess -a $DBNAME update_mus_ortho_loc.sql
		regen="true"
	else
		echo "ERROR parsing \'$mouse\'  NOT running 'update_mus_ortho_loc.sql' `date +'%Y-%m-%d %H:%M:%S'`"
	fi
fi

echo "Finish Mouse update `date +'%Y-%m-%d %H:%M:%S'`"
echo ""
echo "###############################################################"
echo ""
###
### note: leave in tab separated format as 'location' uses pipes sometimes.
###
###
###

prior="`ls -l ./$human`"
echo "Begin Human update `date +'%Y-%m-%d %H:%M:%S'`"
wget -q --timestamping "ftp://ftp.ncbi.nih.gov/gene/DATA/GENE_INFO/Mammalia/$human"
geterr=$?
post="`ls -l ./$human`"

# use to distinguish gene omim from phenotype omin
# wget -q --timestamping "ftp://ftp.ncbi.nih.gov/gene/DATA/mim2gene_partial"
wget -q --timestamping "ftp://anonymous:tomc%40cs%2Euoregon%2Eedu@grcf.jhmi.edu/OMIM/mim2gene.txt"

if [ ! -f $human -o $geterr -eq 0 -a "$prior" != "$post" ] ; then
	echo "Human has a new datafile"
	echo $prior
	echo $post
	echo ""
	grep gene mim2gene.txt | cut -f1,3 | grep -v "-"> mim2gene.tab
	zcat ${human} | cut -f 2,3,6,7,8 | \
	nawk '{n=split($3,dbid,"[:|]"); i=1; omim=""; \
		while(i < n && "MIM" != dbid[i]){i+=2} omim=dbid[1+i]; \
		printf("%s\t%s\t%s\t%s\t%s\t\n",$1,$4,$5,$2,omim)}' |\
	sort -u > hum_chr_loc_sym_mim.tab
	if [ 0 -eq $? ]; then
		echo "$human fetched and parsed into 'hum_chr_loc_sym_mim.tab' `date +'%Y-%m-%d %H:%M:%S'`"
		echo "Running 'update_human_ortho_loc.sql'"
		dbaccess -a $DBNAME update_human_ortho_loc.sql
		regen="true"
	else
		echo "ERROR parsing \'$human\'  NOT running 'update_human_ortho_loc.sql' `date +'%Y-%m-%d %H:%M:%S'`"
	fi
fi
echo "Finish Human update `date +'%Y-%m-%d %H:%M:%S'`"
echo ""
echo "###############################################################"
echo ""
###
### ZFIN no longer even curates Fly (or Yeast) orthology the few that exist
### are all that likely will and we should not let these get many resources
### or interfer with higher value updates

### it seems odd to fetch a fasta file to get location updates
### but I could not find a better one that was not HUGE
### i.e. chado xml file or everything.gff files
### they also do not have a constantly named current file
### so we will have to delete older versions

echo "Begin FLY update `date +'%Y-%m-%d %H:%M:%S'`"
prior="`ls -lt ${fly} | head -1`"
wget -q --timestamping ftp://ftp.flybase.net/genomes/dmel/current/fasta/${fly}
geterr=$?
post="`ls -lt ${fly} | head -1`"

if [ ! -f ${fly} -o $geterr -eq 0 -a "$prior" != "$post" ] ; then
#if [ 1 ] ; then
	echo "Fly has a new datafile"
	echo "$prior"
	echo "$post"
	echo ""
	### if there are more than one $fly files then only keep the most recent
	find . -name ${fly} ! -name `ls -1t ${fly}|head -1` -exec rm -f {} \;
	zcat ${fly} |grep "^>" |cut -f3,4,5,6 -d ' ' |tr "=:," "   " |tr -d \; | \
	nawk '{eg="";for(i=7;i< NF;i+=2){if("EntrezGene"== $i){i++;eg=$i;i=NF}}print $2,$5,$7,eg,""}' | \
	sort -u > fly_chr_id_sym_eg.tab

	if [ 0 -eq $? ]; then
		echo "${fly} fetched and parsed into 'fly_chr_id_sym_eg.tab' `date +'%Y-%m-%d %H:%M:%S'`"
		echo "Running 'update_fly_ortho_loc.sql'"
		dbaccess -a $DBNAME update_fly_ortho_loc.sql
		regen="true"
	else
		echo "ERROR parsing ${fly} NOT running 'update_fly_ortho_loc.sql' `date +'%Y-%m-%d %H:%M:%S'`"
	fi
fi

echo "Finish fly update `date +'%Y-%m-%d %H:%M:%S'`"
echo ""
echo "###############################################################"

# note this regen generates alot of locks (~149,192)

if [ $regen != "false" ] ; then
	echo "running Regen ortho edvidence display, should be zero"
	echo 'execute function regen_oevdisp();' | dbaccess $DBNAME
fi
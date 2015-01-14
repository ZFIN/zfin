#! /usr/bin/sh

# the Makefile has 'run' and 'force' targets
# the force target removes the any downloaded files
# forcing them to be refreshed.

# who and where are we?
echo ""
echo "`pwd`/$0"
echo ""

echo "remove and re-create log directory"
rm -rf log
mkdir log

# run the following script to update orthologue names and make a list of zebrafish genes that may need to update the gene names
NCBIorthology.pl

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
echo ""
echo "Begin Mouse update `date +'%Y-%m-%d %H:%M:%S'`"

parseMGI.pl

echo "Finish Mouse update `date +'%Y-%m-%d %H:%M:%S'`"
echo ""
echo "###############################################################"
echo ""


echo "Begin Human update `date +'%Y-%m-%d %H:%M:%S'`"

# use to distinguish gene omim from phenotype omin
# wget -q --timestamping "ftp://ftp.ncbi.nih.gov/gene/DATA/mim2gene_partial"
rm -f mim2gene.tab
rm -f mim2gene.txt
wget -q --timestamping "ftp://ftp.omim.org/OMIM/mim2gene.txt "
grep gene mim2gene.txt | cut -f1,3 | grep -v "-"> mim2gene.tab

parseHumanData.pl

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
rm -f dmel-all-gene_extended2000-r*.fasta.gz
rm -f fly_chr_id_sym_eg.tab
wget -q --timestamping ftp://ftp.flybase.net/genomes/dmel/current/fasta/${fly}

	echo ""
	### if there are more than one $fly files then only keep the most recent
	find . -name ${fly} ! -name `ls -1t ${fly}|head -1` -exec rm -f {} \;
	zcat ${fly} |grep "^>" |cut -f3,4,5,6 -d ' ' |tr "=:," "   " |tr -d \; | \
	nawk '{eg="";for(i=7;i< NF;i+=2){if("EntrezGene"== $i){i++;eg=$i;i=NF}}print $2,$5,$7,eg,""}' | \
	sort -u > fly_chr_id_sym_eg.tab

	if [ 0 -eq $? ]; then
		echo "${fly} fetched and parsed into 'fly_chr_id_sym_eg.tab' `date +'%Y-%m-%d %H:%M:%S'`"
		echo "Running 'update_fly_ortho_loc.sql'"
		rm -f updateFlyOrthologyLog1
		rm -f updateFlyOrthologyLog2
		dbaccess -a $DBNAME update_fly_ortho_loc.sql >updateFlyOrthologyLog1 2> updateFlyOrthologyLog2
	else
		echo "ERROR parsing ${fly} NOT running 'update_fly_ortho_loc.sql' `date +'%Y-%m-%d %H:%M:%S'`"
	fi


echo "Finish fly update `date +'%Y-%m-%d %H:%M:%S'`"
echo ""
echo "###############################################################"

cp updateFly* log/.
cp updateHuman* log/.
cp updateMouse* log/.
cp orthNames* log/.
cp inconsistent* log/.
cp logOrthology* log/.
cp updateOrthology* log/.

emailOrthologyReports.pl

# note this regen generates a lot of locks (~149,192)

echo "running Regen ortho edvidence display, should be zero"
echo 'execute function regen_oevdisp();' | dbaccess -a $DBNAME



#! /usr/bin/bash

# fetch ensembl AGP

# Makefile has a run_agp target to fire this off
# keep assembly version up to date

V="9"                                         # <-- CHANGEME!
AGP="Zv${V}_chr.agp"

# where we put these source data files these days
gffdir="/research/zprodmore/gff3"

# where we put ZFIN gff3 tracks
dest="<!--|ROOT_PATH|-->/home/data_transfer/Downloads/"

# where we start from
wrkdir="`pwd`"

cd ${gffdir}

echo "retreiving Assembly $V chromosome Golden Path to: ${gffdir}" 
echo "!!!  Be sure it is the Assembly version we need !!!!"
cd ${gffdir}
prior="`ls -lt ${AGP}.gz | head -1`"
wget -q --timestamping "ftp://ftp.ensembl.org/pub/assembly/zebrafish/Zv${V}release/${AGP}.gz"
geterr=$?
post="`ls -lt ${AGP}.gz | head -1`"

if [ -f ${AGP}.gz -a ${geterr} -eq 0 ]; then
	# preserve the "timestamped" download to avoid re-fetching
	gunzip -c  ${AGP}.gz > ${AGP}
	chmod g+w ${AGP}*
fi
cd ${wrkdir}

echo `pwd`
echo ""
echo "get DNA clones that are in ZFIN into: zfin_DNA_clone.txt"

dbaccess -a $DBNAME unload_zfin_DNA_clone.sql

echo "convert agp to gff3 and augment with zfin clone data as: "
echo "${dest}/E_full_zfin_clone.gff3"

# agp_to_gff3.awk ${gffdir}/${AGP} | sort -k 3  -t '=' | clone_gff3_w_alias.awk > ${dest}/E_full_zfin_clone.gff3 
sort -k6 ${gffdir}/${AGP}  | agp_to_gff3.awk > ${dest}/E_full_zfin_clone.gff3 

echo "convert agp to gff3 and augment with zfin clone data as: "
echo "${dest}/E_trim_zfin_clone.gff3"
agp_to_gff3_trim.awk  ${gffdir}/${AGP} | sort -k 3  -t '=' | clone_gff3_w_alias.awk > ${dest}/E_trim_zfin_clone.gff3








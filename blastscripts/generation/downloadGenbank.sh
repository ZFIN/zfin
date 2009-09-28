#!/bin/bash

function loadGBdiv{
   echo "downloading $1" ;
   prefix=$1 ;
   fileset="gb$prefix*.seq.gz";

   # download the file to the appropriate directory
   # add -b to do download in the background
   # make non-verbose -q once working
   wget --verbose -r -l 2 -P $BLAST_FILES -A "$fileset" ftp://ftp.ncbi.nih.gov/genbank/  ;
   mkdir $BLAST_FILES/$prefix ;
   mv ftp.ncbi.nih.gov/genbank/$fileset $prefix

   mv $BLAST_FILES/ftp.ncbi.nih.gov/genbank/$fileset $BLAST_FILES/$prefix ;
   gunzip $BLAST_FILES/$prefix/$fileset ;
   rm -rf $BLAST_FILES/ftp.ncbi.nih.gov/genbank/$fileset ;

}

# gbk_zf_dna.fa  from /common/data/BLAST_files:
loadGBdiv est;
	#./GB/gss/gss_zf_dna.fa
loadGBdiv gss;
loadGBdiv htc;
	#./GB/htg/htg_zf_dna.fa
loadGBdiv htg;
loadGBdiv pat;
	#./GB/sts/sts_zf_dna.fa
loadGBdiv sts;
loadGBdiv pri;
loadGBdiv rod;
	#./GB/vrt/vrt_zf_dna.fa
loadGBdiv vrt;
	#./GB_daily/nc_zf_dna.fa




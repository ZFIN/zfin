#!/bin/tcsh
#
# This master script calls parseThisse.pl and  nameClone.pl first, 
# and generates a group of .unl file for loading. If there is unknown 
# accession number, it will blast the accessions against ZFIN sequence 
# database, use blast2tab.pl to convert the results to table format while 
# filter out matches with <90% identity. Then it calls filterBlast.pl 
# which exams the results and write high qualified match into is_gene.unl, 
# and the rest to blast2zfin.out for manual curation.
#

if ($#argv < 1) then 
    echo ""
    echo "Usage: thisseData2Unl.sh <dbname>"
    exit;
endif

setenv PATH    "/private/apps/wublast:"$PATH
setenv BLASTDB "/research/zblastdb/db/wu-db";

set dbname = $1

#=========== Parsing =================
echo "== parseing Thisse files ..."
./parseThisse.pl

if (-z parseThisse.err)  then # file of zero length
    /bin/rm -f parseThisse.err
else
    echo "Error detected during parsing, check parseThisse.err."
    exit;
    
endif
#=========== Duplication check =================
echo "== checking duplication ..."

$INFORMIXDIR/bin/dbaccess $dbname checkDups.sql >& checkDups.out
set rs = `grep retrieved checkDups.out`
if ( "$rs" != "") then
	echo "Duplication detected in checkDups.out"
	exit;
endif

#========= Naming Clone ================
if (-e acc_imClone.unl) then
    /bin/rm -f acc_imClone.unl
endif

echo "== naming clone, interact with zfin db and blast db  ..."
./nameClone.pl $dbname

if (-z nameClone.err)  then # file of zero length
    /bin/rm -f nameClone.err
else
    echo "Error detected during clone naming, check nameClone.err."
    exit;
    
endif
if (-e acc_imClone.unl) then
    echo "acc_imClone.unl file generated"
endif

#=========== BLAST (optional) ======================

if (-z acc4blast.txt) then 
    echo "== No BLAST efforts needed "
    /bin/rm -f acc4blast.txt
    echo ""
    echo "READY TO LOAD"  
    echo ""
else
    echo "== BLASTing and filter results ..."
    xdget -n -f -Tgb1 -e probe_fasta_retrieve.log gbk_zf_all acc4blast.txt > acc4blast.fa

    nice +10 /private/apps/wublast/blastn zfin_seq acc4blast.fa -e 1e-20  -o blast2zfin.out

    ./blast2tab.pl -p 90 -l 40 < blast2zfin.out | \
    ./filterBlast.pl $dbname 

    echo ""
    if (! -z is_gene.unl) then
	echo "auto gene assignments exist in is_gene.unl"
    endif 

    echo "please send blast2zfin.scnd to curator "
    echo ""

endif 

exit; 

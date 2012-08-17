#!/bin/tcsh
#
# FILE: thisseData2Unl.sh
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
    echo "Usage: thisseData2Unl.sh <dbname> [release_type]"
    exit;
endif

setenv PATH    "/private/apps/wublast:"$PATH
setenv BLASTDB "/research/zblastdb/db/wu-db";

set dbname = $1
if ($2) then
  set rtype = $2
else 
  set rtype = "fr"
endif

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
./nameClone.pl $dbname $rtype

if (-z nameClone.err)  then # file of zero length
    /bin/rm -f nameClone.err
else
    echo "Error detected during clone naming, check nameClone.err."
    exit;
    
endif
if (-e acc_imClone.unl) then
    echo "acc_imClone.unl file generated, error sign for ZGC package."
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
    
    xdget -n -f -Tgb1 -e probe_fasta_retrieve.log gbk_zf_mrna acc4blast.txt > acc4blastmrna.fa
    xdget -n -f -Tgb1 -e probe_fasta_retrieve.log gbk_zf_mrna acc4blast.txt > acc4blastrna.fa
    xdget -n -f -Tgb1 -e probe_fasta_retrieve.log gbk_zf_mrna acc4blast.txt > acc4blastdna.fa
    
    /bin/cat acc4blastmrna.fa acc4blastrna.fa acc4blastdna.fa > acc4blast.fa

    nice +10 /private/apps/wublast/blastn zfin_cdna_seq acc4blast.fa -e 1e-20  -o blast2zfin.out

    ./blast2tab.pl -p 90 -l 40 < blast2zfin.out | \
    ./filterBlast.pl $dbname 

    echo ""
    if (! -z is_gene.unl) then
        set is_gene_count = `/bin/wc -l is_gene.unl`;
	echo "Auto gene assignments: " $is_gene_count;
    endif 

    # count the number of queries in the blast table format result file
    set to_curate_count = `cut -d\| -f1 blast2zfin.scnd | uniq | /bin/wc`;
    @ to_curate_count -= 1 ;   #count off the title line

    echo "To be curated: " $to_curate_count " query sequences in blast2zfin.scnd";
    echo "Please send the above two numbers and blast2zfin.scnd and blast2zfin.third (if not empty) to curator, and wait for suppliment data to the is_gene.unl.";
    echo ""

endif 

exit; 

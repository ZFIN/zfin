#!/bin/tcsh
#
# FILE: talbotData2Unl.sh
# 
# It processes original data files into formated
# unload files, and run blast analysis on probes
# whose accession doesn't exist in zfin. BLAST 
# analysis would automatically assign some probes 
# to zfin genes, assign some to create new genes
# and those in-the-middle would be send to curators
# to look at.
#

if ($#argv < 1) then 
    echo ""
    echo "Usage: talbotData2Unl.sh <dbname>"
    exit;
endif

setenv PATH    "/private/apps/wublast:"$PATH
setenv BLASTDB "/research/zblastdb/db/wu-db";

set dbname = $1

#=========== Parsing  =================
echo "== parseing Talbot files, interact with zfin db ..."
./parseTalbot.pl $dbname

if (-z parseTalbot.err)  then # file of zero length
    /bin/rm -f parseTalbot.err
else
    echo "Error detected during parsing, check parseTalbot.err."
    exit;
    
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

    nice +10 /private/apps/wublast/blastn zfin_cdna acc4blast.fa -e 1e-20 -o blast2zfin.out 

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

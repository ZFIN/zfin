#!/bin/tcsh
#

if ($#argv < 1) then 
    echo ""
    echo "Usage: talbotData2Unl.sh <dbname>"
    exit;
endif

setenv PATH    "/private/apps/wublast/"$PATH
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

    nice +10 /private/apps/wublast/blastn zfin_seq acc4blast.fa -e 1e-20 | \
    ./blast2tab.pl -p 90 | \
    ./filterBlast.pl $dbname

    echo ""
    if (! -z is_gene.unl) then
	echo "auto gene assignments exist in is_gene.unl"
    endif 

    echo "please send blast2zfin.out file to curator "
    echo ""

endif 

exit; 

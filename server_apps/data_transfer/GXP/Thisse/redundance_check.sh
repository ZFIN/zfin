#!/bin/tcsh
#
# This is a redundance check script for Thisse ZGC data load. 
# No naming efforts would be needed.  
# 
# Input: 
#      dbname 
# Require: 
#      probes.unl 
#      gbk_zf_mrna,gbk_zf_rna, gbk_zf_dna
#      blast2tab.pl
#      filterBlast.pl
# Output:
#      is_gene.unl
#      blast2zfin.scnd
#      (blast2zfin.third)

if ($#argv < 1) then
	echo "Usage: redundance_check.sh <dbname> "
	exit
endif

setenv PATH    "/private/apps/wublast:"$PATH
setenv BLASTDB "/research/zblastdb/db/wu-db"

set dbname = $1

#=====================================
# Prepare and retrieve fasta seqence
#======================================
cut -d\| -f4 probes.unl > acc4blast.txt

xdget -n -f -Tgb1 -e probe_fasta_retrieve.log gbk_zf_mrna acc4blast.txt > acc4blastmrna.fa
xdget -n -f -Tgb1 -e probe_fasta_retrieve.log gbk_zf_rna acc4blast.txt > acc4blastrna.fa
xdget -n -f -Tgb1 -e probe_fasta_retrieve.log gbk_zf_dna acc4blast.txt > acc4blastdna.fa

/bin/cat acc4blastmrna.fa acc4blastrna.fa acc4blastdna.fa > acc4blast.fa
#=====================================
# Run Blast
#======================================
nice +10 /private/apps/wublast/blastn zfin_cdna_seq acc4blast.fa -e 1e-50 -o blast2zfin.out

#=====================================
# Run Blast Analysis
#======================================
./blast2tab.pl -p 90 -l 150 < blast2zfin.out | \
./filterBlast.pl $dbname

#=====================================
# Report results
#======================================
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


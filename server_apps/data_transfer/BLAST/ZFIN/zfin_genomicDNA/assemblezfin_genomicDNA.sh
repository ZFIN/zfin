#!/bin/bash
# do not use bash -e
## otherwise this script stops at the first xdget command as the log file consists accessions that
# are not found

# have to provide a path to find the refseq_zf_rna and gbk_zf* blastdbs.
source "../config.sh"

# get a fasta file from the GenBank db
log_message "Downloading GenBank accessions for ZFIN genomic DNA..."
xdget -n -f -e xdget_zfin_genomic_genbank_acc.log -o zfin_genomic_dna_all_mrna.fa -Tgb1 $BLAST_PATH/Current/gbk_zf_mrna zfin_genomic_genbank_acc.unl

xdget -n -f -e xdget_zfin_genomic_genbank_acc.log -o zfin_genomic_dna_all_rna.fa -Tgb1 $BLAST_PATH/Current/gbk_zf_rna zfin_genomic_genbank_acc.unl

xdget -n -f -e xdget_zfin_genomic_genbank_acc.log -o zfin_genomic_dna_all_dna.fa -Tgb1 $BLAST_PATH/Current/gbk_zf_dna zfin_genomic_genbank_acc.unl

log_message "Create combined fasta file"
/bin/cat zfin_genomic_dna_all_mrna.fa zfin_genomic_dna_all_rna.fa zfin_genomic_dna_all_dna.fa > zfin_genomic_dna_all.fa

echo "done making fasta files genomicDNA"

exit

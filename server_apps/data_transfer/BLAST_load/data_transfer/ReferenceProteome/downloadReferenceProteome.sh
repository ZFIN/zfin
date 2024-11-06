#!/bin/tcsh
#
# The script download ReferenceProteome zebrafish file
#
cd @TARGET_PATH@/ReferenceProteome

echo "== Remove old files =="

rm -rf all_refprot_aa.fa
rm -rf downloadedProt.gz
rm -rf downloadedAdditionalIsoforms.gz
rm -rf mapping.gz
rm -rf UP000000437_7955.fasta.gz
rm -rf UP000000437_7955_additional.fasta.gz
rm -rf UP000000437_7955.gene2acc.gz
rm -rf mapping
rm -rf downloadedProt
rm -rf downloadedAdditionalIsoforms

echo "== Download ReferenceProteome files =="

/local/bin/wget -qN "ftp://ftp.ebi.ac.uk/pub/databases/reference_proteomes/QfO/Eukaryota/UP000000437_7955.fasta.gz"
/local/bin/wget -qN "ftp://ftp.ebi.ac.uk/pub/databases/reference_proteomes/QfO/Eukaryota/UP000000437_7955_additional.fasta.gz"
/local/bin/wget -qN "ftp://ftp.ebi.ac.uk/pub/databases/reference_proteomes/QfO/Eukaryota/UP000000437_7955.gene2acc.gz"

/bin/cp UP000000437_7955.fasta.gz downloadedProt.gz
/bin/cp UP000000437_7955_additional.fasta.gz downloadedAdditionalIsoforms.gz
/bin/cp UP000000437_7955.gene2acc.gz mapping.gz

echo "== Unzip files for refseq == "
/local/bin/gunzip downloadedProt.gz
/local/bin/gunzip downloadedAdditionalIsoforms.gz
/local/bin/gunzip mapping.gz

/bin/cat downloadedProt downloadedAdditionalIsoforms > all_refprot_aa.fa
echo "== rename the fastafiles from refseq to more familiar names at ZFIN == "

exit

#! /bin/tcsh
# fetch_entrez.sh
# get human and mouse entrez-id related  nt & aa accessions, symbol and name
#output is 


#taxid|entrez_id|aa_acc|nt_acc|
wget ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/gene2accession.gz

#taxid|entrez_id|symbol|name|
wget ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/gene_info.gz

gunzip -f gene2accession.gz
gunzip -f gene_info.gz

#Homo sapiens (Human)	 [TaxID: 9606]
#Mus musculus (Mouse)	 [TaxID: 10090]
#
#
#FORMAT: gene2accession
#01	tax_id
#02	GeneID
#03	status
#04	RNA_nucleotide_accession.version
#05	RNA_nucleotide_gi
#06	protein_accession.version
#07	protein_gi
#08	genomic_nucleotide_accession.version
#09	genomic_nucleotide_gi
#10	start_position_on_the_genomic_accession
#11	end_position_on_the_genomic_accession
#12	orientation assembly

#FORMAT: gene_info
# tax_id:
# GeneID:
# Symbol:
# LocusTag:
# Synonyms:		(pipe-delimited
# dbXrefs:		(pipe-delimited
# chromosome:
# map location:
# description:
# type of gene:
# Symbol 		(nom-auth or '-')
# Full name		(nom-auth or '-')
# Nomenclature status:	(nom-auth or '-' or O official or I for interim)
# Other designations:	(pipe-delimited or  '-')
# Modification date:	(YYYYMMDD)



#
# for gene2accession
# isolate the Human and mouse records
# strip the ones without nucleotide or protein accessions
# trim the version number off the accessions
# put in pipe terminated feilds
#

egrep "^9606|^10090" gene2accession | cut -f 1,2,4,6, | grep -v "\-.\-" | \
sed 's/\([0-9]*\).\([0-9]*\).\([A-Z 0-9 _\-]*\)[\.0-9]*.\([A-Z 0-9 - _]*\).*/\1	\2	\3	\4/g' |\
tr -d \- | sort -n | sort -u | tr '	' \| | sed 's/\(.*\)/\1|/g' >! HM-txid_geneid_acc.unl

#
# for gene_info
# isolate the Human and mouse records
# strip the ones without symbol and name
# put in pipe terminated feilds
#

egrep "^9606|^10090"  gene_info | cut -f 1,2,11,12, | \
	grep -v "	\-	\-"  | \
 	tr '      ' \| | sed 's/\(.*\)/\1|/g' >! HM-txid_geneid_sym_name.unl 
# move to aught
# scp HM_txid_geneid_acc aught:/home/tomc/workspace/renomp/rebol/data/HM-txid_geneid_acc
# todo
# drop existing data from reno insert new data

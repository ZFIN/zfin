#! /bin/tcsh
# fetch_entrez.sh

# define pointless prompt
test ${?prompt} -eq 1 && set prompt = "cron %c2$Prompt"

setenv PATH "/usr/bin/:/local/bin/"
cd  <!--|ROOT_PATH|-->/server_apps/data_transfer/Load/Entrez/
if ($?) then
        echo "ERROR:  cant get to <!--|ROOT_PATH|-->/server_apps/data_transfer/Load/Entrez/"
        exit $status;
endif


# get human and mouse entrez-id related  nt & aa accessions, symbol and name
#output is

#taxid|entrez_id|aa_acc|nt_acc|
rm -f gene2accession.gz
wget -q ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/gene2accession.gz
set err = $?
if ($err) then
        echo "ERROR:  cant wget ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/gene2accession.gz"
        exit $err;
endif

cp gene2accession.gz gene2accession.gz.hold

#entrez_id|symbol|name|
#entrez_id|xref|

rm -f gene_info.gz
wget -q ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/gene_info.gz
set err = $?
if ($err) then
        echo "ERROR:  cant wget ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/gene_info.gz"
        exit $err
endif
cp gene_info.gz gene_info.gz.hold

# bail if gunzip fails
gunzip -f gene2accession.gz
set err = $?
if($err) then
	echo "ERROR gunzip gene2accession failed"
	rm -f gene2accession.gz
	exit $err
endif

gunzip -f gene_info.gz
set err = $?
if($err) then
        echo "ERROR gunzip gene_info failed"
        rm -f gene_info.gz
	exit $err
endif

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
sed 's/\([0-9]*\).\([0-9]*\).\([A-Z 0-9 _\-]*\)[\.0-9]*.\([A-Z 0-9 - _]*\).*/\1	\2	\4/g' |\
tr -d \- | sort -n | sort -u | tr '	' \| | grep -v '|$' | sed 's/\(.*\)/\1|/g' >! entrez_orth_prot.unl


# for gene_info
# isolate the Human and mouse records
# strip the ones without symbol and name
# keep only MIM: and MGI: dbXrefs
# put in pipe terminated feilds
#

egrep "^9606|^10090"  gene_info | cut -f 1,2,6,11,12 | \
    grep -v "	\-	\-"   >! HM_gene_info

# split the symbol file off from the dbxref(s)
cut -f 2,4,5 < HM_gene_info | sort -u | \
    tr '	' \| | sed 's/\(.*\)/\1|/g' >! entrez_orth_name.unl

cut -f 2,3 < HM_gene_info | grep "MIM:[0-9]*" | \
    sed 's/\([0-9]*\).*\(MIM:[0-9]*\).*/\1|\2|/g' | sort -u >! entrez_orth_xref.unl

cut -f 2,3 < HM_gene_info | grep "MGI:[0-9]*" | \
    sed 's/\([0-9]*\).*\(MGI:[0-9]*\).*/\1|\2|/g' | sort -u >> entrez_orth_xref.unl


#################################################################

source /research/zcentral/Commons/env/<!--|INFORMIX_SERVER|-->

setenv INFORMIX_SERVER "<!--|INFORMIX_SERVER|-->"
setenv   ONCONFIG_FILE "<!--|ONCONFIG_FILE|-->"
setenv   SQLHOSTS_FILE "<!--|SQLHOSTS_FILE|-->"
setenv   INFORMIX_PORT "<!--|INFORMIX_PORT|-->"
setenv    INFORMIX_DIR "<!--|INFORMIX_DIR|-->"

# double check the load files have content
<!--|INFORMIX_DIR|-->/bin/dbaccess <!--|DB_NAME|--> load_entrez_orth.sql

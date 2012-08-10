#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW SVN Project, EntrezGene automated data load directory
#
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#


# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/data_transfer/EntrezGene

GENERICS =  

STATICS = README.help

# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules


# ---------------  Misc Targets  ----------------------------------------

help: README.help
	@less $<

# 
#run : $(TARGETDIR)/ 
#	$(TARGETDIR)/


COMMONSBIN = ${TOP}/commons/bin

# GOAL is to maintain (ADDTIONAL) links from ZFIN to NCBI
# Types of links are GenBank, RefSeq, UniGene, GenPept.
# The basis of the link is NCBI's mapping of EntrezID to ZDBID
# which is found in "gene_info" at ftp://ftp.ncbi.nih.gov/gene/DATA/
# 
# (the file is "recalculated" daily and we pick it up weekly,
# so there is little advantage in timestamping to avoid downloading
# during normal usage but some while developing)

########################################################################

ENTREZGENE := ftp://ftp.ncbi.nih.gov/gene/DATA
############
### Entrez is kind enough to produce a zebrafish specific file
### 1.4M instead of 135M and avoids needing to parse out the
### txid 7955 set  -> 1M load file

Danio_rerio.gene_info.gz:  
	wget --timestamping $(ENTREZGENE)/GENE_INFO/Non-mammalian_vertebrates/Danio_rerio.gene_info.gz

# Mapping from Entrez ID to ZFIN ID

# TODO: handle their pipe seperated multiple LG in 7th col? (or not)

entrezid_zdbid_lg_type.unl: Danio_rerio.gene_info.gz
	#zgrep "ZDB-" Danio_rerio.gene_info.gz | cut -f2,6,7,10 |\
	zgrep "ZDB-" Danio_rerio.gene_info.gz | cut -f2,6,10 |\
	nawk 'BEGIN{FS="\t";OFS="|"}{split($$2,z,":");i=index(z[2],"|");\
	if(i){z[2]=substr(z[2],1,i-1)};$$2=z[2];print $$0"|"}' | sort -n > $@;\
	wc -l $@

# use a hash of the file to decide if further processing should happen?
# md5sum entrezid_zdbid_lg_type.unl > entrezid_zdbid_lg_type_new.md5

###############

# Mapping from  Entrez ID to GenBank accession (inc GenPept?)
# GenBank 325M download (compressed)  -> 2.5M load file

gene2accession.gz:  
	wget --timestamping $(ENTREZGENE)/gene2accession.gz

entrezid_GBnt_GBaa_GBdna.tab: gene2accession.gz entrezid_zdbid_lg_type.unl
	-zgrep "^7955" $< | cut -f 2,3,4,6,8 | \
	nawk 'BEGIN{FS="\t";OFS="\t"} $$2=="-" {\
		for(i=3;i<6;i++){j=index($$i,".");if(j){$$i=substr($$i,1,j-1)}}; \
		for(i=2;i<NF;i++){$$i=$$(i+1)};$$NF=NULL;print}' | \
	sort -u |sort -n > $@ ;\
	wc -l $@

# md5sum entrezid_GBnt_GBaa_GBdna.tab > entrezid_GBnt_GBaa_GBdna_new.md5
################

# Mapping from Entrez ID to UniGene accession
# UniGene file ~9M download (uncompressed)  -> 300k load file

gene2unigene:
	wget --timestamping $(ENTREZGENE)/$@
	
entrezid_ugc.tab:	gene2unigene
	grep "Dr." $< | sed 's/Dr.//g' | sort -n > $@;\
	wc -l $@
	
# md5sum entrezid_ugc.tab > entrezid_ugc_new.md5


# Mapping from RefSeq accession to Entrez ID
# may better belong in a RefSeq dir or nowhere if just a onetime patch
REFSEQCAT=ftp://ftp.ncbi.nih.gov/refseq/release/release-catalog

#there is a 21M (compressed) file that has the RefSeq/RefPept accessions
release%.accession2geneid.gz: 
	wget --timestamping $(REFSEQCAT)/release[0-9]*.accession2geneid.gz
## TODO If there is more than one keep the more recent

entrezid_refseq_refpept.tab: release%.accession2geneid.gz
	zgrep "^7955" release*.accession2geneid.gz |tr \. '	'|cut -f2,3,5|sort -n > $@

# entrezid_RSnt_RSaa_RSdna.tab: 	entrezid_zdbid_lg_type.tab \
#								enterzid_refseq_refpept.tab
#	join -j 1 $+ > $@;\
#	@ wc -l $@

#alternative RefSeq source (which is downloaded anyway)
#zgrep "^7955" gene2accession.gz|cut -f 2,3,4,6,8 | \
#	nawk 'BEGIN{FS="\t";OFS="\t"} $2!="-" {\
#		for(i=3;i<6;i++){j=index($i,"."); \
#		if(j){$i=substr($i,1,j-1)}}; \
#		for(i=2;i<NF;i++){$i=$(i+1)};$NF=NULL;print}' |\
#	sort -u |sort -n >! entrezid_RSnt_RSaa_RSdna.tab


# possible  RefSeq->UniProtKB mapping 
# ~8M rows maybe better used in UPKB dir
#wget ftp://ftp.ncbi.nih.gov/refseq/uniprotkb/gene_refseq_uniprotkb_collab.gz

########################################################################
# 
# Need the lengths of sequences other then GenBank nuclotide 

# RefSeq fasta lengths

REFSEQLEN := ftp://ftp.ncbi.nih.gov/refseq/D_rerio/mRNA_Prot

zebrafish.rna.fna.gz:
	wget --timestamping ${REFSEQLEN}/zebrafish.rna.fna.gz
	
zebrafish.protein.faa.gz:
	wget --timestamping  ${REFSEQLEN}/zebrafish.protein.faa.gz

refseq_len.unl: zebrafish.rna.fna.gz zebrafish.protein.faa.gz ${COMMONSBIN}/fasta_len.awk
	zcat zebrafish.*.f?a.gz | ${COMMONSBIN}/fasta_len.awk > $@

# GenPept fasta lengths
GENPEPTLEN = ftp://ftp.ncbi.nih.gov/genomes/D_rerio/protein/

# ... these are only RefPept acessions
protein.fa.gz: 
	wget --timestamping ${GENPEPTLEN}/protein.fa.gz

genpeptRP_len.unl: protein.fa.gz  ${COMMONSBIN}/fasta_len.awk
	zcat protein.fa.gz | ${COMMONSBIN}/fasta_len.awk > $@

# Gnomon_prot.fsa.gz  not GenPepts	
# too big ...
#	ftp://ftp.ncbi.nih.gov/ncbi-asn1/protein_fasta/
#   ftp://ftp.ncifcrf.gov/pub/genpept/

#
# So a valid question is should GenPept links be made for RefPept 
# acessions my thought is NO. they should be RefPept only
# GenPept is not its own DB but a byproduct of
# scraping other databases including RefPept and GenBank
# we already do have mechanism for links to RefReq proteins 
# so use them instead of secondhand GenPepts which are useful
# for translated Genbank records and a NCBI record of other (SPTR) dbs
# the question to be asked of the curators is:
# what value, beyond that already found on RefPept pages has GenPept?

########################################################################

fetch_entrez: entrezid_ugc.tab refseq_len.unl genpeptRP_len.unl\
		entrezid_GBnt_GBaa_GBdna.tab entrezid_refseq_refpept.tab 

EntrezGenPept_acc.unl: load_entrez.sql commit.sql fetch_entrez
	cat $< commit.sql | dbaccess -a $$DBNAME

GenPept.fasta: EntrezGenPept_acc.unl ${COMMONSBIN}/efetch.r
	${COMMONSBIN}/efetch.r -t fasta -i $<  -o $@

genpept_len.unl: GenPept.fasta ${COMMONSBIN}/fasta_len.awk
	${COMMONSBIN}/fasta_len.awk $< > $@

run: load_entrez.sql fetch_entrez  rollback.sql
	cat $< rollback.sql | dbaccess -a $$DBNAME
	@echo "NB: GenPept lenght update needs the entrez load commited" 

run_commit: update_GenPept_len.sql genpept_len.unl 
	cat load_entrez.sql commit.sql | dbaccess -a $$DBNAME ;\
	cat update_GenPept_len.sql commit.sql | dbaccess -a $$DBNAME ;\
	rm -f genpept_len.unl EntrezGenPept_acc.unl GenPept.fasta
	
clean_entrez:
	rm -f entrezid_zdbid_lg_type.unl entrezid_refseq_refpept.tab \
	entrezid_ugc.tab entrezid_GBnt_GBaa_GBdna.tab \
    genpept_len.unl EntrezGenPept_acc.unl GenPept.fasta
           
very_clean_entrez: clean_entrez
	rm -f gene2accession.gz release*.accession2geneid.gz gene2unigene \
	Danio_rerio.gene_info.gz


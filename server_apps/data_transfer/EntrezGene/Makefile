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

# TODO: handle their pipe seperated multiple LG in 7th col

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

### TODO If there is more than one keep the more recent
RefSeq-release%.catalog.gz: 
	wget --timestamping $(REFSEQCAT)/RefSeq-release[0-9]*.catalog.gz

refseq_len.unl: RefSeq-release[0-9]*.catalog.gz
	zcat RefSeq-release*.catalog.gz | \
	awk '/^7955/ {print substr($$4,1,index($$4,".")-1)"|"$$8"|"}'> $@;\
	wc -l $@

update_RS_len: update_refseq_pept_length.sql refseq_len.unl  
	dbaccess -a $$DBNAME $<

#there is a 21M (compressed) file that has the RefSeq/RefPept accessions
release%.accession2geneid.gz: 
	wget --timestamping $(REFSEQCAT)/release[0-9]*.accession2geneid.gz
## TODO If there is more than one keep the more recent

entrezid_refseq_refpept.tab: release%.accession2geneid.gz
	zgrep "^7955" release*.accession2geneid.gz |tr \. '	'|cut -f2,3,5|sort -n > $@

#entrezid_RSnt_RSaa_RSdna.tab: 	entrezid_zdbid_lg_type.tab \
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


fetch_entrez: entrezid_refseq_refpept.tab entrezid_ugc.tab \
				entrezid_GBnt_GBaa_GBdna.tab


run: load_entrez.sql rollback.sql fetch_entrez
	cat $< rollback.sql | dbaccess -a $$DBNAME


run_commit: load_entrez.sql rollback.sql fetch_entrez
	cat $< commit.sql | dbaccess -a $$DBNAME


clean:
	rm -f entrezid_zdbid_lg_type.unl entrezid_refseq_refpept.tab \
	entrezid_ugc.tab entrezid_GBnt_GBaa_GBdna.tab
           

very_clean: clean
	rm -f gene2accession.gz release*.accession2geneid.gz gene2unigene \
	Danio_rerio.gene_info.gz


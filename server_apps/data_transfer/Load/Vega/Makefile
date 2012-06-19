#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW SVN Project, Vega data load directory
#
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#

# ---------------  Variable Definitions  --------------------------------

TOP = ../../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/data_transfer/Load/Vega

SUBDIRS =

GENERICS =

STATICS =

# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them
include $(TOP)/make.default.rules


# ---------------  Misc Targets  ----------------------------------------

# Makefile to capture dependencies in a Vega load
# A primary source of information on gmake
# is available at your fingertips by typing
#
#           "man gmake"
#
# as mentioned in the man page,
# further information can be found in a Manual.
#
# http://www.gnu.org/software/make/manual/html_node/index.html
#
########################################################################

# nothing by default
all:

help:
	@cat README.help

#begin fetch novel_check withdrawn_check blast_transcripts load_transcripts

run: begin  novel_check withdrawn_check blast_transcripts load_transcripts

run_commit: begin  novel_check withdrawn_check blast_transcripts load_transcripts_commit



# fetch datafiles from Sanger depends on getting
# an email with the personal ftp directory of whomever's
# turn it is this time.

# So:
# gmake FTP="blablabla" DEST="blabla" PREV="blabla" fetch

FTP  = ftp.sanger.ac.uk/pub/ds23/vega47

VEGADIR = /research/zprod/data/VEGA

DEST = $(VEGADIR)/2012-06
PREV = $(VEGADIR)/2012-01
#PREV = $(VEGADIR)/2011-08
#DEST = "$(VEGA)/`date +%Y-%m`"

COMMON_BIN = $(TOP)/commons/bin

ZFIN_DOWNLOADS = http://zfin.org/data_transfer/Downloads

begin:
	@ if [ ! -d $(DEST) ] ;then mkdir $(DEST);chmod g+w $(DEST) ;fi

########################################################################
# fetch_vega target

# the typical file names we get, but it has jumped around alot
INCOMMING = $(DEST)/genes_for_tom.txt $(DEST)/evidence_for_tom.txt \
$(DEST)/transcripts_for_tom.fa $(DEST)/dumped_transcripts_for_tom.fa \
$(DEST)/clonelist_for_tom.txt $(DEST)/assembly_for_tom.txt

# get and  decompress a copy of the vega data files
# protect the originals
fetch: $(FTP%) $(DEST)
	wget -r -np --timestamping "ftp://$(FTP)" -P $(DEST)/

copy_orig: fetch
	-chmod a+w $(DEST)/*
	-cp $(DEST)/$(FTP)/* $(DEST)/
	-gunzip -f $(DEST)/*.gz
	-rm -f $(DEST)/*.gz

$(DEST)/transcripts_for_tom.fa: copy_orig
	-mv -f  $(DEST)/dumped_transcripts_for_tom.fa $@

protect_orig:   copy_orig
	-chmod -R a-w $(DEST)/$(FTP)

$(DEST)/assembly_for_tom.txt: copy_orig

fetch_vega: $(DEST)/transcripts_for_tom.fa protect_orig
	-echo "Vega fetched\n"

clean_fetch_vega:
	 $(DEST)/transcripts_for_tom.fa  $(DEST)/assembly_for_tom.fa


########################################################################
#                  Novel Check target

# it is easier to poke through the sequence metadata without the sequence
$(DEST)/transcripts_for_tom.defline: $(DEST)/transcripts_for_tom.fa
	@grep "^>" $? | sort -u > $@

# first rough hint of the size of the load
count_deflines: $(DEST)/transcripts_for_tom.defline
	@echo "\ntranscript counts"
	@wc -l $?
	@wc -l $(PREV)/transcripts_for_tom.defline
	@echo ""

# isolate the gene versions from this run
$(DEST)/ottdarGV.txt: $(DEST)/genes_for_tom.txt
	@cut -f2 $? | sort -u > $@

# join with gene versions from the previous Vega
$(DEST)/common_ottdarG_oldV_newV.txt: $(DEST)/ottdarGV.txt
	@join  $(PREV)/ottdarGV.txt $? > $@

# make sure the updates are rational i.e. none got younger (it has happened)

$(DEST)/ottdarGV_younger.txt: $(DEST)/common_ottdarG_oldV_newV.txt
	@nawk '{if($$3 > $$5){print sprintf("%s\t%s\t%s",$$1,$$3,$$5)}}' $? > $@

count_younger: $(DEST)/ottdarGV_younger.txt
	@echo "be zero:"
	@wc -l $?
	@echo ""

$(DEST)/ottdarGV_updated.txt: $(DEST)/common_ottdarG_oldV_newV.txt
	@nawk '{if($$3 < $$5){print sprintf("%s\t%s\t%s",$$1,$$3,$$5)}}' $? > $@

count_versions: $(DEST)/ottdarGV_updated.txt
	@echo "Find existing genes with new version numbers"
	@wc -l  $<
	@echo ""

# fine for the update/downdate check, but we need the novel as well.

$(DEST)/ottdarG_toBLAST.txt: $(DEST)/ottdarGV.txt $(PREV)/ottdarGV.txt
	@diff $(PREV)/ottdarGV.txt $(DEST)/ottdarGV.txt|grep "^>"|cut -c3-21 > $@

# check all the updates are caught
doublecheck_updates: $(DEST)/ottdarG_toBLAST.txt $(DEST)/ottdarGV_updated.txt
	@echo "Double check all updated genes are included"
	@join $+ | wc -l

count_genes: $(DEST)/ottdarG_toBLAST.txt
	@echo "ottdarG to blast"
	@wc -l $<

novel_check: count_deflines count_younger count_versions doublecheck_updates \
	count_genes
	@echo "novel_check finished"

clean_novel_check:
	rm -f $(DEST)/transcripts_for_tom.defline $(DEST)/ottdarGV.txt \
	$(DEST)/common_ottdarG_oldV_newV.txt $(DEST)/ottdarGV_younger.txt \
	$(DEST)/ottdarGV_updated.tx $(DEST)/ottdarG_toBLAST.txt

########################################################################
#             Withdrawn Check target

# what is withdrawn at this point?

$(DEST)/ottdarT.list: $(DEST)/transcripts_for_tom.defline
	@cut -f1 -d ' ' $< > $@

$(DEST)/withdrawn.ottdarT:  $(PREV)/ottdarT.list $(DEST)/ottdarT.list
	@comm -23 $+ | cut -c2- > $@
	@echo "Withdrawn ottdarT"
	@wc -l $@

# are any "withdrawn" transcripts on a gene (ottdarG) that has new transcripts
$(DEST)/withdrawn_ottdarTs.ottdarG: $(DEST)/withdrawn.ottdarT $(PREV)/ottdarT_ottdarG_ottdarP.unl
	@ggrep -f $+ | cut -f2 -d \| | sort -u > $@
	@echo "ottdarG with Withdrawn ottdarT also has new ottdarT"
	@ggrep -f $@  $(DEST)/ottdarGV_updated.txt
	@echo "Could the old  and new be merged?"

#how many transcripts do those ottdarGs have in  the new load?
withdrawn_check: $(DEST)/withdrawn_ottdarTs.ottdarG $(DEST)/transcripts_for_tom.defline
	@echo "novel transcripts on genes with freshly withdrawn transcripts"
	@for G in `cat $(DEST)/withdrawn_ottdarTs.ottdarG`;do grep $$G $(DEST)/transcripts_for_tom.defline|cut -f1 -d ' ';done
	@echo "withdrawn_check finished\n"

clean_withdrawn_check:
	rm -f $(DEST)/ottdarT.list $(DEST)/withdrawn.ottdarT \
	$(DEST)/withdrawn_ottdarTs.ottdarG

########################################################################
#              Blast Transcripts target

# want to augment the deflines we create with info from other files sent
$(DEST)/clnacc_lg.txt: $(DEST)/assembly_for_tom.txt
	@echo "\nPossibly Bogus Clone names"
	@grep '_' $?
	@nawk 'BEGIN{OFS="\t"}{print $$4,substr(1,8,$$5),$$1}' $< | sort -u > $@

# also want to create deflines which both allow retrivial via a number
# of keys and provide curators with useful information at a glance
#
# Need to partition the incomming transcripts into the set of ottdarT
# which need to be Blasted and run through Reno and the existing ones
# which are fine as they are( i.e. not updated)
#
# ZFIN Downloads file is:
# http://zfin.org/data_transfer/Downloads/vega_transcript.txt
# which is also the file we provide to Sanger to update their nomenclature

# pulling this datafile out of the rebol script to make the dependence explicit

$(DEST)/vega_transcript.txt:  $(DEST)
	wget -q --timestamping $(ZFIN_DOWNLOADS)/vega_transcript.txt -P $(DEST)/

# "subset of" is not a true depencency but useful construction none the less
$(DEST)/vega_reno.nt: $(DEST)/vega_transcript.nt
	@echo "How many new transcripts"
	@grep -c "^>" $@

clnacc_lg.txt: $(DEST)/clnacc_lg.txt
	cp -f $< $@

$(DEST)/vega_transcript.nt: $(DEST)/transcripts_for_tom.fa \
  $(DEST)/genes_for_tom.txt $(DEST)/vega_transcript.txt \
  clnacc_lg.txt parse-otter-fasta.r
	@echo ""
	@echo "This takes several minutes"
	@./parse-otter-fasta.r $+
	@chmod g+w *.nt *.unl
	@chgrp fishadmin *.nt *.unl
	@mv vega_reno.nt  $(DEST)/vega_reno.nt
	@mv vega_transcript.nt $@
	@mv ottdarT_ottdarG_ottdarP.unl $(DEST)/ottdarT_ottdarG_ottdarP.unl
	@mv vega_zfin.nt  $(DEST)/vega_zfin.nt
	@rm clnacc_lg.txt

# the previous target also produces:
# the current mapping of vega ottdar[TGP] identifiers
$(DEST)/ottdarT_ottdarG_ottdarP.unl: $(DEST)/vega_transcript.nt
# Partitions the load into novel and existing transcripts
$(DEST)/vega_zfin.nt:  $(DEST)/vega_transcript.nt
# vega_transcript.nt is the combination of the two which will become
# the PREVEGA blast database then the Vega_transcript blasdb.
# but first use it to isolate the sequence for existing genes which have
# been updated


# tricky because this needs to be done as informix, setuid could help
push_local_blast: $(DEST)/vega_transcript.nt
	@echo "\n*******************   AS INFORMIX   ************************\n"
	@echo "$(COMMON_BIN)/push_local_blastdb.sh $<"
	@echo "\n************************************************************\n"

clean_push_local_blast:
	rm -f $(DEST)/vega_transcript.nt

vega_transcript.xn%: $(DEST)/vega_transcript.nt
	/private/apps/wublast/xdformat -n -q3 -I -o vega_transcript $<

$(DEST)/vega_reno_gene.nt: vega_transcript.xn%  $(DEST)/ottdarG_toBLAST.txt
	/private/apps/wublast/xdget -n -fd vega_transcript $(DEST)/ottdarG_toBLAST.txt > $@
	@echo "\nHow many transcripts total, including updated genes"
	@grep -c "^>" $@

$(DEST)/vega_reno_gene.out:  $(DEST)/vega_reno_gene.nt
	$(COMMON_BIN)/blast_reno.sh $<

Vega_%_NEW_.ctx: $(DEST)/vega_reno_gene.out
	$(COMMON_BIN)/parse-blast-reno.r  $<   Vega_`date +%Y%m%d`_NEW_

blast_transcripts: Vega_%_NEW_.ctx  push_local_blast
	@echo "\nFeed Vega_%_NEW_.ctx to the reno pipeline"
	@echo "blast_transcripts finished\n"



clean_blast_transcripts:
	rm -f vega_transcript.xn* \
	$(DEST)/vega_reno_gene.nt $(DEST)/vega_reno_gene.out \
	$(DEST)/Vega_*_NEW_.ctx

########################################################################
#                 Load Transcripts target

# isolate novel transcripts

$(DEST)/ottdarT.lst: $(DEST)/transcripts_for_tom.defline
	@cut -c 1-19 $< | sort > $@

# prev should exist but just in case.
$(PREV)/ottdarT.lst: $(PREV)/transcripts_for_tom.defline
	@cut -c 1-19 $< | sort > $@

$(DEST)/novel_transcripts.lst:  $(PREV)/ottdarT.lst $(DEST)/ottdarT.lst
	@comm -13 $+  > $@
	@echo "Novel Transcript records to load "
	@wc -l $@
	@echo ""

$(DEST)/novel_transcripts_for_tom.defline: $(DEST)/novel_transcripts.lst
	join $+ > $@

# the novel are what is _minimaly_ required,
# but it is better to load them all so that
# types and lengths etc may be updated as well

novel_transcript.unl: $(DEST)/transcripts_for_tom.defline
	for_tom_2_unl.awk $<  > $@
	cp -f $@ $(DEST)/$@

$(DEST)/check_vega_type.plain: novel_transcript.unl
	cut -f 4,9 -d \| $< | sort -u > $@

# prev should exist but just in case.
$(PREV)/check_vega_type.plain: $(PREV)/novel_transcript.unl
	cut -f 4,9 -d \| $< | sort -u > $@

# vega_type_translation.unl should be in a dependenccy of some sort
#  it should be re-commited when it changes
#  until it is in the database instead of the transcript teams attempt

$(DEST)/novel_vega_type_to_add: $(PREV)/check_vega_type.plain $(DEST)/check_vega_type.plain
	@comm -13 $+  | nawk -F "|" '{print "transcript|"$$1 "|||";print "gene|"$$2"|||"}' | \
	sort -u | comm -23 - vega_type_translation.unl > $@
	# TODO: need to check that the first two column _alone_ do not match
	# may need to ask curators about zfin types & statuses to add to the 3rd & 4th columns
	# need to sort the appended line into its place the file
	@if [ -s $@ ] ; then cat $@ vega_type_translation.unl | sort -u > tmp_vtt.unl; mv tmp_vtt.unl  vega_type_translation.unl; rm -f tmp_vtt.unl ;fi
	@if [ -s $@ ] ; then echo "Rows added to 'vega_type_translation.unl'\n"; fi
	@if [ -s $@ ] ; then cat $@; fi
	@if [ -s $@ ] ; then echo "\n\tThe last 2 columns may need to be popupated"; fi
	@if [ -s $@ ] ; then echo "\twith the vega type|status IDs from ZFIN\n"; fi


# make the transcript length available for the load
ottdarT_length.unl:	$(DEST)/vega_transcript.nt
	grep "^>" $< | tr \| ' ' | awk '{print $$2 "|" $$(NF-1) q"|"}' > $@
	cp -f $@ $(DEST)/$@

# since we are (ultimatly) not just blasting novel transcripts,
# the non-novel transcripts that are blasted also need their db_link.fdbcont
# to be reset to PREVEGA. that list doesn't already exist so pull it
# from vega_reno_gene.nt

query_ottdarT.unl: $(DEST)/vega_reno_gene.nt
	grep "^>" $< |cut -f2 -d\| | $(COMMON_BIN)/tailpipe >  $@
	cp -f $@ $(DEST)/$@

vega_type_translation.unl: $(DEST)/novel_vega_type_to_add
	@echo "vega_type_translation.unl just needs to be up to date"

load_novel_transcript.log:
	@touch load_novel_transcript.log
	@cat /dev/null > load_novel_transcript.log

TS_LOAD_FILES = load_novel_transcript.sql  $(DEST)/novel_vega_type_to_add \
	vega_type_translation.unl ottdarT_length.unl novel_transcript.unl \
	query_ottdarT.unl load_novel_transcript.log

load_transcripts:  $(TS_LOAD_FILES) rollback.sql
	cat $< rollback.sql | dbaccess -a $$DBNAME
	@echo "load_transcript TESTED\n"

load_transcripts_commit: $(TS_LOAD_FILES)  commit.sql
	cat $< commit.sql | dbaccess -a $$DBNAME
#	|&  tee load_novel_transcript.log
	@echo "load_transcripts COMMITED\n"

clean_load_transcripts:
	rm -f $(DEST)/ottdarT.lst $(DEST)/novel_transcripts_for_tom.defline \
	 $(DEST)/check_vega_type.plain $(DEST)/query_ottdarT.unl \
	novel_vega_type_to_add ottdarT_length.unl novel_transcript.unl


########################################################################
########################################################################


ottdarT_ottdarG_ottdarP.unl: $(DEST)/ottdarT_ottdarG_ottdarP.unl
	@cp $< $@

sanity_check_post_reno: sanity_check_post_reno.sql ottdarT_ottdarG_ottdarP.unl
	dbaccess -a $$DBNAME $<
	@rm ottdarT_ottdarG_ottdarP.unl



########################################################################
#                       Load Assembly
# separate the clone acc from it's version # first
$(DEST)/assembly_for_tom.tab: $(DEST)/assembly_for_tom.txt
	tr '\.' '\11' < $<  > $@

assembly_for_tom.tab: $(DEST)/assembly_for_tom.tab
	gawk 'BEGIN{OFS="\t"}{sub(/_[A-Z_]*/,"",$$4);print}' $< > $@

$(DEST)/clonelist_for_tom.tab: $(DEST)/clonelist_for_tom.txt
	tr '\.' '\11' < $<  > $@

# removing the wrong underscore_suffix on clone names here
annotated_clones.unl: $(DEST)/clonelist_for_tom.tab
	cut -f 1-3 $< | nawk '{sub(/_[A-Z_]*/,"",$$1);print $$2"|"$$1"|"}' > $@

load_assembly:  load_assembly.sql assembly_for_tom.tab annotated_clones.unl
	cat $< rollback.sql | dbaccess -a $$DBNAME

load_assembly_commit:  load_assembly.sql assembly_for_tom.tab annotated_clones.unl
	cat $< commit.sql | dbaccess -a $$DBNAME
	@rm -f  annotated_clones.unl  assembly_for_tom.tab

clean_load_assembly:
	rm -f  annotated_clones.unl  assembly_for_tom.tab \
	$(DEST)/assembly_for_tom.tab $(DEST)/clonelist_for_tom.tab

########################################################################
#                   Withdraw Transcripts

BLASTFILES = /research/zblastfiles/files/LOCAL

existing_withdrawn.fa:  $(BLASTFILES)/vega_withdrawn.fa
	cp @< $@
	grep -c "^>" $@

fresh_withdrawn.fa: $(DEST)/withdrawn.ottdarT  $(PREV)/vega_transcript.nt
	$(COMMON_BIN)/fastagrep -f $+ > $@
	grep -c "^>" $@

all_withdrawn.fa: existing_withdrawn.fa fresh_withdrawn.fa
	cat $+ > $@
	grep -c "^>" $@

select_withdrawn.txt: select_withdrawn.sql
	dbaccess -a $$DBNAME $<

select_withdrawn.lst: select_withdrawn.txt
	cut -f1 $< > $@

vega_withdrawn.fa: select_withdrawn.lst all_withdrawn.fa
	$(COMMON_BIN)/fastagrep -f $+ > $@
	grep -c "^>" $@

withdraw_transcripts: vega_withdrawn.fa
	@echo "\n*******************   AS INFORMIX   ************************\n"
	@echo "$(COMMON_BIN)/push_local_blastdb.sh $<"
	@echo "\n************************************************************\n"

# should depend on public blast databases in position
withdrawn_dblink: update_withdrawn_dblink.sql rollback.sql
	cat $+ | dbaccess -a $$DBNAME

withdrawn_dblink_commit: update_withdrawn_dblink.sql commit.sql
	cat $+ | dbaccess -a $$DBNAME

clean_withdraw_transcripts:
	rm -f select_withdrawn.txt  all_withdrawn.fa \
		fresh_withdrawn.fa existing_withdrawn.fa select_withdrawn.lst

########################################################################
#              Update Supporting Sequence

count_evidence: $(DEST)/evidence_for_tom.txt
	@echo "all evidence"
	grep -c "	Em:" $<
	grep "	Em:" $<|cut -f 4|sort|uniq -c|sort -n

evidence.unl: count_evidence
	grep -v Protein $< |grep "^OTT"|cut -f1-3|sed 's/Em://g'| \
	awk '{print $$1 "|" substr($$3,1,8 ) "|"}'|sort -u > $@

load_evidence: load_supporting_sequence.sql  rollback.sql evidence.unl
	#dbaccess -a $DBNAME $< |& tee load_supporting_sequence.log
	cat $< rollback.sql | dbaccess -a $$DBNAME

load_evidence_commit: load_supporting_sequence.sql commit.sql evidence.unl
	#dbaccess -a $DBNAME $< |& tee load_supporting_sequence.log
	cat $< commit.sql | dbaccess -a $$DBNAME

clean_load_evidence:
	rm -f evidence.unl


########################################################################
#
# some time after the results of reno run is picked by Sanger they
# return files with new names for the transcripts which we load
#
# they also provide gff3 files
# this may be weeks later than the previous sections
########################################################################
#                 Post Reno Load

# the file names they make available and locations are subject to change
$(DEST)/vega_transcript_names.tsv.gz: $(FTP) $(DEST)
	#wget --timestamping "ftp://$(FTP)/vega_transcript_names.tsv.gz" -P $(DEST)
	#!!!	 they have made a correction the the name file
	#!!!	 left in a different place and so I am hardcoding it here.
	#!!! TODO: remove kludge uncomment
 #!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	wget --timestamping "ftp://ftp.sanger.ac.uk/pub/ds23/vega47/vega_transcript_names.tsv.gz" -P ${DEST}
 #!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

$(DEST)/vega_transcript_names.tsv: $(DEST)/vega_transcript_names.tsv.gz
	zcat $< > $@

vega_transcript_names.tsv: $(DEST)/vega_transcript_names.tsv
	cp $< ./$@

update_tscript_names: update_tscript_names.sql vega_transcript_names.tsv \
		rollback.sql
	cat  $< rollback.sql | dbaccess -a $$DBNAME

update_tscript_names_commit: update_tscript_names.sql vega_transcript_names.tsv \
		commit.sql
	cat  $< commit.sql | dbaccess -a $$DBNAME
	rm vega_transcript_names.tsv

clean_update_tscript_names:
	rm -fr $(DEST)/vega_transcript_names.tsv.gz

########################################################################
#                        GFF3


$(DEST)/zebrafish_VEGA*.gff3.gz: ${DEST}
	wget --timestamping "ftp://${FTP}/zebrafish_VEGA*.gff3.gz" -P ${DEST}

$(DEST)/zebrafish_VEGA.gff3: $(DEST)/zebrafish_VEGA*.gff3.gz
	zcat $< > $@

# add 'vega' as the missing source colunm (second column)
# and ommit the introns
$(DEST)/drerio_vega.gff3: $(DEST)/zebrafish_VEGA.gff3
	nawk 'BEGIN{OFS="\t";print "##gff-version   3"}\
	/^[^# ]/{if($$2 != "intron"){$$1 = $$1 "\tvega";print}\
	}' $< > $@

# put the chromosome lengths in their own file
$(DEST)/vega_chromosome.gff3: $(DEST)/zebrafish_VEGA.gff3
	grep "^#" $< >  $@

# make the gff3 file more informix loadable
# by reducing it from pair-value to just columns of values

$(DEST)/drerio_vega.unl: parse-gff3-unl.r  $(DEST)/drerio_vega.gff3
	$+ > $@

$(DEST)/drerio_vega_id.unl: generate_id.awk	$(DEST)/drerio_vega.unl
	sort -t \| -k10,10 -k1,1n -k4,4n $(DEST)/drerio_vega.unl | generate_id.awk > $@

# for anotation status on assembly clone tracks (mind the TAB ' ')

$(DEST)/clone_acc_status.unl: $(DEST)/clonelist_for_tom.tab
	grep annotated $< | cut -f2,5 | tr '	' \| | tailpipe > $@


gff3_backbone: $(DEST)/drerio_vega_id.unl $(DEST)/vega_chromosome.gff3 \
	                 assembly_for_tom.tab $(DEST)/clone_acc_status.unl
	cp assembly_for_tom.tab         $${TARGETROOT}/home/data_transfer/Data
	cp $(DEST)/drerio_vega_id.unl   $${TARGETROOT}/home/data_transfer/Data
	cp $(DEST)/vega_chromosome.gff3 $${TARGETROOT}/home/data_transfer/Data
	cp $(DEST)/clone_acc_status.unl $${TARGETROOT}/home/data_transfer/Data
	@echo "$${TARGETROOT}/home/data_transfer/Data"
	@ls -lt $${TARGETROOT}/home/data_transfer/Data

gff3_backbone_commit: $(DEST)/drerio_vega_id.unl $(DEST)/vega_chromosome.gff3 \
	                 assembly_for_tom.tab $(DEST)/clone_acc_status.unl
	cp assembly_for_tom.tab         /research/zprod/gff3
	cp $(DEST)/drerio_vega.unl      /research/zprod/gff3
	cp $(DEST)/vega_chromosome.gff3 /research/zprod/gff3
	cp $(DEST)/clone_acc_status.unl /research/zprod/gff3

clean_gff3_backbone:
	#rm -fr $(DEST)/zebrafish_VEGA*.gff3.gz
	rm -f $(DEST)/drerio_vega.gff3
	touch $(DEST)/zebrafish_VEGA*.gff3.gz

# vega on Ensembl coordinates
$(DEST)/zebrafish_Zv9_%.gff3.gz:
	wget --timestamping "ftp://${FTP}/zebrafish_Zv9_%.gff3.gz" -P $(DEST)

########################################################################

clean: 	clean_fetch_vega \
		clean_novel_check \
		clean_withdrawn_check \
		clean_blast_transcripts \
		clean_load_transcripts \
		clean_load_assembly \
		clean_withdraw_transcripts \
		clean_update_tscript_names \
		clean_gff3_backbone \
		clean_introspect


.PHONY: begin clean all withdrawn_check count_deflines count_versions \
	sanity_check_vers blast_transcripts check_vega_type

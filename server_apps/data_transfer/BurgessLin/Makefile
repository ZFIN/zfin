#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, Data Transfer server apps directory
#
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# Targets made by this makefile are used to transfer data into or out of
# ZFIN.  They are not directly callable from the web site.
#
# Outgoing data transfer files go in one of two places:
#  FTP:  /pub/data_transfer
#  HTTP: /data_transfer
# Over the long run ZFIN is probably moving from FTP to HTTP for outgoing
# data transfer.  HTTP is slightly slower, but using HTTP only means
# that we have one fewer thing to keep running.

# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/data_transfer/BurgessLin/

SUBDIRS =

GENERICS =

STATICS = load_Burgess_Lin.sql bed2gff3.awk rollback.sql commit.sql

# Define targets that require special handling.  This relies on the
# home directory being made before the server_apps directory.

GLOBAL_GFF_DATASTORE =/research/zprodmore/gff3

# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules


# ---------------  Endemic Targets  --------------------------------------


# ---------------  Misc Targets  ----------------------------------------

########### LOAD B/L data into zfin ######################################
#  boils down to:
#	o a feature name
#	o genbank accessions
#	o ensemble assembly location & strand
#	o sometimes associated with a gene
#	o overlaping subsets of the above define a genotype
# all in all, not very much,
# it is expected at least 75% will never be useful.
# try not to get too excited.

sheet1:
	$(error EXAMPLE: ln -s /research/zprod/data/BurgessLin/2012-Jan/Plate7-12FinalSubmit_cleanedup_yb_2012_sheet1.txt sheet1)
sheet2:
	$(error EXAMPLE: ln -s /research/zprod/data/BurgessLin/2011-Oct/Plate7-12FinalSubmit_cleanedup_yb_2012_sheet2.txt sheet2)

convert: convert_sheet1.sh  convert_sheet2.awk sheet1 sheet2
	convert_sheet1.sh
	convert_sheet2.awk sheet2  | sort -u > la_fish_parent.tab

load_bl: convert load_Burgess_Lin.sql la_gb_chr_loc.tab la_fish_parent.tab rollback.sql
	cat load_Burgess_Lin.sql rollback.sql | dbaccess -a $(DBNAME)

load_bl_commit: convert load_Burgess_Lin.sql  la_gb_chr_loc.tab la_fish_parent.tab commit.sql 
	cat load_Burgess_Lin.sql commit.sql | dbaccess -a $(DBNAME)
	unlink sheet1
	unlink sheet2
	rm -f la_gb_chr_loc.tab la_fish_parent.tab 

####################### PUSH B/L BED  FILE ##########################################
###
###  this target is to convert the BED file from B/L
###  into a gff3 formated file with tabs converted to pipes for informix:
###  file deposited in	$(GLOBAL_GFF_DATASTORE)/Burgess_Lin.unl
###
# The point is to leave the incomming bed file name as is for future reference
# and to be able to push whichever version is appropiate.
# you can do this by making a symlink from whatever bedfile and linking it to "B.bed"
# then runing: gmake push_bed
#
# This will make a informix loadable gff3 file to load into the gff3 table
# which is done in Downloads/GFF3/ with: gmake load_burgess_lin [_commit]
# after which a Zfin specific transgenic insertion gbrowse track
# may be extracted (daily) with gmake run  or gmake run_BL
#
# note: just because the Burgess_Lin.unl file is available, 
# no site including production needs to pick it up. 
# since this process only happens manually and infrequently
# compared with daily track regeneration you should be able to 
# choose not to pull a bad Burgess_Lin.unl into production.  
# going back and pushing an older one if need be 

BL.bed :
	$(error EXAMPLE: ln -s /research/zprod/data/BurgessLin/2011-Oct/zfin_Plate7-12Final.Overlap.InsertSite.bed BL.bed)

push_bed:  bed2gff3.awk BL.bed
	bed2gff3.awk BL.bed > $(GLOBAL_GFF_DATASTORE)/Burgess_Lin.unl
	-chmod g+rw $(GLOBAL_GFF_DATASTORE)/Burgess_Lin.unl
	unlink BL.bed



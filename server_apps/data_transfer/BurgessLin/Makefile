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

STATICS = stow_bed.sh Makefile plate_la_split.awk\
README.txt stow_bed.sh


# Define targets that require special handling.  This relies on the
# home directory being made before the server_apps directory.

GLOBAL_GFF_DATASTORE =/research/zprodmore/gff3

# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules


# ---------------  Endemic Targets  --------------------------------------


# ---------------  Misc Targets  ----------------------------------------
###
###  this target is to convert the BED file from B/L
###  into a tab separated file to leave in:
### 	$(GLOBAL_GFF_DATASTORE)/BL_chr_beg_end_la_strnd.tab
###

BL.bed :
	$(error "EXAMPLE: ln -s zfin_Plate7-12Final.Overlap.InsertSite.bed BL.bed")

run_gff3:  stow_bed.sh BL.bed
	stow_bed.sh BL.bed >! $(GLOBAL_GFF_DATASTORE)/BL_chr_beg_end_la_strnd.tab
	chmod g+rw $(GLOBAL_GFF_DATASTORE)/BL_chr_beg_end_la_strnd.tab
	unlink BL.bed



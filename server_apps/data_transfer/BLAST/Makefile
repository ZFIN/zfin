#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, creates data files for Genomix to downloads
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# This makefile produces the script that creates zfin accession number
# to zfin genes/clones translation file, and put the file to ftp site 
# for Genomix to download from. 
# The script is run by cron. 

# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/data_transfer/BLAST

GENERICS = push4genomix.pl getZfinGbAcc.pl getShortSeq.pl getZfinGenomicDNAAll.pl


# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules


# ---------------  Misc Targets  ----------------------------------------

# Run the download script.  This extracts data from ZFIN and places it in 
# the public ftp directory

run : 
	$(TARGETDIR)/push4genomix.pl
#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, GenPept automated data load directory
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

TARGETDIR = $(TARGETROOT)/server_apps/data_transfer/GenPept

GENERICS =  fetch_load_GenPept.sh fetch-genpept.pl

STATICS =   load_prot_len_acc.sql README manual_curation.genpept\
	load_manual_genpept.sql parse-genpept.r

# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules


# ---------------  Misc Targets  ----------------------------------------

# Run the load script.  This pulls data from GenPept and loads it into ZFIN.

run : $(TARGETS)
	$(TARGETDIR)/fetch_load_GenPept.sh


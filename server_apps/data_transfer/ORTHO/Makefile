
#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, orthology data load directory
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

TARGETDIR = $(TARGETROOT)/server_apps/data_transfer/ORTHO

GENERICS = runOrthology.pl loadAndUpdateNCBIOrthologs.sql parseOrthoFile.pl downloadFiles.pl reportOrthoNameChanges.pl updateZebrafishGeneNames.pl \
           loadAndUpdateNCBIOrthologs_PG.sql reportOrthoNameChanges_PG.pl runOrthology_PG.pl updateZebrafishGeneNames_PG.pl updateZebrafishGeneNames_PG.sql parseOrthoFile_PG.pl
STATICS =  updateZebrafishGeneNames.sql compareNcbiMgiOmim.pl


# Define targets that require special handling.

HTTP_DATA_TRANSFER_DIR = $(TARGETROOT)/home/data_transfer/ORTHO

ENDEMICTARGETS_PRE = $(HTTP_DATA_TRANSFER_DIR)


# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules



# ---------------  Endemic Targets  -------------------------------------

$(HTTP_DATA_TRANSFER_DIR) :
	$(TARGET_MKDIR) $@


# ---------------  Misc Targets  ----------------------------------------

run :
	cd $(TARGETDIR) && update_orthology.sh

force :
	cd $(TARGETDIR) && update_orthology.sh -f


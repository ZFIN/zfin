#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, PUBMED
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

TARGETDIR = $(TARGETROOT)/server_apps/data_transfer/PUBMED
SUBDIRS   = LinkOut Journal
GENERICS  = addMeshTermsToAllPubs.groovy \
            addGeoIdsToAllPubs.groovy \
            loadNewPubs.sql \
            updatePublicationDate.groovy \
            pubActivation.groovy 
STATICS  =  PubmedUtils.groovy loadMeshTerms.groovy fetchPubsFromPubMed.groovy getPDFandImages.groovy load_figs_and_images.sql load_pub_files.sql

# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules


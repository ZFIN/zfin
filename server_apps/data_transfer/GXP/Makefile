#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, Gene expression data load directory
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

TARGETDIR = $(TARGETROOT)/server_apps/data_transfer/GXP

SUBDIRS = Thisse KKawakamiData

STATICS = README pre_gxp_load.sql gxp_load_func.sql \
	gxp_load_quantity_check.sql processImage.sh \
	thumbnail.sh filterBlast.pl blast2tab.pl ispell_common \
	pre_gxp_load_cleanup.sql gxp_load_cleanup.sql load_gxp.sh 


# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules

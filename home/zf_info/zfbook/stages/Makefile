#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, Zebrafish Book Stages web pages.
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

TARGETDIR = $(TARGETROOT)/home/zf_info/zfbook/stages

SUBDIRS = figs

STATICS = abst.html blast.html blast_stgs.html cleave.html cleave_stgs.html \
	gast.html gast_stgs.html gloss.html hatch.html hatch_stgs.html \
	index.html intro.html larval.html larv_stgs.html \
	org.html phar.html phar_stgs.html proc.html \
	refs.html seg.html seg_stgs.html temp.html zygote.html

GENERICS = stages.html

# ---------------  Production Rules  ------------------------------------

# Use default rules for directories without app pages in them.

include $(TOP)/make.default.rules

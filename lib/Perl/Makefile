#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, lib/Perl directory
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
#


# ---------------  Variable Definitions  --------------------------------

TOP = ../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/lib/Perl

#SUBDIRS = bioperl gbrowse cpan

# each line executes in it's own shell ala cron
#SVNROOT = /research/zcentral/Vault/SVNROOT/

# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them
# include $(TOP)/make.default.rules

# the point here is to overwrite any bioperl graphics with the cpan Bio-Graphics
all :
	rsync -r --delete -C --exclude=Makefile ./bioperl $(TARGETDIR)/ && cp -fr  ./cpan/Bio/* $(TARGETDIR)/bioperl/Bio/

# ---------------  Special Targets  -------------------------------------

#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, Zebrafish News directory.
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

TARGETDIR = $(TARGETROOT)/home/zf_info/news

# Subdirectories come and go as meetings come and go.

GENERICS = siteNews.html

# List permanent files first -- files that will stay around no matter what
# announcements come and go.  Follow that list with files that will disappear
# when the meetings or jobs they go with will disappear.

PERMANENT_STATICS = bluline.gif jobs.html mtgs.html

STATICS = $(PERMANENT_STATICS) workshop.pdf




# ---------------  Production Rules  ------------------------------------

# Use default rules for directories without app pages in them.

include $(TOP)/make.default.rules



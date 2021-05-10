#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, WWW Server Apps directory
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# Targets made by this makefile are applications that run on the server
# and that are not directly callable through HTTP.  Applications that
# run on the server that are directly callable through HTTP are in the
# cgi-bin directory.


# ---------------  Variable Definitions  --------------------------------

TOP = ..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps

SUBDIRS = apache cron data_transfer sysexecs WebSiteTools \
	DB_maintenance Reports quicksearch

GENERICS = ZFINPerlModules.pm
STATICS = build.xml


# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules

# ---------------  Misc Targets  ----------------------------------------

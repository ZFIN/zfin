#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, ZFIN CGI binary directory.
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
#


# ---------------  Variable Definitions  --------------------------------

TOP = ..
include $(TOP)/make.include

#SUBDIRS = cgiemail-1.6

TARGETDIR = $(TARGETROOT)/$(TARGETCGIBIN)


STATICS = favicon.ico

GENERICS = ZFIN_jump map-options.pl print_map.cgi runSPL.cgi cgiemail.cgi\
	   DebugWebdriver searchwww.cgi send_request.perl \
	   mapper_select.cgi mapper_select.pl \
	   view_mapplet.cgi view_zmapplet.cgi select_structures.cgi \
	   xpat_select_parts.cgi regen_genomics.cgi \
	   input_welcome.cgi input_welcome_generic.cgi


# ---------------  Production Rules  ------------------------------------

# Use default rules.

include $(TOP)/make.default.rules

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


TARGETDIR = $(TARGETROOT)/$(TARGETCGIBIN)


STATICS = favicon.ico

GENERICS = ZFIN_jump map-options.pl print_map.cgi runSPL.cgi \
	   searchwww.cgi send_request.perl \
	   mapper_select.cgi mapper_select.pl \
	   view_mapplet.cgi view_zmapplet.cgi select_structures.cgi \
	   input_welcome.cgi input_welcome_generic.cgi expired_locus.cgi \
           publications_today.cgi send_notification.cgi usersurvey2005.cgi \
	   upload.cgi pre_vega.cgi send_Judy_your_report.pl \
	   weekly_report_reminder.txt new_gene.cgi new_line.cgi \
	   internal_vega.cgi internal_wzest.cgi

# ---------------  Production Rules  ------------------------------------

# Use default rules.

include $(TOP)/make.default.rules

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


STATICS = favicon.ico weekly_report_reminder.txt send_notification.cgi 

GENERICS = map-options.pl print_map.cgi runSPL.cgi \
	   searchwww.cgi send_request.perl \
	   mapper_select.cgi mapper_select.pl \
	   view_mapplet.cgi view_zmapplet.cgi \
       internal_wzest.cgi \
	   upload.cgi \
	   new_gene.cgi new_line.cgi merge_markers.pl 

# ---------------  Production Rules  ------------------------------------

# Use default rules.

include $(TOP)/make.default.rules

#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, Site Down web pages.
#
# $Source$
# $Id$
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# The web pages in this directory are used in these situations:
# 1. whenever access to a web site is down (possibly only down to the 
#    outside world, possibly down to everyone), but apache and the server 
#    machine are up enough to at least display a static web page.  (In the 
#    case where the server machine is barely up, one of the web pages 
#    should be copied to /private/httpd/htdocs and the httpd.conf file 
#    should be modified to point at it.)
# 
#    This directory contains web pages for a variety of situations.  The files
#    that start with main_ can be used on the main zfin site, and files 
#    starting with backup_ can be used on the backup site.
#
#    main_downNoBackup.html   Main site is down and backup is not coming up.
#	 		      Points users only to mirror sites.
#    main_downGotoBackup.html Main site is down, but backup is either up or
#                             will be coming up shortly.
# 			      Points users to backup site and mirror sites.
#    main_omingUp.html        Main site is in process of coming up.
# 			      Tells users main site will be up shortly
#    backup_comingUp.html     Main site is down, in the process of bringing
#                             up the backup site.
#                             Tells users backup site will be up shortly.
# 			      Also points them to mirror sites.
#    backup_gotoMain.html     Backup is down, main is up.
#                             Tells users to go to main site.
# 
#    In order for any of these to actually be used, 
#    /private/httpd/conf/httpd.conf must also be modified.
# 
# 2. General error handling.
#
#    unsupportedLink.html     Used whenever someone tries to come in from
# 			      outside of ZFIN by going directly to webdriver
#			      instead of using the ZFIN_jump mechanism.
#			      Note that we do not detect all such accesses,
#			      but any that we do detect get sent to this page.


# ---------------  Variable Definitions  --------------------------------

TOP = ../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/home/unavailable

STATICS = backup_comingUp.html backup_gotoMain.html main_comingUp.html \
          main_downGotoBackup.html main_downNoBackup.html \
	  unsupportedLink.html



# ---------------  Production Rules  ------------------------------------

# Use default rules for directories without app pages in them.

include $(TOP)/make.default.rules

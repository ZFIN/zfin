#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, Server apps, cron directory
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# This directory puts the contents of the informix crontab file under 
# CVS control.  We do this for a number of reasons:
#
# 1. It just seems like a good idea to have the crontab file under
#    source code control, just in case we inadvertently removed it.
#
# 2. We occassionally switch the web site from the production server
#    to the backup/development server.  Switching the crontab file
#    from one machine to the other (and then back) has been problematic.
#    Hopefully, this makefile will smooth that process.


# ---------------  Variable Definitions  --------------------------------

TOP = ../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/cron

GENERICS = crontab


# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules


# ---------------  Misc Targets  ----------------------------------------

# Provide targets to start and stop cron.
#   stop	Clears the CURRENT USER's crontab file on this machine
#   start	Sets the CURRENT USER's crontab file on this machine.
#
# The current user had better be INFORMIX.  Otherwise the target will 
# either fail (because you don't have cron permission), or worse yet,
# succeed and thereby stomp on your personal crontab file.
#
# Also note, that typing "gmake start" or "gmake stop" at the top of
# ZFIN_WWW tree will not fire any targets in this directory.

start :
	crontab $(TARGETDIR)/crontab

stop :
	crontab -r

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
# This directory puts the contents of ZFIN crontab files under 
# CVS control.  We do this for a number of reasons:
#
# 1. It just seems like a good idea to have the crontab files under
#    source code control, just in case we inadvertently removed it.
#
# 2. We occassionally switch the web site from the production server
#    to the backup/development server.  Switching the crontab files
#    from one machine to the other (and then back) has been problematic.
#    Hopefully, this makefile will smooth that process.


# ---------------  Variable Definitions  --------------------------------

TOP = ../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/cron

GENERICS = crontab.production crontab.development crontab.zfishweb crontab.informix.test
STATICS= crontab.root.helix


# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules


# ---------------  Misc Targets  ----------------------------------------

# Provide targets to start and stop cron.
#   stop	Clears the CURRENT USER's crontab file on this machine
#   start	Sets the CURRENT USER's crontab file on this machine.
#               If not running on embryonix (i.e., you are running on the
#               regular production machine) this uses just the file 
#               "crontab.production".
#               If running on embryonix this uses the file "crontab.production"
#               and the file "crontab.development" concatenated together.
#   startdevl   Sets the CURRENT USER's crontab file on this machine
#               to the contents of the "crontab.development machine.
#
# The start and startdevl targets are both needed because we have cron
# jobs that run on both production and development.  When production is
# running on development we achieve this by concatenating the files
# together.
#
# The current user had better be INFORMIX.  Otherwise the target will 
# either fail (because you don't have cron permission), or worse yet,
# succeed and thereby stomp on your personal crontab file.
#
# Also note, that typing "gmake start" or "gmake stop" at the top of
# ZFIN_WWW tree will not fire any targets in this directory.

start : $(TARGETS)
  ifneq ($(HOST),embryonix)
	/bin/crontab $(TARGETDIR)/crontab.production
  else
	/bin/cat $(TARGETDIR)/crontab.production $(TARGETDIR)/crontab.development | /bin/crontab
  endif

startdevl : $(TARGETS)
	/bin/crontab $(TARGETDIR)/crontab.development

# as informix src'd test.env, you can combine crons for production and development (keeping those
# for almost pointing to almost, and the rest pointing to test.
starttest : $(TARGETS)
	/bin/crontab $(TARGETDIR)/crontab.informix.test

stop :
	/bin/crontab -r

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

SUBDIRS = cron data_transfer mapimage sysexecs WebSiteTools \
	DB_maintenance Reports quicksearch

MIRROR_SUBDIRS = WebSiteTools



# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules

# ---------------  Misc Targets  ----------------------------------------

mirror: $(TARGETDIR) $(MIRROR_SUBDIRS)

# Note that the cron directory is not affected by the start or stop
# targets.  To start or stop cron you must go into the cron directory
# and start or stop it there (something which should only be done for
# the informix user, by the way).
#
# Since the javaserver went away, the stop and start targets don't do 
# anything.  Hang onto them anyway just in case.

start stop :
	echo 'start and stop targets no longer do anything in server_apps directory.

postloaddb :
	$(MAKE) -C sysexecs clobber
	$(MAKE) -C sysexecs 
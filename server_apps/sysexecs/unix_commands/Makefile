#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, WWW Server Apps sysexecs unix commands
# directory
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
# This directory populates the execweb table with records that are just
# invocations of existing Unix commands.  The other sysexec makefiles
# populate the execweb tables with script names and then also create 
# the scripts.  In the case of unix commands, there are no scripts to
# create.
#
# NOTE: This is a complicated Makefile.  Read on.
#
# The output of this makefile is records in the database.  There are not
# corresponding source files for each record this makefile adds.  This 
# presents a problem for make because make is driven by the timestamps 
# on files.  This makefile goes through a great deal of effort
# to create and manipulate a file in the TARGETDIR for each record it
# creates.  It then uses that created (and empty) file to tell it when to
# reinsert the records.


# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/sysexecs/unix_commands

# The COMMANDS_WITH_PATH must have full paths.

COMMANDS_WITH_PATH = /bin/rm
COMMANDS = $(notdir $(COMMANDS_WITH_PATH))

STATICS = $(COMMANDS)

STATICTARGETS = $(foreach STAT, $(STATICS), $(TARGETDIR)/$(STAT))
TARGETS = $(STATICTARGETS)



# ---------------  Production Rules  ------------------------------------

.PHONY : all sanitycheck reloadrow clobberrow clean clobber onetimeonly


all : $(TARGETDIR) $(TARGETS)


$(TARGETDIR) :
	$(TARGET_MKDIR) $(TARGETDIR);

$(STATICTARGETS) :
	echo 'delete from execweb where execweb_id = "$(notdir $@)"' | $(DBACCESS) $(DBNAME)
	echo 'insert into execweb (execweb_id, execweb_executable) values("$(notdir $@)","$(filter %$(notdir $@), $(COMMANDS_WITH_PATH))")' | $(DBACCESS) $(DBNAME)
	touch $@



# ---------------  Maintenance Rules  -----------------------------------

clean :				# No local intermediate files or subdirs


# In addition to removing the targets (but not the targetdir), 
# clobber also removes the row for encryptpass from the execweb table

clobber :			# Remove targets, but not target dir.
	rm -f $(TARGETS)
	$(foreach CMND, $(COMMANDS), echo 'delete from execweb where execweb_id = "$(CMND)"' | $(DBACCESS) $(DBNAME) ;)


sanitycheck :			# Maybe other stuff to do here as well.
	$(SPECIFICCHECK) $(GENERICS)


# define one time only target, for when something special needs to be done
# only once.  

onetimeonly :
	$(ONETIMEONLY)



# ---------------  Misc Targets  ----------------------------------------


#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, WWW Server Apps sysexecs directory
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# Several app pages invoke unix executables/scripts on the server through
# the sysexec SQL funtion, which is currently (2000/12) defined in 
# lib/DB_functions/zextend/zextend.c.  That function is passed a string
# that uniquely identifies a unix executable/script within ZFIN.  The
# sysexec function uses that string to lookup a row in the execweb table
# in the database.  It gets the full path of the executable from that
# row.
#
# This makefile creates the executables that are called by sysexec and
# populates the execweb table as well.  The unix_commands subdirectory 
# handles the case where the executables are one line invocations of Unix 
# commands and therefore there is no supporting script.
#



# ---------------  Variable Definitions  --------------------------------

TOP = ../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/sysexecs

SUBDIRS = image_stats make_thumbnail unix_commands

# There are no local targets


# ---------------  Production Rules  ------------------------------------

.PHONY : all sanitycheck clean clobber $(SUBDIRS)


all : $(TARGETDIR) $(SUBDIRS)


$(TARGETDIR) :
	$(TARGET_MKDIR) $(TARGETDIR);

$(SUBDIRS) :
	$(MAKE) -C $@



# ---------------  Maintenance Rules  -----------------------------------

clean :				# No local intermediate files
	$(foreach SUBDIR,$(SUBDIRS), $(MAKE) -C $(SUBDIR) $@; )


# The clobber target is why this file can use the make.default.rules file.
# It calls clobber in the subdirectories, which is standard, but also
# removes all rows from the execweb table, which is not standard.

clobber :			# Remove targets, but not target dir.
	echo 'delete from execweb' | $(DBACCESS) $(DBNAME)
	$(foreach SUBDIR,$(SUBDIRS), $(MAKE) -C $(SUBDIR) $@; )

sanitycheck :			# No local files to check.
	$(foreach SUBDIR,$(SUBDIRS), $(MAKE) -C $(SUBDIR) $@; )


# define one time only target, for when something special needs to be done
# only once.  

onetimeonly :
	$(ONETIMEONLY)
	$(foreach SUBDIR,$(SUBDIRS), $(MAKE) -C $(SUBDIR) $@; )



# ---------------  Misc Targets  ----------------------------------------


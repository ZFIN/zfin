#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, SQL/SPL procedures.
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# The routines defined in this directory are written in SPL, the Informix
# Stored Procedure language.  Each file first deletes the routine, and
# then defines it.  Files have the same name as the routine they define.
# Each file defines only one routine.
#

# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/lib/DB_functions/SPL

STATICS  = regen_genomics.sql regen_fishsearch.sql

STATICTARGETS = $(foreach STAT, $(STATICS), $(TARGETDIR)/$(STAT))
TARGETS =  $(STATICTARGETS) 


# ---------------  Production Rules  ------------------------------------

.PHONY : all sanitycheck clean clobber onetimeonly


all : $(TARGETDIR) $(TARGETS)

$(TARGETDIR) :
	$(TARGET_MKDIR) $(TARGETDIR);


# SPL procedures need to be loaded into the DB.
# This makefile assumes that code to delete the old version of the function
# is in the file.

$(STATICTARGETS) : $(TARGETDIR)/% : %
	$(TARGET_CP) $^ $@
	$(DBACCESS) $(DBNAME) $@


# ---------------  Maintenance Rules  -----------------------------------

clean :				# No local intermediate files


clobber :			# Remove targets, but not target dir.
	rm -f $(TARGETS)



sanitycheck :
	$(SPECIFICCHECK) $(TARGETS)


# define one time only target

onetimeonly :
	$(ONETIMEONLY)

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
# See the routines themselves for comments on their parameters and what 
# they do.

# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/lib/DB_functions/SPL

FISH_FUNCS  = get_fish_full_name.sql
IMAGE_FUNCS = get_image_stats.sql
REGEN_FUNCS = regen_anatomy.sql regen_fishsearch.sql regen_genomics.sql
ZDB_FUNCS   = get_obj_name.sql

# Some of the stage functions call other stage functions.  Therefore
# the order they are listed matters.

STAGE_FUNCS = \
	create_stg_name_ext.sql \
	create_stg_name_long.sql \
	get_stg_name_html.sql \
	get_stg_name_long_html.sql \
	stg_window_consistent.sql \
	fimg_overlaps_stg_window.sql \
	anatitem_overlaps_stg_window.sql \
	stg_windows_overlap.sql

GENERICS = $(REGEN_FUNCS)
STATICS  = $(FISH_FUNCS) $(IMAGE_FUNCS) $(STAGE_FUNCS) $(ZDB_FUNCS)

SPECIFICTARGETS = $(foreach SPEC, $(GENERICS), $(TARGETDIR)/$(SPEC))
STATICTARGETS = $(foreach STAT, $(STATICS), $(TARGETDIR)/$(STAT))


TARGETS =  $(SPECIFICTARGETS) $(STATICTARGETS) 


# ---------------  Production Rules  ------------------------------------

.PHONY : all sanitycheck clean clobber onetimeonly


all : $(TARGETDIR) $(TARGETS)

$(TARGETDIR) :
	$(TARGET_MKDIR) $(TARGETDIR);


# SPL procedures need to be loaded into the DB.
# This makefile assumes that code to delete the old version of the function
# is in the file.

$(SPECIFICTARGETS) : $(TARGETDIR)/% : %
	$(MAKESPECIFIC) $< $(TRANSLATETABLE) $@
	$(DBACCESS) $(DBNAME) $@

$(STATICTARGETS) : $(TARGETDIR)/% : %
	$(TARGET_CP) $^ $@
	touch $@
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

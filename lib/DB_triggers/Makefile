#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, WWW Database triggers library
# directory
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# Every trigger that is defined on ZFIN tables is defined in its own 
# file in this directory.  

# ST: modified trigger files and this Makefile to have the Makefile itself 
# drop all the triggers at make-time.   
# Each file creates the trigger.  
# Files *must* have the same name as the trigger they define.
# ---------------  Variable Definitions  --------------------------------

TOP = ../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/lib/DB_triggers

DBLINK_TRIGGERS = dblink_insert_trigger.sql \
		  dblink_acc_num_update_trigger.sql

PUB_TRIGGERS = publication_insert_trigger.sql \
	       publication_update_trigger.sql

STAGE_TRIGGERS = stage_insert_trigger.sql \
		 stage_update_trigger.sql 

ZERO_PAD_TRIGGERS = alteration_insert_trigger.sql \
		    alteration_allele_update_trigger.sql \
		    locus_insert_trigger.sql \
		    locus_abbrev_update_trigger.sql \
		    marker_insert_trigger.sql \
		    mrkr_abbrev_update_trigger.sql \
		    mrkr_name_update_trigger.sql \
		    fish_insert_trigger.sql \
		    fish_allele_update_trigger.sql \
		    fish_name_update_trigger.sql

STATICS = $(DBLINK_TRIGGERS) $(PUB_TRIGGERS) \
          $(STAGE_TRIGGERS) $(ZERO_PAD_TRIGGERS)
STATICTARGETS = $(foreach STAT, $(STATICS), $(TARGETDIR)/$(STAT))

SUBDIRS = ValidateTriggers

TARGETS =  $(STATICTARGETS)


# ---------------  Production Rules  ------------------------------------

.PHONY : all sanitycheck clean clobber onetimeonly $(SUBDIRS)

all : $(TARGETDIR) $(TARGETS) $(SUBDIRS)

$(TARGETDIR) :
	$(TARGET_MKDIR) $(TARGETDIR);

$(STATICTARGETS) : $(TARGETDIR)/% : %
	$(TARGET_CP) $^ $@
	$(TOUCH) $@
	-$(DBACCESS) $(DBNAME) $@

$(SUBDIRS) :
	$(MAKE) -C $@

# ---------------  Maintenance Rules  -----------------------------------

clean :				# No local intermediate files
	$(foreach SUBDIR,$(SUBDIRS), $(MAKE) -C $(SUBDIR) $@; )

clobber :			# Remove targets, but not target dir.
	rm -f $(TARGETS)
	$(foreach SUBDIR,$(SUBDIRS), $(MAKE) -C $(SUBDIR) $@; )


sanitycheck :
	$(SPECIFICCHECK) $(TARGETS)
	$(foreach SUBDIR,$(SUBDIRS), $(MAKE) -C $(SUBDIR) $@; )

# define one time only target

onetimeonly :
	$(ONETIMEONLY)
	$(foreach SUBDIR,$(SUBDIRS), $(MAKE) -C $(SUBDIR) $@; )


# ---------------  Special Rules  ---------------------------------------

postloaddb : clobber all



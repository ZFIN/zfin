#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, WWW Database triggers to help replace 
# validate_data.pl nightly check.
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
#
# This makefile is unique from other makefiles in that it does the work of dropping each 
# trigger from the database before it creates them.  
# This way, we can capture error messages that come from compiling the trigger itself, and ignore 
# the error that comes from the command to drop a trigger that does not exisit.  
# Requirements for new triggers: Trigger files *must* have the same name as the trigger they define.
# the first line in the trigger file should be "create trigger..." not "drop trigger..."
# each trigger should end with a semi-colon.
#
# ---------------  Variable Definitions  --------------------------------
TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/lib/DB_triggers/ValidateTriggers

TRIGGERS = marker_relationship_insert_trigger.sql \
	   marker_relationship_update_trigger.sql \
	zdb_object_type_insert_trigger.sql \
	zdb_object_type_update_trigger.sql \
	anatomy_contains_insert_trigger.sql \
	anatomy_contains_update_trigger.sql \
	anatomy_items_insert_trigger.sql \
	anatomy_items_update_trigger.sql \
	expression_pattern_anatomy_insert_trigger.sql \
	expression_pattern_anatomy_update_trigger.sql \
	expression_pattern_image_insert_trigger.sql \
	expression_pattern_image_update_trigger.sql \
	expression_pattern_stage_insert_trigger.sql \
	expression_pattern_stage_update_trigger.sql \
	fish_image_stage_insert_trigger.sql \
	fish_image_stage_update_trigger.sql \
	fish_image_anatomy_insert_trigger.sql \
	fish_image_anatomy_update_trigger.sql \
	linkage_pair_member_insert_trigger.sql \
	linkage_pair_member_update_trigger.sql \
	marker_go_term_insert_trigger.sql \
	marker_go_term_update_trigger.sql \
	orthologue_evidence_display_insert_trigger.sql \
	orthologue_evidence_display_update_trigger.sql \

# DROPS = $(foreach TRIG, $(TRIGGERS), $(patsubst %,drop_%,$(TRIG)))

STATICS = $(TRIGGERS) 

STATICTARGETS = $(foreach STAT, $(STATICS), $(TARGETDIR)/$(STAT))

TARGETS =  $(STATICTARGETS)

# ---------------  Production Rules  ------------------------------------

.PHONY : all sanitycheck clean clobber onetimeonly

all : $(TARGETDIR) $(TARGETS)

$(TARGETDIR) :
	$(TARGET_MKDIR) $(TARGETDIR);

$(STATICTARGETS) : $(TARGETDIR)/% : %
	-echo "drop trigger $(patsubst %.sql,%,$^)" | $(DBACCESS) $(DBNAME) #drop trigger
	$(TARGET_CP) $^ $@
	$(TOUCH) $@
	$(DBACCESS) $(DBNAME) $^


# ---------------  Maintenance Rules  -----------------------------------

clean :				# No local intermediate files


clobber :			# Remove targets, but not target dir.
	rm -f $(TARGETS)



sanitycheck :
	$(SPECIFICCHECK) $(TARGETS)


# define one time only target

onetimeonly :
	$(ONETIMEONLY)



# ---------------  Special Rules  ---------------------------------------

postloaddb : clobber all



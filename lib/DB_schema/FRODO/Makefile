
#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, SWISS-PROT automated data load directory
#
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# See the README file. 


# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/lib/DB_schema/FRODO


GENERICS = Fv4.sh trigger_fix.sh \
	   fix_figs.sql \
	   fix_figs_1.sql \
           Frodo_v4.sql \
	   assign_default_annotations.sql \
	   PhenoKeywordTrans0601.csv \
	   EU_trans_1stSubmission.csv \
	   EU_tt_second_batch.csv \
	   hopkins_deletion.txt \
	   KeywordTrans_v3.csv \
	   KeywordTrans_v2.csv \
           EUKeywordNew_v1.csv \
	   notes_for_feature.csv \
           PhenoKeywordTrans_v4.1.csv \
	   PhenoKeywordTrans_v5.csv \
	   update_handle.sql \
	   update_himuts.sql \
           EU_text_stuff_for_all.csv \
	   EU_alleles_batch2.csv \
	   original_sub_description.csv \
           translateEU.pl \
	   translateEUtrans.pl \
	   translateNewEU.pl \
	   translateOSC.pl \
	   translatevirus.pl \
	   README \
	   regen_genotype_display.sql \
           attribute.sql \
	   update_Feature_notes.sql \
	   feature.try \
	   fix_efg.sql \
	   fix_aliases_2.sql \
	   fix_zdb_ids.sql \
	   fix_dup_apato.sql \
	   fix_Df_Tg_locus_names.sql \
	   fix_sources.sql \
	   fix_wts.sql \
	   last_constraints.sql \
	   load_obofile.sql \
	   load_aliases.sql \
	   lethal_fish.txt \
	   make_obo_table.sql \
	   merge_data.sql \
	   move_locusreg.sql \
	   move_EU_annotations.sql \
	   move_EU2_annotations.sql \
	   notes_for_genotype.txt \
	   notes_for_marker.tab \
	   notes_for_marker.txt \
           post_Fv4.sql update_image.sql \
	   translatePheno.pl \
           update_comments.sql \
           drop_tables.sql \
	   parse_pheno_keywords.pl \
           segregation.unl \
           pato_infrastructure.sql \
	   just_apato_changes.sql \
           fix_entity_group_members.sql \
	   parse_tgconstructs.pl \
           unload_tgcons.sql \
           load_tg_con_relations.sql \
	   make_generic_pato_tables.sql \
           move_unTg_alleles.sql \
	   create_procedures_functions.sql \
	   replace_first_EU.sql \
	   add_breakpoint.sql \
	   load_second_eu.sql \
	   tg_changes_pato.sql \
	   update_feature_marker_relationships.sql \
	   virusperallele.csv \


# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules


# ---------------  Special Rules  ---------------------------------------

postloaddb : clean clobber all


create trigger marker_relationship_mrkr_2_update_trigger 
  update of mrel_mrkr_2_zdb_id 
  on marker_relationship
  referencing new as new_mrkr_rel old as old_mrkr_rel
    for each row (
        execute procedure p_mrel_grpmem_correct (
          new_mrkr_rel.mrel_mrkr_1_zdb_id, 
	  new_mrkr_rel.mrel_mrkr_2_zdb_id, 
	  new_mrkr_rel.mrel_type
        ),
        execute procedure p_update_fmrel_genotype_names(
          new_mrkr_rel.mrel_mrkr_2_zdb_id,
          old_mrkr_rel.mrel_mrkr_2_zdb_id)
    );


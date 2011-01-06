create trigger lab_feature_prefix_update_trigger
  update of lfp_current_designation on lab_feature_prefix
  referencing old as oldLfp
  	      new as newLfp
   for each row (
       execute procedure updatePrimaryLabFeaturePrefix (newLfp.lfp_lab_zdb_id, newlfp.lfp_current_designation,
       	       					      newLfp.lfp_prefix_id)
);
create trigger one_to_one_accession_update_trigger update of ooa_dblink_zdb_id
 on one_to_one_accession 
 referencing new as newo 
 for each row (execute procedure checkDblinkOneToOneAccessionMapping (newo.ooa_dblink_zdb_id, newo.ooa_feature_zdb_id, newo.ooa_dblink_acc_num))
;
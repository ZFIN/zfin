create trigger genofeat_update_trigger update of genofeat_feature_zdb_id 
    on genotype_feature referencing old as old_genofeat 
    new as new_genofeat
    for each row
        (
        execute procedure p_check_tginsertion_has_construct_relationship(new_genofeat.genofeat_feature_zdb_id 
    ));
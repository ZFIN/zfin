create trigger genofeat_insert_trigger insert on
    genotype_feature referencing new as new_genofeat
    for each row
        (
        execute procedure p_check_tginsertion_has_construct_relationship(new_genofeat.genofeat_feature_zdb_id,
	execute procedure p_update_related_genotype(new_genofeat.genofeat_feature_zdb_id)
    ));

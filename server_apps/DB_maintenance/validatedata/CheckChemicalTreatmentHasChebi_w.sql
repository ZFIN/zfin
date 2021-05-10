select expcond_exp_zdb_id, expcond_zdb_id
 from experiment_condition, all_term_Contains
 where expcond_zeco_term_Zdb_id = alltermcon_contained_zdb_id
	and alltermcon_container_zdb_id = (Select term_zdb_id from
						term where term_ont_id =
						'ZECO:0000111')
 and expcond_chebi_Term_zdb_id is null;

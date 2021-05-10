select genox_fish_zdb_id, fish_name, genox_exp_zdb_id, exp_name, term_name, term_ont_id, dat_source_zdb_id, disease_annotation
  from disease_annotation_model, term, fish_experiment, fish, disease_annotation,experiment
  where damo_genox_zdb_id = genox_zdb_id
  and genox_fish_zdb_id = fish_zdb_id
and term_zdb_id = dat_term_zdb_id
and genox_is_std_or_generic_control = 't'
and fish_functional_affected_gene_count = 0
 and exp_zdb_id = genox_exp_zdb_id
and dat_zdb_id = damo_dat_zdb_id;

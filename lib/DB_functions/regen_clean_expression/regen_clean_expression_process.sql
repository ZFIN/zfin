create or replace function regen_clean_expression_process()
returns void as $$

begin 
insert into regen_ce_temp (rggt_mrkr_zdb_id, rggt_genox_zdb_id)
  select distinct rggz_mrkr_zdb_id, rggz_genox_zdb_id
    from regen_ce_input_zdb_id_temp, fish_experiment, fish
    where rggz_genox_zdb_id = genox_zdb_id
    and genox_fish_Zdb_id = fish_zdb_id
    and fish_is_wildtype = 't'
    and rggz_mrkr_Zdb_id like 'ZDB-GENE%';

insert into regen_ce_temp (rggt_mrkr_zdb_id, rggt_genox_zdb_id)
  select distinct rggz_mrkr_zdb_id, rggz_genox_zdb_id
    from regen_ce_input_zdb_id_temp, fish_experiment, fish, genotype_feature a, feature_marker_relationship b
    where rggz_genox_zdb_id = genox_zdb_id
    and genox_fish_Zdb_id = fish_zdb_id
    and fish_is_wildtype = 'f'
    and fish_genotype_zdb_id = a.genofeat_geno_Zdb_id
    and b.fmrel_ftr_zdb_id = a.genofeat_feature_zdb_id
    and b.fmrel_type = 'contains innocuous sequence feature'
    and not exists (Select 'x' from genotype_feature c, feature_marker_relationship d
    	    	   	       where a.genofeat_geno_zdb_id = c.genofeat_Geno_Zdb_id
			       and a.genofeat_feature_zdb_id != c.genofeat_feature_zdb_id
			       and c.genofeat_feature_zdb_id = d.fmrel_ftr_zdb_id
			       and d.fmrel_type != 'contains innocuous sequence feature')
    and rggz_mrkr_Zdb_id like 'ZDB-GENE%';

insert into regen_ce_temp (rggt_mrkr_zdb_id, rggt_genox_zdb_id)
  select distinct rggz_mrkr_zdb_id, rggz_genox_zdb_id
    from regen_ce_input_zdb_id_temp, fish_experiment, fish, fish_str a
    where rggz_genox_zdb_id = genox_zdb_id
    and genox_fish_Zdb_id = fish_zdb_id
    and a.fishstr_fish_zdb_id = fish_zdb_id
    and a.fishstr_str_zdb_id = rggz_mrkr_zdb_id
    and not exists (Select 'x' from fish_Str b
    	    	   	   where b.fishstr_fish_Zdb_id = a.fishstr_fish_zdb_id
			   and b.fishstr_str_zdb_id != a.fishstr_str_zdb_id)
--    and fish_is_wildtype = 't'
    and rggz_mrkr_Zdb_id not like 'ZDB-GENE%';

insert into regen_ce_temp (rggt_mrkr_zdb_id, rggt_genox_zdb_id)
  select distinct rggz_mrkr_zdb_id, rggz_genox_zdb_id
    from regen_ce_input_zdb_id_temp, fish_experiment, fish, fish_str a, genotype_feature genofeat, feature_marker_relationship fmrel
    where rggz_genox_zdb_id = genox_zdb_id
    and genox_fish_Zdb_id = fish_zdb_id
    and a.fishstr_fish_zdb_id = fish_zdb_id
    and a.fishstr_str_zdb_id = rggz_mrkr_zdb_id
    and not exists (Select 'x' from fish_Str b
    	    	   	   where b.fishstr_fish_Zdb_id = a.fishstr_fish_zdb_id
			   and b.fishstr_str_zdb_id != a.fishstr_str_zdb_id)
    and fish_genotype_zdb_id = genofeat.genofeat_geno_Zdb_id
    and fmrel.fmrel_ftr_zdb_id = genofeat.genofeat_feature_zdb_id
    and fmrel.fmrel_type = 'contains innocuous sequence feature'
    and not exists (Select 'x' from genotype_feature c, feature_marker_relationship d
    	    	   	       where genofeat.genofeat_geno_zdb_id = c.genofeat_Geno_Zdb_id
			       and genofeat.genofeat_feature_zdb_id != c.genofeat_feature_zdb_id
			       and c.genofeat_feature_zdb_id = d.fmrel_ftr_zdb_id
			       and d.fmrel_type != 'contains innocuous sequence feature')
    and rggz_mrkr_Zdb_id not like 'ZDB-GENE%';

end ;
$$ LANGUAGE plpgsql;

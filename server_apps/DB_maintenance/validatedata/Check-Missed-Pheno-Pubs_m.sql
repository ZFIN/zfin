select zdb_id, entry_time, b.mrkr_zdb_id, b.mrkr_abbrev,
  a.mrkr_abbrev, a.mrkr_zdb_id
      from publication,record_attribution,
                        marker a, marker b,marker_relationship
            where recattrib_source_zdb_id = zdb_id
        and recattrib_data_zdb_id = a.mrkr_zdb_id
        and a.mrkr_type = 'MRPHLNO'
        and a.mrkr_zdb_id = mrel_mrkr_1_zdb_id
    and b.mrkr_zdb_id = mrel_mrkr_2_zdb_id
        and mrel_type = 'knockdown reagent targets gene'
        and mrel_mrkr_2_zdb_id not in (select fmrel_mrkr_Zdb_id from
                    feature_marker_relationship)
        and a.mrkr_zdb_id not in (Select expcond_mrkr_zdb_id
                        from experiment_condition,
                            genotype_experiment,
                            experiment,
                            phenotype_experiment
            where phenox_genox_zdb_id = genox_zdb_id
            and genox_exp_zdb_id = exp_zdb_id
            and expcond_exp_zdb_id =exp_zdb_id
            and expcond_mrkr_zdb_id is not null)
and entry_time >='2009-01-01 00:00:00.000'
and (pub_completion_date is null or pub_completion_date = '')
and not exists (Select 'x' from data_reporting
    	       	       where dr_data_zdb_id = a.mrkr_zdb_id)
;

insert into data_reporting (dr_data_zdb_id, dr_report_generated_date, dr_report_name)
  select distinct zdb_id, current year to second, "Check-Missed-Pheno-Pubs"
    from publication,record_attribution,
                        marker a, marker b,marker_relationship
            where recattrib_source_zdb_id = zdb_id
        and recattrib_data_zdb_id = a.mrkr_zdb_id
        and a.mrkr_type = 'MRPHLNO'
        and a.mrkr_zdb_id = mrel_mrkr_1_zdb_id
    and b.mrkr_zdb_id = mrel_mrkr_2_zdb_id
        and mrel_type = 'knockdown reagent targets gene'
        and mrel_mrkr_2_zdb_id not in (select fmrel_mrkr_Zdb_id from
                    feature_marker_relationship)
        and a.mrkr_zdb_id not in (Select expcond_mrkr_zdb_id
                        from experiment_condition,
                            genotype_experiment,
                            experiment,
                            phenotype_experiment
            where phenox_genox_zdb_id = genox_zdb_id
            and genox_exp_zdb_id = exp_zdb_id
            and expcond_exp_zdb_id =exp_zdb_id
            and expcond_mrkr_zdb_id is not null)
and entry_time >='2009-01-01 00:00:00.000'
and (pub_completion_date is null or pub_completion_date = '')
and not exists (Select 'x' from data_reporting
    	       	       where dr_data_zdb_id = a.mrkr_zdb_id); 
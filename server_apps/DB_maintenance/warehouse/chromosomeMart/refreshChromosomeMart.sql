--drop FKs.


delete from linkage_membership_search_bkup;

insert into linkage_membership_search_bkup
 select * from linkage_membership_search;


delete from sequence_feature_chromosome_location_generated_bkup
 where sfclg_location_source in ('other map location','General Load');

insert into sequence_feature_chromosome_location_generated_bkup (sfclg_chromosome, sfclg_data_zdb_id,
    sfclg_acc_num ,
    sfclg_start ,
    sfclg_end,
    sfclg_location_source,
    sfclg_location_subsource,
    sfclg_fdb_db_id)
select sfclg_chromosome, sfclg_data_zdb_id,
    sfclg_acc_num ,
    sfclg_start ,
    sfclg_end,
    sfclg_location_source,
    sfclg_location_subsource,
    sfclg_fdb_db_id from sequence_feature_chromosome_location_generated
where sfclg_location_source in ('other map location','General Load');

delete from linkage_membership_search;

update zdb_flag
  set (zflag_is_on,zflag_last_modified) = ("t",current year to second)
 where zflag_name = "regen_chromosomemart" ;

delete from sequence_feature_chromosome_location_generated
 where sfclg_location_source in ('other map location','General Load');

insert into sequence_feature_chromosome_location_generated (sfclg_chromosome, sfclg_data_zdb_id,
    sfclg_acc_num ,
    sfclg_start ,
    sfclg_end,
    sfclg_location_source,
    sfclg_location_subsource,
    sfclg_fdb_db_id)
 select distinct sfclg_chromosome, sfclg_data_zdb_id,
    sfclg_acc_num ,
    sfclg_start ,
    sfclg_end,
    sfclg_location_source,
    sfclg_location_subsource,
    sfclg_fdb_db_id from sequence_feature_chromosome_location_generated_temp
where sfclg_location_source in ('other map location','General Load');


insert into linkage_membership_search
  select * from linkage_membership_search_temp;

update linkage_membership set lnkgm_metric = null where
lnkgm_metric is not null and lnkgm_distance is null;

update linkage_membership_search set lms_units = null where
lms_units is not null and lms_distance is null;

update zdb_flag
  set (zflag_is_on,zflag_last_modified) = ("f",current year to second)
 where zflag_name = "regen_chromosomemart" ;

update warehouse_run_tracking
 set wrt_last_loaded_date = current year to second
 where wrt_mart_name = "chromosome mart";

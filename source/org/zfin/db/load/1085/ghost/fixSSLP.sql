--liquibase formatted sql
--changeset sierra:fixSSLP

begin work; 

create temp table tmp_id (id varchar(50))
with no log;

insert into tmp_id
 select get_id('SSLP') from single;

set constraints all deferred;

update mapped_marker 
 set marker_id = (select id from tmp_id)
 where marker_id = 'ZDB-SSLP-00515-2';

update paneled_markers
  set zdb_id = (Select id from tmp_id)
 where zdb_id = 'ZDB-SSLP-00515-2';

update record_attribution
 set recattrib_Data_zdb_id = (select id from tmp_id)
 where recattrib_data_zdb_id ='ZDB-SSLP-00515-2';

update unique_location
  set ul_data_zdb_id = (select id from tmp_id)
 where ul_data_zdb_id = 'ZDB-SSLP-00515-2';

update updates
 set rec_id = (select id from tmp_id)
 where rec_id = 'ZDB-SSLP-00515-2';

update zdb_active_data
 set zactvd_zdb_id = (select id from tmp_id)
 where zactvd_zdb_id = 'ZDB-SSLP-00515-2';

update zmap_pub_pan_mark
 set zdb_id = (select id from tmp_id)
 where zdb_id = 'ZDB-SSLP-00515-2';

update all_map_names
 set allmapnm_zdb_id = (select id from tmp_id)
 where allmapnm_zdb_id = 'ZDB-SSLP-00515-2';

update marker
 set mrkr_zdb_id = (select id from tmp_id)
 where mrkr_zdb_id = 'ZDB-SSLP-00515-2';

insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id)
 select 'ZDB-SSLP-00515-2', id 
   from tmp_id;

update sequence_feature_chromosome_location_bkup
  set sfcl_data_zdb_id = (select id from tmp_id)
  where sfcl_data_zdb_id = 'ZDB-SSLP-00515-2';

update sequence_feature_chromosome_location_temp
  set sfcl_data_zdb_id = (select id from tmp_id)
  where sfcl_data_zdb_id = 'ZDB-SSLP-00515-2';

update sequence_feature_chromosome_location_generated_bkup
  set sfclg_data_zdb_id = (select id from tmp_id)
  where sfclg_data_zdb_id = 'ZDB-SSLP-00515-2';

update sequence_feature_chromosome_location_generated_temp
  set sfclg_data_zdb_id = (select id from tmp_id)
  where sfclg_data_zdb_id = 'ZDB-SSLP-00515-2';

update sequence_feature_chromosome_location_generated
  set sfclg_data_zdb_id = (select id from tmp_id)
  where sfclg_data_zdb_id = 'ZDB-SSLP-00515-2';


set constraints all immediate;
rollback work;

--commit work;

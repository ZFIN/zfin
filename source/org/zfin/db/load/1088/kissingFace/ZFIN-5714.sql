--liquibase formatted sql
--changeset sierra:ZFIN-5714

create temp table tmp_id (id varchar(50))
with no log;

insert into tmp_id (id)
 select get_id('GENE') from single;

insert into zdb_active_data
 select id from tmp_id;

insert into marker (mrkr_zdb_id, mrkr_abbrev, mrkr_name, mrkr_owner, mrkr_type)
  select id, mrkr_abbrev||"sierra", mrkr_name||"sierra", mrkr_owner, mrkr_type
    from tmp_id, marker
    where mrkr_zdb_id = 'ZDB-GENEP-131126-38';

insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id)
  select 'ZDB-GENEP-131126-38', id
    from tmp_id;

update record_attribution
 set recattrib_data_zdb_id = (Select id from tmp_id)
  where recattrib_Data_zdb_id = 'ZDB-GENEP-131126-38';

update all_map_names
  set allmapnm_zdb_id = (Select id from tmp_id)
  where allmapnm_zdb_id = 'ZDB-GENEP-131126-38';

update data_alias
  set dalias_data_zdb_id = (select id from tmp_id)
 where dalias_data_zdb_id = 'ZDB-GENEP-131126-38';

update db_link
  set dblink_linked_recid = (select id from tmp_id)
 where dblink_linked_recid = 'ZDB-GENEP-131126-38';

update expression_experiment2
  set xpatex_gene_zdb_id = (select id from tmp_id)
 where xpatex_gene_zdb_id = 'ZDB-GENEP-131126-38';

update feature_marker_relationship
  set fmrel_mrkr_zdb_id = (select id from tmp_id)
 where fmrel_mrkr_zdb_id = 'ZDB-GENEP-131126-38';

update marker_history
 set mhist_mrkr_zdb_id =  (select id from tmp_id)
 where mhist_mrkr_zdb_id = 'ZDB-GENEP-131126-38';

update marker_history_audit
 set mha_mrkr_zdb_id = (select id from tmp_id)
 where mha_mrkr_zdb_id = 'ZDB-GENEP-131126-38';

update marker_relationship
 set mrel_mrkr_1_zdb_id = (select id from tmp_id)
 where mrel_mrkr_1_zdb_id = 'ZDB-GENEP-131126-38';

update marker_relationship
 set mrel_mrkr_2_zdb_id = (select id from tmp_id)
 where mrel_mrkr_2_zdb_id = 'ZDB-GENEP-131126-38';

update updates
 set rec_id = (select id from tmp_id)
 where rec_id = 'ZDB-GENEP-131126-38';

delete from marker
 where mrkr_zdb_id = 'ZDB-GENEP-131126-38';

update marker
  set mrkr_Abbrev = replace(mrkr_abbrev, 'sierra', '')
 where mrkr_zdb_id = (Select id from tmp_id);

update marker
  set mrkr_name = replace(mrkr_name, 'sierra', '')
 where mrkr_zdb_id = (Select id from tmp_id);

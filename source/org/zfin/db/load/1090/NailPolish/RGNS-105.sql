--liquibase formatted sql
--changeset prita:RGNS-103.sql

update marker_relationship_type
 set  mreltype_mrkr_type_group_1 = 'GENEDOM'
where mreltype_name = 'gene produces transcript';

create temp table tmp_id2 (id varchar(50))
with no log;

insert into tmp_id2 (id)
 select get_id('SCRNAG') from single;

insert into zdb_active_data (zactvd_zdb_id)
 select id from tmp_id2;

insert into marker (mrkr_zdb_id, mrkr_abbrev, mrkr_name, mrkr_owner, mrkr_type)
  select id, mrkr_abbrev||"prita", mrkr_name||"prita", mrkr_owner, mrkr_type
    from tmp_id2, marker
    where mrkr_zdb_id = 'ZDB-GENE-130425-1';

insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id)
  select 'ZDB-GENE-130425-1', id
    from tmp_id2;

update ortholog
 set ortho_zebrafish_gene_zdb_id = (Select id from tmp_id2)
  where ortho_zebrafish_gene_zdb_id = 'ZDB-GENE-130425-1';

update record_attribution
 set recattrib_data_zdb_id = (Select id from tmp_id2)
  where recattrib_Data_zdb_id = 'ZDB-GENE-130425-1';

update all_map_names
  set allmapnm_zdb_id = (Select id from tmp_id2)
  where allmapnm_zdb_id = 'ZDB-GENE-130425-1';

update data_alias
  set dalias_data_zdb_id = (select id from tmp_id2)
 where dalias_data_zdb_id = 'ZDB-GENE-130425-1';

update db_link
  set dblink_linked_recid = (select id from tmp_id2)
 where dblink_linked_recid = 'ZDB-GENE-130425-1';

update expression_experiment
 set xpatex_gene_zdb_id = (select id from tmp_id2)
 where xpatex_gene_zdb_id = 'ZDB-GENE-130425-1';

update expression_experiment2
  set xpatex_gene_zdb_id = (select id from tmp_id2)
 where xpatex_gene_zdb_id = 'ZDB-GENE-130425-1';

update marker_history
 set mhist_mrkr_zdb_id =  (select id from tmp_id2)
 where mhist_mrkr_zdb_id = 'ZDB-GENE-130425-1';

update marker_history_audit
 set mha_mrkr_zdb_id = (select id from tmp_id2)
 where mha_mrkr_zdb_id = 'ZDB-GENE-130425-1';

update marker_relationship
 set mrel_mrkr_1_zdb_id = (select id from tmp_id2)
 where mrel_mrkr_1_zdb_id = 'ZDB-GENE-130425-1';

update marker_relationship
 set mrel_mrkr_2_zdb_id = (select id from tmp_id2)
 where mrel_mrkr_2_zdb_id = 'ZDB-GENE-130425-1';

update updates
 set rec_id = (select id from tmp_id2)
 where rec_id = 'ZDB-GENE-130425-1';

update sequence_feature_chromosome_location_generated
 set sfclg_data_zdb_id = (select id from tmp_id2)
 where sfclg_data_zdb_id = 'ZDB-GENE-130425-1';

update unique_location
 set ul_data_zdb_id = (select id from tmp_id2)
 where ul_data_zdb_id = 'ZDB-GENE-130425-1';

update clean_expression_fast_Search
 set cefs_mrkr_Zdb_id = (select id from tmp_id2)
 where cefs_mrkr_zdb_id = 'ZDB-GENE-130425-1';


delete from marker
 where mrkr_zdb_id = 'ZDB-GENE-130425-1';

update marker
  set mrkr_Abbrev = replace(mrkr_abbrev, 'prita', '')
 where mrkr_zdb_id = (Select id from tmp_id2);

update marker
  set mrkr_name = replace(mrkr_name, 'prita', '')
 where mrkr_zdb_id = (Select id from tmp_id2);

update marker
 set mrkr_type = 'SCRNAG'
 where mrkr_zdb_id = (Select id from tmp_id2);

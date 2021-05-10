--liquibase formatted sql
--changeset sierra:fix_ensdar_name_mapping.sql

alter table ensdart_name_mapping
  rename column zfin_gene_zdb_id to enm_tscript_zdb_id;

alter table ensdart_name_mapping
 rename column ensdart_stable_id to enm_ensdart_stable_id;

alter table ensdart_name_mapping
 rename column ensdart_versioned_id to enm_ensdart_versioned_id;

alter table ensdart_name_mapping
 rename column ensdarg_id to enm_ensdarg_id;

alter table ensdart_name_mapping
 rename column ensembl_tscript_name to enm_ensembl_tscript_name;

alter table ensdart_name_mapping
 rename column ottdart_id to enm_ottdart_id;

create index enm_tscript_Zdb_id_index on ensdart_name_mapping (enm_tscript_zdb_id);

create index enm_ensdarg_gene_stable_id_index on ensdart_name_mapping (enm_ensdart_stable_id);

create index enm_ottdart_id_index on ensdart_name_mapping(enm_ottdart_id);

create index enm_ensembl_tscript_name on ensdart_name_mapping(enm_ensembl_tscript_name);



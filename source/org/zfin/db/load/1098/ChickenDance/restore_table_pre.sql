--liquibase formatted sql
--changeset sierra:restore_table_pre

create table tmp_xpatex (xpatex_zdb_id text,
       xpatex_assay_name text,
       xpatex_probe_feature_zdb_id text,
       xpatex_gene_zdb_id text,
       xpatex_direct_submission_date timestamp without time zone,
       xpatex_dblink_zdb_id text,
       xpatex_genox_zdb_id text,
       xpatex_atb_zdb_id text,
       xpatex_source_zdb_id text)
;


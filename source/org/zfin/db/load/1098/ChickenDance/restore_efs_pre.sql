--liquibase formatted sql
--changeset sierra:restore_efs_pre

create table tmp_efs (efs_pk_id bigint,
                      efs_xpatex_zdb_id text,
                      efs_fig_zdb_id text,
                      efs_start_stg_zdb_id text,
                      efs_end_stg_zdb_id text)
;


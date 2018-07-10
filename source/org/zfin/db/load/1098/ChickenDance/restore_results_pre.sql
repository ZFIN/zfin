--liquibase formatted sql
--changeset sierra:restore_results_pre

create table tmp_xpatres (xpatres_pk_id bigint,
       xpatres_efs_id bigint,
       xpatres_expression_found boolean,
       xpatres_superterm_zdb_id text,
       xpatres_subterm_zdb_id text)
;



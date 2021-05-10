--liquibase formatted sql
--changeset christian:ZFIN2930

alter table expression_term_fast_search
add etfs_xpatres_pk_id int8;

update expression_term_fast_search
set etfs_xpatres_pk_id = etfs_xpatres_zdb_id;

alter table expression_term_fast_search
drop etfs_xpatres_zdb_id;

--liquibase formatted sql
--changeset sierra:removeComments


alter table experiment_condition
 modify (expcond_cdt_zdb_id varchar(50));

alter table experiment_condition
 modify (expcond_zdb_id varchar(50) not null constraint expcond_zdb_id_not_null);

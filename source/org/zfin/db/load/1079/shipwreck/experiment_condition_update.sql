--liquibase formatted sql
--changeset sierra:zeco

alter table experiment_condition
 add (expcond_ao_term_zdb_id varchar(50));

alter table experiment_condition
 add (expcond_go_cc_term_zdb_id varchar(50));


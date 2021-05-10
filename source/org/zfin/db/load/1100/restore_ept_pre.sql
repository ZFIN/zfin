--liquibase formatted sql
--changeset sierra:restore_ept_pre

create table tmp_expression_phenotype_term (
	ept_pk_id int8,
	ept_relational_term text,
	ept_quality_term_zdb_id text,
	ept_tag text,
	ept_xpatres_id int8
);

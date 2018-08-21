--liquibase formatted sql
--changeset sierra:restore_ept_pre

create table tmp_expression_phenotype_term(
	ept_pk_id int8,
	ept_relational_term,
	ept_qualifier_term_zdb_id,
	ept_tag,
	ept_xpatres_id
);

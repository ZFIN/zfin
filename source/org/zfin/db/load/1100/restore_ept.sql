--liquibase formatted sql
--changeset sierra:restore_ept.sql


delete from tmp_expression_phenotype_term
 where ept_pk_id in (select ept_pk_id from expression_phenotype_term);

insert into expression_phenotype_term (ept_pk_id,
	ept_relational_term,
	ept_quality_term_zdb_id,
	ept_tag,
	ept_xpatres_id)
select 
	ept_pk_id,
        ept_relational_term,
        ept_quality_term_zdb_id,
        ept_tag,
        ept_xpatres_id
from tmp_expression_phenotype_term;

--liquibase formatted sql
--changeset sierra:restore_ept.sql

create temp table tmp_expression_phenotype_term (ept_pk_id bigint,
       ept_relational_term text,
       ept_qualifier_term_zdb_id text,
       ept_tag text,
       ept_xpatres_id bigint)
;

\copy tmp_expression_phenotype_term from 'ept_merge.csv' with delimiter ','; 

delete from tmp_expression_phenotype_term
 where ept_pk_id in (select ept_pk_id from expression_phenotype_term);

select * from tmp_expression_phenotype_term;

insert into expression_phenotype_term
 select * from tmp_expression_phenotype_term;

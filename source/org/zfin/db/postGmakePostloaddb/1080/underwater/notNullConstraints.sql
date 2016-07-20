--liquibase formatted sql
--changeset sierra:notNullConstraints

update experiment_condition
 set expcond_zeco_term_zdb_id = (Select term_zdb_id 
     			      		from term
					where term_ont_id = 'ZECO:0000100')
 and expcond_zeco_term_zdb_id is null;

alter table experiment_condition
 modify (expcond_zeco_term_zdb_id varchar(50) not null constraint expcond_zeco_Term_zdb_id_not_null);

--liquibase formatted sql
--changeset sierra:pushToTerm

create temp table tmp_id (new_id varchar(50), term_id varchar(50), old_id varchar(50))
with no log;

insert into tmp_id (new_id, term_id, old_id)
  select get_id('TERM'),
  	 term_id,
	 old_id
 from tmp_term
where term_ont_id like 'CHEBI:%';

create index term_id_index
  on tmp_id (term_id)
 using btree in idxdbs3;


insert into zdb_Active_data
 select new_id from tmp_id;

insert into term (term_zdb_id,
    term_ont_id,
    term_name,
    term_ontology,
    term_is_obsolete,
    term_is_secondary,
    term_is_root,
    term_comment,
    term_definition,
    term_primary_subset_id,
    term_ontology_id,
    term_name_order)
select new_id, 
       term_ont_id,
    term_name,
    term_ontology,
    term_is_obsolete,
    term_is_secondary,
    term_is_root,
    term_comment,
    term_definition,
    term_primary_subset_id,
    term_ontology_id,
    term_name_order
 from tmp_term, tmp_id
 where term_id = term_ont_id;


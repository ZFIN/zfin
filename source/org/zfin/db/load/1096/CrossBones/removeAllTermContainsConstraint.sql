--liquibase formatted sql
--changeset sierra:removeAllTermContainsConstraint.sql

drop table all_term_contains_old;

alter table all_term_contains add constraint
   primary key (alltermcon_container_zdb_id,     
		alltermcon_contained_zdb_id) constraint all_term_contains_primary_key;

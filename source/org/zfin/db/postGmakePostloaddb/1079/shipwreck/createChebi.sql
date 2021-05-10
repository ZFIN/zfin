--liquibase formatted sql
--changeset sierra:createChebi

create table tmp_term (
    term_zdb_id varchar(50),
    term_ont_id varchar(50),
    term_name varchar(255),
    term_ontology varchar(50),
    term_is_obsolete boolean,
    term_is_secondary boolean,
    term_is_root boolean,
    term_definition varchar(255),
    term_primary_subset_id int,
    term_ontology_id varchar(50),
    term_name_order varchar(50),
    extra varchar(10)
    )
in tbldbs2
extent size 4096 next size 4096;

--liquibase formatted sql
--changeset cmpich:ZFIN-9327.sql

create temp table marker_go_temp as
select mrkrgoev_mrkr_zdb_id as id, mrkr_abbrev as name, count(*) as ct from marker_go_term_evidence, term, marker
where mrkrgoev_term_zdb_id = term.term_zdb_id
and term.term_ontology = 'biological_process'
and mrkrgoev_mrkr_zdb_id = mrkr_zdb_id
group by mrkrgoev_mrkr_zdb_id, mrkr_abbrev
order by ct desc limit 10;

select * from marker_go_temp;

create temp table marker_go_records_temp as
select mrkrgoev_mrkr_zdb_id, mrkr_abbrev, mrkrgoev_term_zdb_id, term_name ,mrkrgoev_evidence_code, mrkrgoev_source_zdb_id from marker_go_term_evidence, marker, term, marker_go_temp
    where
    mrkrgoev_term_zdb_id = term_zdb_id AND
    mrkr_zdb_id = mrkrgoev_mrkr_zdb_id AND
    mrkrgoev_mrkr_zdb_id = id;

select mrkrgoev_mrkr_zdb_id, mrkr_abbrev, count(*) as ct from marker_go_records_temp
group by mrkrgoev_mrkr_zdb_id, mrkr_abbrev order by ct desc;


\COPY (SELECT * FROM marker_go_records_temp) TO 'marker_go_many_BP.csv' WITH (FORMAT CSV, DELIMITER E'\t', QUOTE ' ');

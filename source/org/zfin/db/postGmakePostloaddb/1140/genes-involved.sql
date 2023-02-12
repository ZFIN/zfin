--liquibase formatted sql
--changeset cmpich:zfin-8416

drop table if exists UI.OMIM_ZFIN_ASSOCIATION;
Drop table if exists UI.OMIM_PHENOTYPE_DISPLAY;

create table UI.OMIM_PHENOTYPE_DISPLAY
(
    opd_id                       serial8 not null,
    opd_omim_term_name           text,
    opd_omim_accession           text,
    opd_term_zdb_id              text,
    opd_human_gene_id            text,
    opd_zfin_gene_symbols_search text
);

ALTER TABLE UI.OMIM_PHENOTYPE_DISPLAY
    ADD CONSTRAINT OMIM_PHENOTYPE_DISPLAY_un UNIQUE (opd_id);

ALTER TABLE UI.OMIM_PHENOTYPE_DISPLAY
    ADD CONSTRAINT constraint_fk
        FOREIGN KEY (opd_term_zdb_id)
            REFERENCES term (term_zdb_id);


create table UI.OMIM_ZFIN_ASSOCIATION
(
    oza_zfin_gene_zdb_id   text,
    oza_human_phenotype_id integer
);

ALTER TABLE UI.OMIM_ZFIN_ASSOCIATION
    ADD CONSTRAINT constraint_fk1
        FOREIGN KEY (oza_zfin_gene_zdb_id)
            REFERENCES MARKER (mrkr_zdb_id);

ALTER TABLE UI.OMIM_ZFIN_ASSOCIATION
    ADD CONSTRAINT constraint_fk2
        FOREIGN KEY (oza_human_phenotype_id)
            REFERENCES UI.OMIM_PHENOTYPE_DISPLAY (opd_id);


-- all_terms_contains table for disease terms for searching

/*DROP table UI.ALL_TERMS_CONTAINS;

create table UI.ALL_TERMS_CONTAINS
(
    atc_term_zdb_id  text,
    atc_parent_names text
);

insert into UI.ALL_TERMS_CONTAINS
select alltermcon_contained_zdb_id, array_to_string(ARRAY_AGG(parent.term_name),',')
from all_term_contains,
     term as child,
     term as parent
where child.term_zdb_id = alltermcon_contained_zdb_id
  AND child.term_ontology = 'disease_ontology'
  and parent.term_zdb_id = alltermcon_container_zdb_id
and child.term_is_obsolete != 't'
group by alltermcon_contained_zdb_id;
*/

--CREATE EXTENSION pg_trgm;

--CREATE INDEX trgm_idx_UI.OMIM_PHENOTYPE_DISPLAY ON UI.OMIM_PHENOTYPE_DISPLAY USING gin (opd_zfin_gene_symbols_search gin_trgm_ops);

-- https://niallburkley.com/blog/index-columns-for-like-in-postgres/
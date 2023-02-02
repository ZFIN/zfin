--liquibase formatted sql
--changeset cmpich:zfin-8416

drop table UI_OMIM_ZFIN_ASSOCIATION;
Drop table UI_OMIM_PHENOTYPE_DISPLAY;

create table UI_OMIM_PHENOTYPE_DISPLAY
(
    opd_id serial8 not null,
    opd_omim_term_name text,
    opd_term_zdb_id text,
    opd_human_gene_id text,
    opd_zfin_gene_symbol_array text[],
    opd_disease_term_name_array text[]
);

ALTER TABLE UI_OMIM_PHENOTYPE_DISPLAY
    ADD CONSTRAINT OMIM_PHENOTYPE_DISPLAY_un UNIQUE (opd_id);

ALTER TABLE UI_OMIM_PHENOTYPE_DISPLAY
    ADD CONSTRAINT constraint_fk
        FOREIGN KEY (opd_term_zdb_id)
            REFERENCES term(term_zdb_id);


create table UI_OMIM_ZFIN_ASSOCIATION
(
    oza_zfin_gene_zdb_id text,
    oza_human_phenotype_id integer
);

ALTER TABLE UI_OMIM_ZFIN_ASSOCIATION
    ADD CONSTRAINT constraint_fk1
        FOREIGN KEY (oza_zfin_gene_zdb_id)
            REFERENCES MARKER(mrkr_zdb_id);

ALTER TABLE UI_OMIM_ZFIN_ASSOCIATION
    ADD CONSTRAINT constraint_fk2
        FOREIGN KEY (oza_human_phenotype_id)
            REFERENCES UI_OMIM_PHENOTYPE_DISPLAY(opd_id);



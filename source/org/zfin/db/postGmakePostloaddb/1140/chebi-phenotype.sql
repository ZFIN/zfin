--liquibase formatted sql
--changeset cmpich:zfin-8433

drop table IF EXISTS UI.CHEBI_PHENOTYPE_WAREHOUSE_ASSOCIATION;
Drop table IF EXISTS UI.CHEBI_PHENOTYPE_DISPLAY;

create table UI.CHEBI_PHENOTYPE_DISPLAY
(
    cpd_id                                      serial8 not null,
    cpd_term_zdb_id                             text,
    cpd_fish_zdb_id                             text,
    cpd_exp_zdb_id                              text,
    cpd_fig_zdb_id                              text,
    cpd_pub_zdb_id                              text,
    cpd_pub_count                               integer,
    cpd_fig_count                               integer,
    cpd_fish_search                             text,
    cpd_gene_search                             text,
    cpd_phenotype_search                        text,
    cpd_condition_search                        text,
    cpd_is_multi_chebi_condition                boolean,
    cpd_ameliorated_exacerbated_phenotype_search text
);

ALTER TABLE UI.CHEBI_PHENOTYPE_DISPLAY
    ADD CONSTRAINT CHEBI_PHENOTYPE_DISPLAY_un UNIQUE (cpd_id);

ALTER TABLE UI.CHEBI_PHENOTYPE_DISPLAY
    ADD CONSTRAINT constraint_fk2
        FOREIGN KEY (cpd_fig_zdb_id)
            REFERENCES figure (fig_zdb_id);

ALTER TABLE UI.CHEBI_PHENOTYPE_DISPLAY
    ADD CONSTRAINT constraint_fk1
        FOREIGN KEY (cpd_exp_zdb_id)
            REFERENCES experiment (exp_zdb_id);

ALTER TABLE UI.CHEBI_PHENOTYPE_DISPLAY
    ADD CONSTRAINT constraint_fk3
        FOREIGN KEY (cpd_fish_zdb_id)
            REFERENCES fish (fish_zdb_id);

ALTER TABLE UI.CHEBI_PHENOTYPE_DISPLAY
    ADD CONSTRAINT constraint_fk4
        FOREIGN KEY (cpd_pub_zdb_id)
            REFERENCES publication (zdb_id);

ALTER TABLE UI.CHEBI_PHENOTYPE_DISPLAY
    ADD CONSTRAINT constraint_fk5
        FOREIGN KEY (cpd_term_zdb_id)
            REFERENCES term (term_zdb_id);

create table UI.CHEBI_PHENOTYPE_WAREHOUSE_ASSOCIATION
(
    cpwa_phenotype_warehouse_id integer,
    cpwa_phenotype_id           integer
);

/*ALTER TABLE UI.PHENOTYPE_WAREHOUSE_ASSOCIATION
    ADD CONSTRAINT constraint_fk1
        FOREIGN KEY (pwa_phenotype_warehouse_id)
            REFERENCES PHENOTYPE_OBSERVATION_GENERATED (psg_id);
*/
ALTER TABLE UI.CHEBI_PHENOTYPE_WAREHOUSE_ASSOCIATION
    ADD CONSTRAINT constraint_fk2
        FOREIGN KEY (cpwa_phenotype_id)
            REFERENCES UI.CHEBI_PHENOTYPE_DISPLAY (cpd_id);


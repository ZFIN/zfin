--liquibase formatted sql
--changeset cmpich:publication-expression

Drop table IF EXISTS UI.PUBLICATION_EXPRESSION_DISPLAY;

create table UI.PUBLICATION_EXPRESSION_DISPLAY
(
    ped_id               serial8 not null,
    ped_gene_zdb_id      text,
    ped_antibody_zdb_id  text,
    ped_fish_zdb_id      text,
    ped_exp_zdb_id       text,
    ped_fig_zdb_id       text,
    ped_pub_zdb_id       text,
    ped_superterm_zdb_id text,
    ped_subterm_zdb_id   text,
    ped_start_zdb_id     text,
    ped_end_zdb_id       text,
    ped_assay_id         text,
    ped_qualifier        text,
    ped_fish_search      text
);

ALTER TABLE UI.PUBLICATION_EXPRESSION_DISPLAY
    ADD CONSTRAINT PUBLICATION_EXPRESSION_DISPLAY_un UNIQUE (ped_id);

ALTER TABLE UI.PUBLICATION_EXPRESSION_DISPLAY
    ADD CONSTRAINT constraint_fk1
        FOREIGN KEY (ped_exp_zdb_id)
            REFERENCES experiment (exp_zdb_id);

ALTER TABLE UI.PUBLICATION_EXPRESSION_DISPLAY
    ADD CONSTRAINT constraint_fk2
        FOREIGN KEY (ped_fig_zdb_id)
            REFERENCES figure (fig_zdb_id);

ALTER TABLE UI.PUBLICATION_EXPRESSION_DISPLAY
    ADD CONSTRAINT constraint_fk3
        FOREIGN KEY (ped_fish_zdb_id)
            REFERENCES fish (fish_zdb_id);

ALTER TABLE UI.PUBLICATION_EXPRESSION_DISPLAY
    ADD CONSTRAINT constraint_fk4
        FOREIGN KEY (ped_pub_zdb_id)
            REFERENCES publication (zdb_id);

ALTER TABLE UI.PUBLICATION_EXPRESSION_DISPLAY
    ADD CONSTRAINT constraint_fk5
        FOREIGN KEY (ped_antibody_zdb_id)
            REFERENCES marker (mrkr_zdb_id);

ALTER TABLE UI.PUBLICATION_EXPRESSION_DISPLAY
    ADD CONSTRAINT constraint_fk6
        FOREIGN KEY (ped_gene_zdb_id)
            REFERENCES marker (mrkr_zdb_id);

ALTER TABLE UI.PUBLICATION_EXPRESSION_DISPLAY
    ADD CONSTRAINT constraint_fk7
        FOREIGN KEY (ped_subterm_zdb_id)
            REFERENCES term (term_zdb_id);

ALTER TABLE UI.PUBLICATION_EXPRESSION_DISPLAY
    ADD CONSTRAINT constraint_fk11
        FOREIGN KEY (ped_superterm_zdb_id)
            REFERENCES term (term_zdb_id);

ALTER TABLE UI.PUBLICATION_EXPRESSION_DISPLAY
    ADD CONSTRAINT constraint_fk8
        FOREIGN KEY (ped_start_zdb_id)
            REFERENCES Stage (stg_zdb_id);

ALTER TABLE UI.PUBLICATION_EXPRESSION_DISPLAY
    ADD CONSTRAINT constraint_fk9
        FOREIGN KEY (ped_end_zdb_id)
            REFERENCES Stage (stg_zdb_id);

ALTER TABLE UI.PUBLICATION_EXPRESSION_DISPLAY
    ADD CONSTRAINT constraint_fk10
        FOREIGN KEY (ped_assay_id)
            REFERENCES expression_pattern_assay (xpatassay_name);



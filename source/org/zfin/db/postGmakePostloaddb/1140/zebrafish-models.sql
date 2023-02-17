--liquibase formatted sql
--changeset cmpich:zfin-8416-zebrafish-models

drop table if exists UI.ZEBRAFISH_MODELS_EVIDENCE_ASSOCIATION;
drop table if exists UI.ZEBRAFISH_MODELS_CHEBI_ASSOCIATION;
Drop table IF EXISTS UI.ZEBRAFISH_MODELS_DISPLAY;

create table UI.ZEBRAFISH_MODELS_DISPLAY
(
    zmd_id                serial8 not null,
    zmd_term_zdb_id       text,
    zmd_fish_zdb_id       text,
    zmd_experiment_zdb_id text,
    zmd_pub_zdb_id        text,
    zmd_pub_count         integer,
    zmd_evidence_search   text,
    zmd_fish_search       text,
    zmd_condition_search  text
);

ALTER TABLE UI.ZEBRAFISH_MODELS_DISPLAY
    ADD CONSTRAINT ZEBRAFISH_MODELS_DISPLAY_un UNIQUE (zmd_id);

-- ALTER TABLE UI.ZEBRAFISH_MODELS_DISPLAY
--     ADD CONSTRAINT constraint_fk2
--         FOREIGN KEY (zmd_fig_zdb_id)
--             REFERENCES figure (fig_zdb_id);

ALTER TABLE UI.ZEBRAFISH_MODELS_DISPLAY
    ADD CONSTRAINT constraint_fk3
        FOREIGN KEY (zmd_fish_zdb_id)
            REFERENCES fish (fish_zdb_id);

ALTER TABLE UI.ZEBRAFISH_MODELS_DISPLAY
    ADD CONSTRAINT constraint_fk3a
        FOREIGN KEY (zmd_experiment_zdb_id)
            REFERENCES experiment (exp_zdb_id);

ALTER TABLE UI.ZEBRAFISH_MODELS_DISPLAY
    ADD CONSTRAINT constraint_fk4
        FOREIGN KEY (zmd_pub_zdb_id)
            REFERENCES publication (zdb_id);

ALTER TABLE UI.ZEBRAFISH_MODELS_DISPLAY
    ADD CONSTRAINT constraint_fk5
        FOREIGN KEY (zmd_term_zdb_id)
            REFERENCES term (term_zdb_id);


create table UI.ZEBRAFISH_MODELS_EVIDENCE_ASSOCIATION
(
    omea_term_zdb_id         text,
    omea_zebfrafish_model_id integer
);

ALTER TABLE UI.ZEBRAFISH_MODELS_EVIDENCE_ASSOCIATION
    ADD CONSTRAINT constraint_fk1
        FOREIGN KEY (omea_term_zdb_id)
            REFERENCES TERM (term_zdb_id);

ALTER TABLE UI.ZEBRAFISH_MODELS_EVIDENCE_ASSOCIATION
    ADD CONSTRAINT constraint_fk2
        FOREIGN KEY (omea_zebfrafish_model_id)
            REFERENCES UI.ZEBRAFISH_MODELS_DISPLAY (zmd_id);


create table UI.ZEBRAFISH_MODELS_CHEBI_ASSOCIATION
(
    omca_term_zdb_id         text,
    omca_zebfrafish_model_id integer
);

ALTER TABLE UI.ZEBRAFISH_MODELS_CHEBI_ASSOCIATION
    ADD CONSTRAINT constraint_fk1
        FOREIGN KEY (omca_term_zdb_id)
            REFERENCES TERM (term_zdb_id);

ALTER TABLE UI.ZEBRAFISH_MODELS_CHEBI_ASSOCIATION
    ADD CONSTRAINT constraint_fk2
        FOREIGN KEY (omca_zebfrafish_model_id)
            REFERENCES UI.ZEBRAFISH_MODELS_DISPLAY (zmd_id);


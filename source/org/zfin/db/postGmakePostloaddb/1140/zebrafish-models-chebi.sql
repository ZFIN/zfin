--liquibase formatted sql
--changeset cmpich:zfin-8416-zebrafish-models-chebi

drop table if exists UI.CHEBI_ZEBRAFISH_MODELS_EVIDENCE_ASSOCIATION;
Drop table IF EXISTS UI.ZEBRAFISH_MODELS_CHEBI_ASSOCIATION;
Drop table IF EXISTS UI.CHEBI_ZEBRAFISH_MODELS_DISPLAY;

create table UI.CHEBI_ZEBRAFISH_MODELS_DISPLAY
(
    czmd_id                serial8 not null,
    czmd_disease_zdb_id    text,
    czmd_chebi_zdb_id      text,
    czmd_fish_zdb_id       text,
    czmd_experiment_zdb_id text,
    czmd_pub_zdb_id        text,
    czmd_pub_count         integer,
    czmd_evidence_search   text,
    czmd_fish_search       text,
    czmd_condition_search  text
);

ALTER TABLE UI.CHEBI_ZEBRAFISH_MODELS_DISPLAY
    ADD CONSTRAINT ZEBRAFISH_MODELS_DISPLAY_unique UNIQUE (czmd_id);

ALTER TABLE UI.CHEBI_ZEBRAFISH_MODELS_DISPLAY
    ADD CONSTRAINT constraint_fk3
        FOREIGN KEY (czmd_fish_zdb_id)
            REFERENCES fish (fish_zdb_id);

ALTER TABLE UI.CHEBI_ZEBRAFISH_MODELS_DISPLAY
    ADD CONSTRAINT constraint_fk3a
        FOREIGN KEY (czmd_experiment_zdb_id)
            REFERENCES experiment (exp_zdb_id);

ALTER TABLE UI.CHEBI_ZEBRAFISH_MODELS_DISPLAY
    ADD CONSTRAINT constraint_fk4
        FOREIGN KEY (czmd_pub_zdb_id)
            REFERENCES publication (zdb_id);

ALTER TABLE UI.CHEBI_ZEBRAFISH_MODELS_DISPLAY
    ADD CONSTRAINT constraint_fk5
        FOREIGN KEY (czmd_disease_zdb_id)
            REFERENCES term (term_zdb_id);


create table UI.CHEBI_ZEBRAFISH_MODELS_EVIDENCE_ASSOCIATION
(
    comea_term_zdb_id         text,
    comea_zebfrafish_model_id integer
);

ALTER TABLE UI.CHEBI_ZEBRAFISH_MODELS_EVIDENCE_ASSOCIATION
    ADD CONSTRAINT constraint_fk1
        FOREIGN KEY (comea_term_zdb_id)
            REFERENCES TERM (term_zdb_id);

ALTER TABLE UI.CHEBI_ZEBRAFISH_MODELS_EVIDENCE_ASSOCIATION
    ADD CONSTRAINT constraint_fk2
        FOREIGN KEY (comea_zebfrafish_model_id)
            REFERENCES UI.CHEBI_ZEBRAFISH_MODELS_DISPLAY (czmd_id);


create table UI.ZEBRAFISH_MODELS_CHEBI_ASSOCIATION
(
    omca_id                  serial8 not null,
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
            REFERENCES UI.CHEBI_ZEBRAFISH_MODELS_DISPLAY (czmd_id);


--liquibase formatted sql
--changeset cmpich:ZFIN-9606.sql

CREATE INDEX phenotype_source_generated_genox ON phenotype_source_generated
    (pg_genox_zdb_id);

CREATE INDEX phenotype_source_generated_fig ON phenotype_source_generated
    (pg_fig_zdb_id);

CREATE INDEX phenotype_source_generated_start ON phenotype_source_generated
    (pg_start_stg_zdb_id);
CREATE INDEX phenotype_source_generated_end ON phenotype_source_generated
    (pg_end_stg_zdb_id);


ALTER TABLE phenotype_source_generated
    ADD CONSTRAINT constraint_fk1
        FOREIGN KEY (pg_genox_zdb_id)
            REFERENCES fish_experiment (genox_zdb_id);

ALTER TABLE phenotype_source_generated
    ADD CONSTRAINT constraint_fk2
        FOREIGN KEY (pg_fig_zdb_id)
            REFERENCES figure (fig_zdb_id);

ALTER TABLE phenotype_source_generated
    ADD CONSTRAINT constraint_fk3
        FOREIGN KEY (pg_start_stg_zdb_id)
            REFERENCES stage (stg_zdb_id);

ALTER TABLE phenotype_source_generated
    ADD CONSTRAINT constraint_fk4
        FOREIGN KEY (pg_end_stg_zdb_id)
            REFERENCES stage (stg_zdb_id);

ALTER TABLE phenotype_source_generated
    ADD PRIMARY KEY (pg_id);

ALTER TABLE phenotype_observation_generated
    ADD PRIMARY KEY (psg_id);

ALTER TABLE phenotype_observation_generated
    ADD CONSTRAINT phenotype_warehouse_foreign_key FOREIGN KEY (psg_pg_id) REFERENCES phenotype_source_generated (pg_id);

ALTER TABLE phenotype_observation_generated
    ADD CONSTRAINT marker_foreign_key FOREIGN KEY (psg_mrkr_zdb_id) REFERENCES marker (mrkr_zdb_id);

ALTER TABLE phenotype_observation_generated
    ADD CONSTRAINT e1a_foreign_key FOREIGN KEY (psg_e1a_zdb_id) REFERENCES term (term_zdb_id);

ALTER TABLE phenotype_observation_generated
    ADD CONSTRAINT e1b_foreign_key FOREIGN KEY (psg_e1b_zdb_id) REFERENCES term (term_zdb_id);

ALTER TABLE phenotype_observation_generated
    ADD CONSTRAINT e2a_foreign_key FOREIGN KEY (psg_e2a_zdb_id) REFERENCES term (term_zdb_id);

ALTER TABLE phenotype_observation_generated
    ADD CONSTRAINT e2b_foreign_key FOREIGN KEY (psg_e2b_zdb_id) REFERENCES term (term_zdb_id);

ALTER TABLE phenotype_observation_generated
    ADD CONSTRAINT quality_foreign_key FOREIGN KEY (psg_quality_zdb_id) REFERENCES term (term_zdb_id);



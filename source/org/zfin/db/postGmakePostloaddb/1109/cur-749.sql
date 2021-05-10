--liquibase formatted sql
--changeset christian:cur-749

ALTER TABLE experiment_condition
ADD COLUMN expcond_spatial_term_zdb_id text;

ALTER TABLE experiment_condition
    ADD CONSTRAINT expcond_spatial_term_zdb_id_fk FOREIGN KEY (expcond_spatial_term_zdb_id) REFERENCES term (term_zdb_id)
    ON UPDATE RESTRICT ON DELETE RESTRICT;

CREATE INDEX expcond_spatial_term_zdb_id_fk_index ON experiment_condition (expcond_spatial_term_zdb_id);
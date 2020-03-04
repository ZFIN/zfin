--liquibase formatted sql
--changeset cmpich:INF-3717

ALTER TABLE FISH_EXPERIMENT
    ADD CONSTRAINT fish_experiment_experiment_foreign_key FOREIGN KEY (genox_exp_zdb_id) REFERENCES experiment (exp_zdb_id)
    ON UPDATE RESTRICT ON DELETE CASCADE;
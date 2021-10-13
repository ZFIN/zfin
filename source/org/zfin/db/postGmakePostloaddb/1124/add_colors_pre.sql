--liquibase formatted sql
--changeset cmpich:add_colors_pre



drop table if exists fluorescent_protein;
create table fluorescent_protein (
                 fp_pk_id serial PRIMARY KEY,
                 fp_name VARCHAR(100),
                 fp_excitation_length VARCHAR(5),
                 fp_emission_length VARCHAR(5),
                 fp_aliases VARCHAR(200)
);

drop table if exists efg_fluorescence;
create table efg_fluorescence (
                 ef_pk_id serial PRIMARY KEY,
                 ef_mrkr_zdb_id TEXT NOT NULL REFERENCES marker (mrkr_zdb_id),
                 ef_excitation_length VARCHAR(5),
                 ef_emission_length VARCHAR(5)
);

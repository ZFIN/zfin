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

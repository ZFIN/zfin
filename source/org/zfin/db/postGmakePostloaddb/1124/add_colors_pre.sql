--liquibase formatted sql
--changeset cmpich:add_colors_pre


drop table if exists fluorescent_protein CASCADE;
create table fluorescent_protein
(
    fp_pk_id             serial PRIMARY KEY,
    fp_name              VARCHAR(100),
    fp_excitation_length VARCHAR(5),
    fp_emission_length   VARCHAR(5),
    fp_aliases           VARCHAR(200),
    fp_emission_color    VARCHAR(12),
    fp_excitation_color  VARCHAR(12)
);

drop table if exists fluorescent_marker;
create table fluorescent_marker
(
    fm_pk_id             serial PRIMARY KEY,
    fm_mrkr_zdb_id       TEXT NOT NULL REFERENCES marker (mrkr_zdb_id),
    fm_excitation_length INTEGER,
    fm_emission_length   INTEGER,
    fm_emission_color    VARCHAR(12),
    fm_excitation_color  VARCHAR(12)
);

drop table if exists fluorescent_color;
create table fluorescent_color
(
    fc_pk_id            serial PRIMARY KEY,
    fc_color            VARCHAR(12),
    fc_lower_bound      FLOAT,
    fc_upper_bound      FLOAT,
    fc_wave_length_display      VARCHAR(40)
);

insert into fluorescent_color (fc_color, fc_lower_bound, fc_upper_bound, fc_wave_length_display)
values ('violet', 380, 439.5, '380-440 nm');
insert into fluorescent_color (fc_color, fc_lower_bound, fc_upper_bound, fc_wave_length_display)
values ('blue', 439.5, 474.5, '440-475 nm');
insert into fluorescent_color (fc_color, fc_lower_bound, fc_upper_bound, fc_wave_length_display)
values ('cyan', 474.5, 499.5, '475-500 nm');
insert into fluorescent_color (fc_color, fc_lower_bound, fc_upper_bound, fc_wave_length_display)
values ('green', 499.5, 534.5, '500-525 nm');
insert into fluorescent_color (fc_color, fc_lower_bound, fc_upper_bound, fc_wave_length_display)
values ('yellow', 534.5, 554.5, '525-555 nm');
insert into fluorescent_color (fc_color, fc_lower_bound, fc_upper_bound, fc_wave_length_display)
values ('orange', 554.5, 579.5, '555-580 nm');
insert into fluorescent_color (fc_color, fc_lower_bound, fc_upper_bound, fc_wave_length_display)
values ('red', 579.5, 629.5, '580-630 nm');
insert into fluorescent_color (fc_color, fc_lower_bound, fc_upper_bound, fc_wave_length_display)
values ('far red', 629.5, 770, '630-700 nm');

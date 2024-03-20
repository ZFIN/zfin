--liquibase formatted sql
--changeset cmpich:ZFIN-9083.sql

update fluorescent_marker
set (fm_protein_pk_id,
     fm_excitation_length,
     fm_excitation_color,
     fm_emission_length,
     fm_emission_color) =
        (SELECT fp_pk_id,
                fp_excitation_length,
                fp_excitation_color,
                fp_emission_length,
                fp_emission_color
         FROM fluorescent_protein
         WHERE fp_pk_id = 123)
where fm_mrkr_zdb_id = 'ZDB-TGCONSTRCT-190812-12'
  and fm_protein_pk_id = 197;
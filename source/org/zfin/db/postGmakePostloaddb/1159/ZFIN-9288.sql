--liquibase formatted sql
--changeset rtaylor:ZFIN-9288.sql

UPDATE
    fluorescent_marker
SET
    fm_protein_pk_id = 24,
    fm_excitation_length = 505,
    fm_excitation_color = 'green',
    fm_emission_color = 'green',
    fm_emission_length = 515
WHERE
    fm_mrkr_zdb_id = 'ZDB-TGCONSTRCT-190211-3';

INSERT INTO fpprotein_efg
    VALUES ('ZDB-TGCONSTRCT-190211-3', 24);

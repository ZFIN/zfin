--liquibase formatted sql
--changeset cmpich:ZFIN-8269

-- connect new EFG with FP protein
insert into fpprotein_efg (fe_mrkr_zdb_id, fe_fl_protein_id)
VALUES ('ZDB-EFG-221110-1', 569);

-- copy FP protein info into fluorescent_marker table
insert into fluorescent_marker (fm_mrkr_zdb_id, fm_excitation_length, fm_emission_length, fm_protein_pk_id) (
    select fe_mrkr_zdb_id, fp_excitation_length::INTEGER, fp_emission_length::INTEGER, fe_fl_protein_id
    FROM fpprotein_efg,
         fluorescent_protein
    where fe_fl_protein_id = fp_pk_id
      AND fe_mrkr_zdb_id = 'ZDB-EFG-221110-1'
);

-- connect new EFG with FP protein
insert into fpprotein_efg (fe_mrkr_zdb_id, fe_fl_protein_id)
VALUES ('ZDB-EFG-221115-1', 96);

-- copy FP protein info into fluorescent_marker table
insert into fluorescent_marker (fm_mrkr_zdb_id, fm_excitation_length, fm_emission_length, fm_protein_pk_id) (
    select fe_mrkr_zdb_id, fp_excitation_length::INTEGER, fp_emission_length::INTEGER, fe_fl_protein_id
    FROM fpprotein_efg,
         fluorescent_protein
    where fe_fl_protein_id = fp_pk_id
      AND fe_mrkr_zdb_id = 'ZDB-EFG-221115-1'
);

-- generate the color info
select from create_color_info();
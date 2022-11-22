--liquibase formatted sql
--changeset cmpich:ZFIN-8296

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

-- all constructs with sfGFP coding sequence
create temp table tmp_construct (id varchar(50));
insert into tmp_construct
select construct_zdb_id from construct where construct_name ~ 'sfGFP';

-- connect construct with FP protein
insert into fpprotein_construct (fc_mrkr_zdb_id, fc_fl_protein_id)
select id, 96 from tmp_construct;

insert into fluorescent_marker (fm_mrkr_zdb_id, fm_excitation_length, fm_emission_length, fm_protein_pk_id) (
    select fc_mrkr_zdb_id, fp_excitation_length::INTEGER, fp_emission_length::INTEGER, fc_fl_protein_id
    FROM fpprotein_construct,
         fluorescent_protein,
         tmp_construct
    where fc_fl_protein_id = fp_pk_id
      AND fc_mrkr_zdb_id = id
);

create temp table tmp_mrel (id_mrel varchar(50), id_construct varchar(50));

insert into tmp_mrel (id_mrel, id_construct)
select get_id('MREL'), id from tmp_construct;

insert into zdb_active_data (zactvd_zdb_id)
select id_mrel from tmp_mrel;

insert into marker_relationship (mrel_zdb_id, mrel_type, mrel_mrkr_1_zdb_id, mrel_mrkr_2_zdb_id)
  select id_mrel,'coding sequence of', id_construct, 'ZDB-EFG-221115-1' from tmp_mrel;

-- generate the color info
select from create_color_info();
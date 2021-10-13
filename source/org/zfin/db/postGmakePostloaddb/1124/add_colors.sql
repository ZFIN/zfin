--liquibase formatted sql
--changeset cmpich:add_colors


-- define marker - fluorescent-protein association table
drop table  if exists fpProtein_efg;
create table fpProtein_efg
(
    fe_mrkr_zdb_id   VARCHAR(100) NOT NULL REFERENCES marker (mrkr_zdb_id),
    fe_fl_protein_id bigint       NOT NULL REFERENCES fluorescent_protein (fp_pk_id)
);

insert into fpProtein_efg
select distinct mrkr_zdb_id, fp_pk_id
from marker,
     fluorescent_protein
where mrkr_type = 'EFG'
  AND lower(mrkr_name) = lower(fluorescent_protein.fp_name);

insert into efg_fluorescence (ef_mrkr_zdb_id, ef_excitation_length,ef_emission_length) (
    select fe_mrkr_zdb_id, fp_excitation_length, fp_emission_length
    FROM fpprotein_efg, fluorescent_protein
    where fe_fl_protein_id = fp_pk_id
);


--liquibase formatted sql
--changeset cmpich:add_colors


-- define marker - fluorescent-protein association table
drop table if exists fpProtein_efg;
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

update fluorescent_protein
set fp_emission_length = null
where fp_emission_length = '';

update fluorescent_protein
set fp_excitation_length = null
where fp_excitation_length = '';

alter table fluorescent_protein
    alter column fp_emission_length TYPE numeric USING NULLIF(fp_emission_length, '')::numeric;

alter table fluorescent_protein
    alter column fp_excitation_length TYPE numeric USING NULLIF(fp_excitation_length, '')::numeric;

insert into fpProtein_efg
select distinct mrkr_zdb_id, fp_pk_id
from marker,
     fluorescent_protein
where mrkr_type = 'EFG'
  and mrkr_abbrev = 'Bongwoori'
  and fp_name = 'pHluorin,ecliptic';

insert into fpProtein_efg
select distinct mrkr_zdb_id, fp_pk_id
from marker,
     fluorescent_protein
where mrkr_type = 'EFG'
  and mrkr_abbrev = 'SYPHY'
  and fp_name = 'pHluorin,ecliptic';

insert into fpProtein_efg
select distinct mrkr_zdb_id, fp_pk_id
from marker,
     fluorescent_protein
where mrkr_type = 'EFG'
  and mrkr_abbrev = 'mYFP'
  and fp_name = 'EYFP';

insert into fpProtein_efg
select distinct mrkr_zdb_id, fp_pk_id
from marker,
     fluorescent_protein
where mrkr_type = 'EFG'
  and mrkr_abbrev = 'roGFP'
  and fp_name = 'roGFP2';

insert into fpProtein_efg
select distinct mrkr_zdb_id, fp_pk_id
from marker,
     fluorescent_protein
where mrkr_type = 'EFG'
  and mrkr_abbrev = 'mKOFP2'
  and fp_name = 'mKO2';

insert into fpProtein_efg
select distinct mrkr_zdb_id, fp_pk_id
from marker,
     fluorescent_protein
where mrkr_type = 'EFG'
  and mrkr_abbrev = 'YC2'
  and fp_name = 'EYFP-Q69K';

insert into fpProtein_efg
select distinct mrkr_zdb_id, fp_pk_id
from marker,
     fluorescent_protein
where mrkr_type = 'EFG'
  and mrkr_abbrev = 'YC2'
  and fp_name = 'CFP';

insert into fpProtein_efg
select distinct mrkr_zdb_id, fp_pk_id
from marker,
     fluorescent_protein
where mrkr_type = 'EFG'
  and mrkr_abbrev = 'ZsYellow'
  and fp_name = 'ZsYellow1';

insert into fpProtein_efg
select distinct mrkr_zdb_id, fp_pk_id
from marker,
     fluorescent_protein
where mrkr_type = 'EFG'
  and mrkr_abbrev = 'YFP'
  and fp_name = 'EYFP';

insert into fpProtein_efg
select distinct mrkr_zdb_id, fp_pk_id
from marker,
     fluorescent_protein
where mrkr_type = 'EFG'
  and mrkr_abbrev = 'ChR2'
  and fp_name = 'Channelrhodopsin2';

insert into fpProtein_efg
select distinct mrkr_zdb_id, fp_pk_id
from marker,
     fluorescent_protein
where mrkr_type = 'EFG'
  and mrkr_abbrev = 'ReaChR'
  and fp_name = 'Channelrhodopsin2';

insert into fpProtein_efg
select distinct mrkr_zdb_id, fp_pk_id
from marker,
     fluorescent_protein
where mrkr_type = 'EFG'
  and mrkr_abbrev = 'd1EGFP'
  and fp_name = 'EGFP';

insert into fpProtein_efg
select distinct mrkr_zdb_id, fp_pk_id
from marker,
     fluorescent_protein
where mrkr_type = 'EFG'
  and mrkr_abbrev = 'mClover'
  and fp_name = 'mClover1.5';

insert into fpProtein_efg
select distinct mrkr_zdb_id, fp_pk_id
from marker,
     fluorescent_protein
where mrkr_type = 'EFG'
  and mrkr_abbrev = 'copGFP'
  and fp_name = 'ppluGFP2';

insert into fpProtein_efg
select distinct mrkr_zdb_id, fp_pk_id
from marker,
     fluorescent_protein
where mrkr_type = 'EFG'
  and mrkr_abbrev = 'VSFP'
  and fp_name = 'mCitrine';

insert into fpProtein_efg
select distinct mrkr_zdb_id, fp_pk_id
from marker,
     fluorescent_protein
where mrkr_type = 'EFG'
  and mrkr_abbrev = 'VSFP'
  and fp_name = 'mCerulean';

insert into fpProtein_efg
select distinct mrkr_zdb_id, fp_pk_id
from marker,
     fluorescent_protein
where mrkr_type = 'EFG'
  and mrkr_abbrev = 'D3cpv'
  and fp_name = 'ECFP';

insert into fpProtein_efg
select distinct mrkr_zdb_id, fp_pk_id
from marker,
     fluorescent_protein
where mrkr_type = 'EFG'
  and mrkr_abbrev = 'D3cpv'
  and fp_name = 'Venus';

insert into fpProtein_efg
select distinct mrkr_zdb_id, fp_pk_id
from marker,
     fluorescent_protein
where mrkr_type = 'EFG'
  and mrkr_abbrev = 'Crimson'
  and fp_name = 'E2-Crimson';

insert into fpProtein_efg
select distinct mrkr_zdb_id, fp_pk_id
from marker,
     fluorescent_protein
where mrkr_type = 'EFG'
  and mrkr_abbrev = 'cpYFP'
  and fp_name = 'cpEYFP(V68L/Q69K)';

insert into fpProtein_efg
select distinct mrkr_zdb_id, fp_pk_id
from marker,
     fluorescent_protein
where mrkr_type = 'EFG'
  and mrkr_abbrev = 'AmCyan'
  and fp_name = 'amCyan1';

insert into fpProtein_efg
select distinct mrkr_zdb_id, fp_pk_id
from marker,
     fluorescent_protein
where mrkr_type = 'EFG'
  and mrkr_abbrev = 'CD2V'
  and fp_name = 'ECFP';

insert into fpProtein_efg
select distinct mrkr_zdb_id, fp_pk_id
from marker,
     fluorescent_protein
where mrkr_type = 'EFG'
  and mrkr_abbrev = 'CD2V'
  and fp_name = 'Venus';

insert into fpProtein_efg
select distinct mrkr_zdb_id, fp_pk_id
from marker,
     fluorescent_protein
where mrkr_type = 'EFG'
  and mrkr_abbrev = 'mKOFP'
  and fp_name = 'mKO';

insert into fpProtein_efg
select distinct mrkr_zdb_id, fp_pk_id
from marker,
     fluorescent_protein
where mrkr_type = 'EFG'
  and mrkr_abbrev = 'Tomato'
  and fp_name = 'tdTomato';



insert into fluorescent_marker (fm_mrkr_zdb_id, fm_excitation_length, fm_emission_length, fm_protein_pk_id) (
    select fe_mrkr_zdb_id, fp_excitation_length::INTEGER, fp_emission_length::INTEGER, fe_fl_protein_id
    FROM fpprotein_efg,
         fluorescent_protein
    where fe_fl_protein_id = fp_pk_id
);

-- create construct - FP protein associations
-- from EFG - FP protein associations

-- define marker - fluorescent-protein association table
drop table if exists fpProtein_construct;
create table fpProtein_construct
(
    fc_mrkr_zdb_id   VARCHAR(100) NOT NULL REFERENCES marker (mrkr_zdb_id),
    fc_fl_protein_id bigint       NOT NULL REFERENCES fluorescent_protein (fp_pk_id)
);

insert into fpProtein_construct
select distinct mrel_mrkr_1_zdb_id, fe_fl_protein_id
from fpProtein_efg, marker_relationship
where mrel_mrkr_2_zdb_id = fe_mrkr_zdb_id AND
      mrel_type = 'coding sequence of';

-- create construct - fluorescence associations
-- from EFG - fluorescence associations
insert into fluorescent_marker (fm_mrkr_zdb_id, fm_excitation_length, fm_emission_length, fm_emission_color, fm_excitation_color, fm_protein_pk_id)
select distinct mrel_mrkr_1_zdb_id,fm_excitation_length, fm_emission_length, fm_emission_color, fm_excitation_color, fm_protein_pk_id
from fluorescent_marker, marker_relationship
where mrel_mrkr_2_zdb_id = fm_mrkr_zdb_id AND
        mrel_type = 'coding sequence of';

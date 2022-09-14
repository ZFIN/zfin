--liquibase formatted sql
--changeset cmpich:ZFIN-8169

create temp table tmp_id as
select get_id('EXTNOTE')
from single;

insert into zdb_active_data
select * from tmp_id;

insert into external_note (extnote_data_zdb_id, extnote_note, extnote_note_type, extnote_source_zdb_id, extnote_zdb_id)
select 'ZDB-GENO-960809-7','AB larvae failed to produce fibrinogen AaE, due to a mutation in the AaE-specific coding region of fibrinogen a-chain gene (fga). Therefore, the blood coagulation is decreased in AB <p/> Reference: A genetic modifier of venous thrombosis in zebrafish reveals a functional role for fibrinogen AaE in early hemostasis ',
        'genotype', 'ZDB-PUB-201120-76', * from tmp_id;
--liquibase formatted sql
--changeset cmpich:ZFIN-8223

update construct set construct_name = 'Tg(mylpfa:Hsa.TPM3_E151A,myl7:EGFP)'
where construct_zdb_id = 'ZDB-TGCONSTRCT-210915-2';

update marker set mrkr_name = 'Tg(mylpfa:Hsa.TPM3_E151A,myl7:EGFP)', mrkr_abbrev = 'tg(mylpfa:hsa.tpm3_e151a,myl7:egfp)'
where mrkr_zdb_id = 'ZDB-TGCONSTRCT-210915-2';

delete from record_attribution where recattrib_data_zdb_id = 'ZDB-DALIAS-210915-1';

delete from data_alias where dalias_data_zdb_id = 'ZDB-TGCONSTRCT-210915-2';


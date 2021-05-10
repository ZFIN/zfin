--liquibase formatted sql
--changeset pm:CUR-827

update feature_protein_mutation_detail set fpmd_sequence_of_reference_accession_number='XP_001920094.1' where fpmd_zdb_id='ZDB-FPMD-160601-699';
update feature_protein_mutation_detail set fpmd_fdbcont_zdb_id='ZDB-FDBCONT-040412-39' where fpmd_zdb_id='ZDB-FPMD-160601-699';


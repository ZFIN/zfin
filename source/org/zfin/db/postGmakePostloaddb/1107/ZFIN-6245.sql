--liquibase formatted sql
--changeset pm:ZFIN-6245


update feature_dna_mutation_detail set fdmd_deleted_sequence=null where fdmd_feature_zdb_id='ZDB-ALT-140917-9';
update feature_dna_mutation_detail set fdmd_deleted_sequence=null where fdmd_feature_zdb_id='ZDB-ALT-190322-7';
update feature_dna_mutation_detail set fdmd_deleted_sequence=null where fdmd_feature_zdb_id='ZDB-ALT-190322-8';
update feature_dna_mutation_detail set fdmd_deleted_sequence=null where fdmd_feature_zdb_id='ZDB-ALT-190110-5';
update feature_dna_mutation_detail set fdmd_inserted_sequence=null where fdmd_feature_zdb_id='ZDB-ALT-190110-5';
update feature_dna_mutation_detail set fdmd_deleted_sequence=null where fdmd_feature_zdb_id='ZDB-ALT-181113-2';
update feature_dna_mutation_detail set fdmd_inserted_sequence=null where fdmd_feature_zdb_id='ZDB-ALT-181113-2';
update feature_dna_mutation_detail set fdmd_deleted_sequence=null where fdmd_feature_zdb_id='ZDB-ALT-181207-1';






--liquibase formatted sql
--changeset prita:INF-3170

delete  from record_attribution where recattrib_source_zdb_id='ZDB-PUB-961014-809' and recattrib_source_type not like 'standard';
delete  from record_attribution where recattrib_source_zdb_id='ZDB-PUB-961014-809' and recattrib_data_zdb_id in (select genofeat_zdb_id from genotype_feature where genofeat_feature_zdb_id not like 'ZDB-ALT-970801-19');
delete  from record_attribution where recattrib_source_zdb_id='ZDB-PUB-961014-1052' and recattrib_source_type not like 'standard';
delete  from record_attribution where recattrib_source_zdb_id='ZDB-PUB-961014-1052' and recattrib_data_zdb_id in (select genofeat_zdb_id from genotype_feature,feature where genofeat_feature_zdb_id =feature_zdb_id and feature_abbrev not like 'b%');
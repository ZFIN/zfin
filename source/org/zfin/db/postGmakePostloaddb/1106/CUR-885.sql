--liquibase formatted sql
--changeset pm:CUR-885

update feature set feature_abbrev='dp197' where feature_zdb_id='ZDB-ALT-180117-3';
update feature set feature_name='dp197' where feature_zdb_id='ZDB-ALT-180117-3';
update feature set feature_dominant='true' where feature_zdb_id='ZDB-ALT-180117-3';
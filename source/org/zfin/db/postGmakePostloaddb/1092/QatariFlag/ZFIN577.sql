--liquibase formatted sql
--changeset pm:ZFIN577

update feature set feature_type='POINT_MUTATION' where feature_zdb_id in (select feature from tmp_updfeature);
update feature set feature_type='DELETION' where feature_zdb_id='ZDB-ALT-131112-1';
drop table tmp_updfeature;





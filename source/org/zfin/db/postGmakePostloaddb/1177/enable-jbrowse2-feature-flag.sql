--liquibase formatted sql
--changeset rtaylor:enable-jbrowse2-feature-flag

delete from zdb_personal_feature_flag where zpff_flag_name in ('jBrowse', 'jBrowse2');
delete from zdb_feature_flag where zfeatflag_name in ('jBrowse', 'jBrowse2');
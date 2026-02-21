--liquibase formatted sql
--changeset cmpich:remove-jbrowse2-feature-flag

delete from zdb_personal_feature_flag where zpff_flag_name = 'jBrowse2';
delete from zdb_feature_flag where zfeatflag_name = 'jBrowse2';
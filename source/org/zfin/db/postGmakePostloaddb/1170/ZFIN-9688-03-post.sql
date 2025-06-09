--liquibase formatted sql
--changeset rtaylor:ZFIN-9688-01-pre

insert into sequence_feature_chromosome_location_generated (select * from tmp_sfclg);
DROP TABLE tmp_sfclg;

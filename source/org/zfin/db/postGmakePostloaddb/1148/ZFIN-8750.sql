--liquibase formatted sql
--changeset cmpich:ZFIN-8750.sql

delete from feature_tracking
where ft_feature_zdb_id = 'ZDB-ALT-131108-47'
and ft_feature_abbrev = 'mn0599Gt';
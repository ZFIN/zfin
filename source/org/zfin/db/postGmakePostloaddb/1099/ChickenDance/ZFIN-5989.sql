--liquibase formatted sql
--changeset sierra:ZFIN-5989.sql

update feature set (feature_abbrev, feature_name) = (feature_abbrev, feature_name);


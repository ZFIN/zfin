--liquibase formatted sql
--changeset xiang:CUR-71

delete from feature where feature_zdb_id = "ZDB-ALT-120820-2";
delete from fish where fish_zdb_id = "ZDB-FISH-150901-16226";

--liquibase formatted sql
--changeset pm:DLOAD-484_pre

CREATE TABLE tmp_clone_alias (
aliasid VARCHAR(50),
  clonealias VARCHAR(50),
  cloneid VARCHAR(50)
);


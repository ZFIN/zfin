--liquibase formatted sql
--changeset pm:DLOAD-466b_pre

CREATE TABLE tmp_existfeatures (
featureid VARCHAR(50),
genoid varchar(50),
constructid varchar(50),
fishid varchar(50),
fmrel varchar(50),
genofeat varchar(50)
  );


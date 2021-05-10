--liquibase formatted sql
--changeset pm:zkoCRISPRS_pre

CREATE TABLE tmp_feature (
feature_abbrev VARCHAR(50),
  sequence VARCHAR(50),
  tgtgeneid VARCHAR(50),
  tgtgenesymbol VARCHAR(50),
  mutationtype VARCHAR(50),
  pageURL VARCHAR(255)
);


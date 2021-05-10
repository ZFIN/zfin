--liquibase formatted sql
--changeset pkalita:PLC-317_pre

CREATE TABLE tmp_company_country (
  id VARCHAR(50),
  country VARCHAR(50)
);

CREATE TABLE tmp_lab_country (
  id VARCHAR(50),
  country VARCHAR(50)
);

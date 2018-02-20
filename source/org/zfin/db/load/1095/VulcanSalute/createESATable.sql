--liquibase formatted sql
--changeset sierra:createESATable.sql

CREATE TABLE expression_search_anatomy_generated (
  esag_efs_id varchar(100),
  esag_term_name varchar(255),
  esag_is_direct boolean
) fragment by round robin in tbldbs1, tbldbs2, tbldbs3
  extent size 819200  next size 819200;

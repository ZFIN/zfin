--liquibase formatted sql
--changeset sierra:createPubJournalsTable

create table publication_journals
  (
    pub_id varchar(50),
    pub_vol varchar(10),
    pub_year varchar(10),
    journal_title varchar(255),
    journal_issn varchar(100)
  ) in tbldbs1;

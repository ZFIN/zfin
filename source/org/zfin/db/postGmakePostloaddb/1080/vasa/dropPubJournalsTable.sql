--liquibase formatted sql
--changeset sierra:dropPubJournalsTable

drop table publication_journals;

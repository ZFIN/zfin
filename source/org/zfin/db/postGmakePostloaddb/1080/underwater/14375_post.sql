--liquibase formatted sql
--changeset pkalita:dropMutationDetailReferencesTempTable

DROP TABLE tmp_md_file;

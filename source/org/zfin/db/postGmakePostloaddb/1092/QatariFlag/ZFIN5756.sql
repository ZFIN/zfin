--liquibase formatted sql
--changeset pm:ZFIN5756

update  paneled_markers set or_lg=8 where zdb_id='ZDB-GENE-030131-4505';






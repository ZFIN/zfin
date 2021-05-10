--liquibase formatted sql
--changeset pm:PUB-654

update publication set pub_pmc_id='PMC7256946' where zdb_id='ZDB-PUB-200611-9';

update publication set pub_pmc_id='PMC7225520' where zdb_id='ZDB-PUB-200611-10';




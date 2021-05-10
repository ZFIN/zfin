--liquibase formatted sql
--changeset pm:DLOAD-666.sql
--deleting gene attributions from ZDB-PUB-110127-2

delete from record_attribution where recattrib_source_zdb_id='ZDB-PUB-110127-2';





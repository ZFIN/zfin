--liquibase formatted sql
--changeset xiang:JENK-262

update journal set jrnl_name = "Blood", jrnl_abbrev = "Blood" where jrnl_zdb_id = "ZDB-JRNL-050621-365";

delete from zdb_active_source where zactvs_zdb_id = "ZDB-SALIAS-161128-979";

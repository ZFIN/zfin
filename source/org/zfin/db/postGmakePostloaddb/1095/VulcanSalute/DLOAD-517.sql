--liquibase formatted sql
--changeset xshao:DLOAD-517

delete from zdb_active_data where zactvd_zdb_id = "ZDB-ORTHO-150311-5";

delete from zdb_active_data where zactvd_zdb_id = "ZDB-ORTHO-140411-1";


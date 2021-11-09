--liquibase formatted sql
--changeset christian:ZFIN-7637

update journal set jrnl_abbrev = 'Radiobiologiia' where jrnl_zdb_id = 'ZDB-JRNL-050621-1256';

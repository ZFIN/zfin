--liquibase formatted sql
--changeset pm:PUB-486

update journal set jrnl_is_nice='t' where jrnl_zdb_id='ZDB-JRNL-171003-1';
update journal set jrnl_is_nice='t' where jrnl_zdb_id='ZDB-JRNL-110706-2';
update journal set jrnl_is_nice='t' where jrnl_zdb_id='ZDB-JRNL-121105-2';
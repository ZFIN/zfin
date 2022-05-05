--liquibase formatted sql
--changeset cmpich:ZFIN-7979

update journal set jrnl_is_nice = true where jrnl_zdb_id = 'ZDB-JRNL-181003-1';


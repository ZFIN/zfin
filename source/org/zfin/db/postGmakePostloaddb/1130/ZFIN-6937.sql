--liquibase formatted sql
--changeset christian:ZFIN-6937

update data_alias set dalias_alias = REPLACE(dalias_alias,'\u2206','∆') where dalias_alias like '%u2206%';
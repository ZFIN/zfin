--liquibase formatted sql
--changeset cmpich:ZFIN-7584


create table ensdarg_temp
(
    et_id        VARCHAR(100),
    et_reference VARCHAR(100),
    et_variant   VARCHAR(100)
);

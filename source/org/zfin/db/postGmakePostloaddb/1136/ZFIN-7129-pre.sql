--liquibase formatted sql
--changeset cmpich:ZFIN-7129-pre

create table ensdarg_tt
(
    ensdarg varchar(100),
    zfinId  varchar(100)
);

create table ensdarg_tt_no_dups
(
    d_ensdarg  varchar(100),
    d_zfinid   varchar(100),
    d_count    integer,
    d_dblinkid varchar(100)
);


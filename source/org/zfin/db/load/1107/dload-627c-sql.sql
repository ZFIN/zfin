--liquibase formatted sql
--changeset sierra:dload-627c-sql.sql

create table newfeaturedata (
alleleid text not null,
accid text
) ;

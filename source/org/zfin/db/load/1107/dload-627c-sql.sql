--liquibase formatted sql
--changeset sierra:dload-627c-sql.sql
drop table  if exists newfeaturedata;
create table newfeaturedata (
alleleid text not null,
accid text
) ;

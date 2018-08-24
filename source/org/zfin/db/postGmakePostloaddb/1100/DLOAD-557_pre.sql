--liquibase formatted sql
--changeset pm:DLOAD-557_pre

create table cnedata (
 cneid text not null,
        zfishchr text not null,
        cnename text);



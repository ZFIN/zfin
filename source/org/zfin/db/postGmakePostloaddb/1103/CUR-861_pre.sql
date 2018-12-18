--liquibase formatted sql
--changeset pm:CUR-861

create  table constructrel (construct1 text, construct2 text,region text,relationship text);

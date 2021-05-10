--liquibase formatted sql
--changeset pm:CUR-861b_pre

create  table constructreln (construct1 text, construct2 text,region text,relationship text);

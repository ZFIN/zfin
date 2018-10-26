--liquibase formatted sql
--changeset pm:DLOAD-572_pre

create table   dupCRISPR (
calias varchar(50), cname varchar(50),cseq varchar(50),gene varchar(50), zdbid varchar(50),pmid text) ;

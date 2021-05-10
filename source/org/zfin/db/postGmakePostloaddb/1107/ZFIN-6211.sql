--liquibase formatted sql
--changeset pm:ZFIN-6211


delete from gene_description where gd_description='null';





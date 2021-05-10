--liquibase formatted sql
--changeset sierra:alnc_308.sql


delete from gene_description where gd_description = 'null';


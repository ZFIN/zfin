--liquibase formatted sql
--changeset prita:CUR-855.sql

update marker_relationship_type set mreltype_mrkr_type_group_2='GENEDOM' where  mreltype_name='NTR interacts with GENE';
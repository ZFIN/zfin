--liquibase formatted sql
--changeset cmpich:ZFIN-8558.sql

insert into marker_type_group_member ( mtgrpmem_mrkr_type,mtgrpmem_mrkr_type_group)
VALUES ('EFG','GENEDOM_PROD_PROTEIN');

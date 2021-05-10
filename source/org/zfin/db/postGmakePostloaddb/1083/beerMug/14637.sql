--liquibase formatted sql
--changeset prita:14637.sql

insert into  marker_type_group values('GENEDOM_EFG_REGION_KNOCKDOWN','for relationships with constructs');
update  marker_type_group_member  set mtgrpmem_mrkr_type_group='GENEDOM_EFG_REGION_K' where mtgrpmem_mrkr_type_group='GENEDOM_EFG_REGION';
insert into marker_type_group_member values ('CRISPR','GENEDOM_EFG_REGION_K');
insert into marker_type_group_member values ('MRPHLNO','GENEDOM_EFG_REGION_K');
insert into marker_type_group_member values ('TALEN','GENEDOM_EFG_REGION_K');
update  marker_relationship_type  set mreltype_mrkr_type_group_2='GENEDOM_EFG_REGION_K' where mreltype_mrkr_type_group_2='GENEDOM_EFG_REGION';


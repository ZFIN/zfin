--liquibase formatted sql
--changeset sierra:addRelationships

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('GENEDOM_AND_NTR', 'genes, transcribed and non-trascribed regions for use with feature in is_allele_of relations');


insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 values ('GENE','GENEDOM_AND_NTR');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 values ('GENEP','GENEDOM_AND_NTR');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 values ('GENEFAMILY','GENEDOM_AND_NTR');


insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 values ('LINCRNAG','GENEDOM_AND_NTR');


insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 values ('MIRNAG','GENEDOM_AND_NTR');


insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 values ('LNCRNAG','GENEDOM_AND_NTR');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 values ('SRPRNAG','GENEDOM_AND_NTR');


insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 values ('PIRNAG','GENEDOM_AND_NTR');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 values ('SCRNAG','GENEDOM_AND_NTR');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 values ('SNORNAG','GENEDOM_AND_NTR');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 values ('TRNAG','GENEDOM_AND_NTR');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 values ('NCRNAG','GENEDOM_AND_NTR');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 values ('RRNAG','GENEDOM_AND_NTR');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 values ('PROTBS','GENEDOM_AND_NTR');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 values ('NCCR','GENEDOM_AND_NTR');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 values ('TLNRR','GENEDOM_AND_NTR');


insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 values ('BR','GENEDOM_AND_NTR');


insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 values ('BINDSITE','GENEDOM_AND_NTR');


insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 values ('LIGANDBS','GENEDOM_AND_NTR');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 values ('TFBS','GENEDOM_AND_NTR');


insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 values ('EBS','GENEDOM_AND_NTR');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 values ('NCBS','GENEDOM_AND_NTR');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 values ('EMR','GENEDOM_AND_NTR');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 values ('HMR','GENEDOM_AND_NTR');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 values ('MDNAB','GENEDOM_AND_NTR');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 values ('RR','GENEDOM_AND_NTR');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 values ('TRR','GENEDOM_AND_NTR');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 values ('PROMOTER','GENEDOM_AND_NTR');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 values ('ENHANCER','GENEDOM_AND_NTR');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 values ('LCR','GENEDOM_AND_NTR');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 values ('NUCMO','GENEDOM_AND_NTR');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 values ('DNAMO','GENEDOM_AND_NTR');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 values ('RNAMO','GENEDOM_AND_NTR');

update feature_marker_relationship_type
 set fmreltype_mrkr_type_group = 'GENEDOM_AND_NTR'
 where fmreltype_name = 'is allele of';

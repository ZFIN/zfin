--liquibase formatted sql
--changeset sierra:addNTRRelationships

insert into marker_Type_group (mtgrp_name, mtgrp_commentS)
 values ('CRISPR','A group containing only crisprs');

insert into marker_Type_group (mtgrp_name, mtgrp_commentS)
 values ('TALEN','A group containing only talens');

insert into marker_type_group_member (mtgrpmem_mrkr_type,mtgrpmem_mrkr_type_group)
 values ('TALEN','TALEN');

insert into marker_type_group_member (mtgrpmem_mrkr_type,mtgrpmem_mrkr_type_group)
 values ('CRISPR','CRISPR');

update marker_relationship_type
  set mreltype_mrkr_type_Group_1 = 'TRANSCRIPT'
 where mreltype_name  = 'transcript targets gene';

insert into marker_relationship_type (mreltype_name, mreltype_mrkr_type_group_1, mreltype_mrkr_type_group_2, mreltype_1_to_2_comments, mreltype_2_to_1_comments)
  values ('crispr targets region', 'CRISPR', 'NONTSCRBD_REGION', 'Targets', 'Targeted by');

insert into marker_relationship_type (mreltype_name, mreltype_mrkr_type_group_1, mreltype_mrkr_type_group_2, mreltype_1_to_2_comments, mreltype_2_to_1_comments)
  values ('talen targets region', 'TALEN', 'NONTSCRBD_REGION', 'Targets', 'Targeted by');


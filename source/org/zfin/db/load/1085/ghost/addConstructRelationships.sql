--liquibase formatted sql
--changeset sierra:addConstructRelationships

insert into marker_type_group (mtgrp_name, mtgrp_comments)
  values ('CONSTRUCT_COMPONENTS','group containing markers that can be used in promoter and coding sequence relationships to constructs');

select mtgrpmem_mrkr_type, 'CONSTRUCT_COMPONENTS' as grouper
    from marker_type_group_member
 where mtgrpmem_mrkr_type_group = 'GENEDOM_EFG_EREGION_K'
 union
  select mtgrpmem_mrkr_type, 'CONSTRUCT_COMPONENTS' 
    from marker_type_group_member
 where mtgrpmem_mrkr_type_group = 'GENEDOM_AND_NTR'
into temp tmp_markers;

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
 select mtgrpmem_mrkr_type, grouper
   from tmp_markers;

insert into marker_relationship_type(mreltype_name, mreltype_mrkr_type_group_1, mreltype_mrkr_type_group_2, mreltype_1_to_2_comments, mreltype_2_to_1_comments)
 values ('contains region', 'CONSTRUCT', 'CONSTRUCT_COMPONENTS', 'Contains','Contained in');

update marker_relationship
 set mrel_type = 'contains region' 
where mrel_type = 'contains engineered region';

update construct_marker_relationship
 set conmrkrrel_relationship_type = 'contains region'
where conmrkrrel_relationship_type = 'contains engineered region';

delete from marker_relationship_type
 where mreltype_name = 'contains engineered region';

update marker_relationship_type
  set mreltype_mrkr_type_group_2 = 'CONSTRUCT_COMPONENTS'
  where mreltype_mrkr_type_group_2 = 'GENEDOM_EFG_EREGION_K'
 and mreltype_name in ('coding sequence of','promoter of');


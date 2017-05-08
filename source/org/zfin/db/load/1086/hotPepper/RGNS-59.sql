--liquibase formated sql
--changeset sierra:rgns-59

insert into marker_type_Group (mtgrp_name, mtgrp_comments)
 values ('REGION', 'a group containing both region genes and non trascribed regions ala NTRs.');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 select mtgrpmem_mrkr_type, 'REGION'
  from marker_type_group_member
 where mtgrpmem_mrkr_type_group = 'GENEDOM';

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 select mtgrpmem_mrkr_type, 'REGION'
  from marker_type_group_member
 where mtgrpmem_mrkr_type_group = 'NONTSCRBD_REGION';

insert into marker_relationship_type (mreltype_name, 
       	    			      mreltype_mrkr_type_group_1, 
				      mreltype_mrkr_type_group_2, 
				      mreltype_1_to_2_comments, 
				      mreltype_2_to_1_comments)
 values ('interacts with',
 		    'REGION',
		    'REGION',
		    'interacts with',
		    'interacts with');

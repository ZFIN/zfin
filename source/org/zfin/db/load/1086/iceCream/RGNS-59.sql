--liquibase formated sql
--changeset sierra:rgns-59

alter table marker_type_Group
 modify (mtgrp_searchable boolean default 'f' not null constraint mtgrp_searchable_not_null);

alter table marker_type_Group
 modify (mtgrp_display_name varchar(80) not null constraint mtgrp_searchable_not_null);

insert into marker_Type_group (mtgrp_name, mtgrp_comments, mtgrp_searchable, mtgrp_display_name)
  values ('RNAGENE', 'group of genes defined as RNA genes', 'f','RNA Genes');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
 select marker_type, 'RNAGENE'
  from marker_types
 where marker_type like '%G'
and marker_type != 'EFG';


insert into marker_relationship_type (mreltype_name, 
       	    			      mreltype_mrkr_type_group_1, 
				      mreltype_mrkr_type_group_2, 
				      mreltype_1_to_2_comments, 
				      mreltype_2_to_1_comments)
 values ('interacts with',
 		    'NONTSCRBD_REGION',
		    'GENE',
		    'interacts with',
		    'interacts with');

insert into marker_relationship_type (mreltype_name, 
       	    			      mreltype_mrkr_type_group_1, 
				      mreltype_mrkr_type_group_2, 
				      mreltype_1_to_2_comments, 
				      mreltype_2_to_1_comments)
 values ('interacts with',
 		    'NONTSCRBD_REGION',
		    'GENEP',
		    'interacts with',
		    'interacts with');

insert into marker_relationship_type (mreltype_name, 
       	    			      mreltype_mrkr_type_group_1, 
				      mreltype_mrkr_type_group_2, 
				      mreltype_1_to_2_comments, 
				      mreltype_2_to_1_comments)
 values ('interacts with',
 		    'NONTSCRBD_REGION',
		    'NONTSCRBD_REGION',
		    'interacts with',
		    'interacts with');

insert into marker_relationship_type (mreltype_name, 
       	    			      mreltype_mrkr_type_group_1, 
				      mreltype_mrkr_type_group_2, 
				      mreltype_1_to_2_comments, 
				      mreltype_2_to_1_comments)
 values ('interacts with',
 		    'RNAGENE',
		    'GENE',
		    'interacts with',
		    'interacts with');

insert into marker_relationship_type (mreltype_name, 
       	    			      mreltype_mrkr_type_group_1, 
				      mreltype_mrkr_type_group_2, 
				      mreltype_1_to_2_comments, 
				      mreltype_2_to_1_comments)
 values ('interacts with',
 		    'RNAGENE',
		    'GENEP',
		    'interacts with',
		    'interacts with');

insert into marker_relationship_type (mreltype_name, 
       	    			      mreltype_mrkr_type_group_1, 
				      mreltype_mrkr_type_group_2, 
				      mreltype_1_to_2_comments, 
				      mreltype_2_to_1_comments)
 values ('interacts with',
 		    'RNAGENE',
		    'NONTSCRBD_REGION',
		    'interacts with',
		    'interacts with');

insert into marker_relationship_type (mreltype_name, 
       	    			      mreltype_mrkr_type_group_1, 
				      mreltype_mrkr_type_group_2, 
				      mreltype_1_to_2_comments, 
				      mreltype_2_to_1_comments)
 values ('interacts with',
 		    'RNAGENE',
		    'RNAGENE',
		    'interacts with',
		    'interacts with');


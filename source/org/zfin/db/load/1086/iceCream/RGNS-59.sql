--liquibase formated sql
--changeset sierra:rgns-59

<<<<<<< HEAD


alter table marker_type_Group
 modify (mtgrp_display_name varchar(80) not null constraint mtgrp_searchable_not_null);

=======

>>>>>>> e5ed51dbddd3a88b7583f92fc0672cc5737930ee
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
 values ('NTR interacts with GENE',
 		    'NONTSCRBD_REGION',
		    'GENE',
		    'interacts with',
		    'interacts with');

insert into marker_relationship_type (mreltype_name, 
       	    			      mreltype_mrkr_type_group_1, 
				      mreltype_mrkr_type_group_2, 
				      mreltype_1_to_2_comments, 
				      mreltype_2_to_1_comments)
 values ('NTR interacts with GENEP',
 		    'NONTSCRBD_REGION',
		    'GENEP',
		    'interacts with',
		    'interacts with');

insert into marker_relationship_type (mreltype_name, 
       	    			      mreltype_mrkr_type_group_1, 
				      mreltype_mrkr_type_group_2, 
				      mreltype_1_to_2_comments, 
				      mreltype_2_to_1_comments)
 values ('NTR interacts with NTR',
 		    'NONTSCRBD_REGION',
		    'NONTSCRBD_REGION',
		    'interacts with',
		    'interacts with');

insert into marker_relationship_type (mreltype_name, 
       	    			      mreltype_mrkr_type_group_1, 
				      mreltype_mrkr_type_group_2, 
				      mreltype_1_to_2_comments, 
				      mreltype_2_to_1_comments)
 values ('RNAGENE interacts with GENE',
 		    'RNAGENE',
		    'GENE',
		    'interacts with',
		    'interacts with');

insert into marker_relationship_type (mreltype_name, 
       	    			      mreltype_mrkr_type_group_1, 
				      mreltype_mrkr_type_group_2, 
				      mreltype_1_to_2_comments, 
				      mreltype_2_to_1_comments)
 values ('RNAGENE interacts with GENEP',
 		    'RNAGENE',
		    'GENEP',
		    'interacts with',
		    'interacts with');

insert into marker_relationship_type (mreltype_name, 
       	    			      mreltype_mrkr_type_group_1, 
				      mreltype_mrkr_type_group_2, 
				      mreltype_1_to_2_comments, 
				      mreltype_2_to_1_comments)
 values ('RNAGENE interacts with NTR',
 		    'RNAGENE',
		    'NONTSCRBD_REGION',
		    'interacts with',
		    'interacts with');

insert into marker_relationship_type (mreltype_name, 
       	    			      mreltype_mrkr_type_group_1, 
				      mreltype_mrkr_type_group_2, 
				      mreltype_1_to_2_comments, 
				      mreltype_2_to_1_comments)
 values ('RNAGENE interacts with RNAGENE',
 		    'RNAGENE',
		    'RNAGENE',
		    'interacts with',
		    'interacts with');


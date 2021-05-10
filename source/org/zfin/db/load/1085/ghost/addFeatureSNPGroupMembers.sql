--liquibase formatted sql
--changeset sierra:RGNS-33

select mtgrpmem_mrkr_type as type from marker_Type_group_member
 where mtgrpmem_mrkr_type_Group = 'GENEDOM'
into temp tmp_add;

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_Type_group)
  select type, 'FEATURE'
    from tmp_add
 where not exists (select 'x' from marker_type_group_member
       	   	  	  where mtgrpmem_mrkr_type = type
			  	and mtgrpmem_mrkr_type_group = 'FEATURE');

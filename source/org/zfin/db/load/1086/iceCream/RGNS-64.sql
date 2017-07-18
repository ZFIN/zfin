--liquibase formatted sql
--changeset sierra:RGNS-64

insert into marker_type_group (mtgrp_name, mtgrp_comments, mtgrp_searchable, mtgrp_display_name)
 values ('SMALLSEG_NO_ESTCDNA', 'group used to constrict the contains relationship for small segments', 'f','small segments without ESTs and cDNAs');

insert into marker_Type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
  select mtgrpmem_mrkr_type, 'SMALLSEG_NO_ESTCDNA'
    from marker_type_group_member
    where mtgrpmem_mrkr_type_group = 'SMALLSEG'
and mtgrpmem_mrkr_type not in ('EST','CDNA');

update marker_Relationship_type
 set mreltype_mrkr_type_Group_2 = 'SMALLSEG_NO_ESTCDNA'
 where mreltype_name = 'gene contains small segment';

--liquibase formated sql
--changeset kschaper:srch-1052.sql

delete from marker_type_group_member 
where mtgrpmem_mrkr_type = 'EFG' and mtgrpmem_mrkr_type_group = 'SEARCHABLE_GENE';

insert into marker_type_group
(mtgrp_name, mtgrp_display_name, mtgrp_searchable, mtgrp_comments)
values ('SEARCHABLE_EFG', 'Engineered Foriegn Gene', 't', 'Used for marker search');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('EFG','SEARCHABLE_EFG');

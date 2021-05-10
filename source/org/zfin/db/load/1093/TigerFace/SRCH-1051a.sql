--liquibase formatted sql
--changeset kschaper:SRCH-1051a.sql

insert into marker_type_group
(mtgrp_name, mtgrp_display_name, mtgrp_searchable, mtgrp_comments)
values ('SEARCHABLE_CDNA_EST', 'cDNA/EST', 't', 'Used for marker search');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('CDNA','SEARCHABLE_CDNA_EST');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('EST','SEARCHABLE_CDNA_EST');

delete from marker_type_group_member where mtgrpmem_mrkr_type_group = 'SEARCHABLE_CNDA_EST';
delete from marker_type_group where mtgrp_name = 'SEARCHABLE_CNDA_EST';



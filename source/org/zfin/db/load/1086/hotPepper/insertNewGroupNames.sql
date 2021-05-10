--liquibase formated sql
--changeset kschaper:insertNewGroupNames

insert into marker_type_group
(mtgrp_name, mtgrp_display_name, mtgrp_searchable, mtgrp_comments)
values ('SEARCHABLE_ANTIBODY', 'Construct', 't', 'Used for marker search');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('ATB','SEARCHABLE_ANTIBODY');



insert into marker_type_group
(mtgrp_name, mtgrp_display_name, mtgrp_searchable, mtgrp_comments)
values ('SEARCHABLE_CONSTRUCT', 'Construct', 't', 'Used for marker search');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('TGCONSTRCT','SEARCHABLE_CONSTRUCT');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('ETCONSTRCT','SEARCHABLE_CONSTRUCT');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('GTCONSTRCT','SEARCHABLE_CONSTRUCT');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('PTCONSTRCT','SEARCHABLE_CONSTRUCT');




insert into marker_type_group
(mtgrp_name, mtgrp_display_name, mtgrp_searchable, mtgrp_comments)
values ('SEARCHABLE_SMALL_SEGMENT', 'Small Segment', 't', 'Used for marker search');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('SSLP','SEARCHABLE_SMALL_SEGMENT');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('RAPD','SEARCHABLE_SMALL_SEGMENT');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('STS','SEARCHABLE_SMALL_SEGMENT');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('BAC_END','SEARCHABLE_SMALL_SEGMENT');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('PAC_END','SEARCHABLE_SMALL_SEGMENT');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('CDNA','SEARCHABLE_SMALL_SEGMENT');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('EST','SEARCHABLE_SMALL_SEGMENT');





insert into marker_type_group
(mtgrp_name, mtgrp_display_name, mtgrp_searchable, mtgrp_comments)
values ('SEARCHABLE_CLONE', 'Clone', 't', 'Used for marker search');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('BAC','SEARCHABLE_CLONE');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('PAC','SEARCHABLE_CLONE');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('FOSMID','SEARCHABLE_CLONE');




insert into marker_type_group
(mtgrp_name, mtgrp_display_name, mtgrp_searchable, mtgrp_comments)
values ('SEARCHABLE_TRANSCRIPT', 'Transcript', 't', 'Used for marker search');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('TSCRIPT','SEARCHABLE_TRANSCRIPT');





insert into marker_type_group
(mtgrp_name, mtgrp_display_name, mtgrp_searchable, mtgrp_comments)
values ('SEARCHABLE_GENE', 'Gene', 't', 'Used for marker search');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('GENE','SEARCHABLE_GENE');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('GENEP','SEARCHABLE_GENE');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('GENEFAMILY','SEARCHABLE_GENE');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('MIRNAG','SEARCHABLE_GENE');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('LINCRNAG','SEARCHABLE_GENE');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('LNCRNAG','SEARCHABLE_GENE');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('PIRNAG','SEARCHABLE_GENE');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('RRNAG','SEARCHABLE_GENE');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('SCRNAG','SEARCHABLE_GENE');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('SNORNAG','SEARCHABLE_GENE');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('SRPRNAG','SEARCHABLE_GENE');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('TRNAG','SEARCHABLE_GENE');






insert into marker_type_group
(mtgrp_name, mtgrp_display_name, mtgrp_searchable, mtgrp_comments)
values ('SEARCHABLE_REGION', 'Region', 't', 'Used for marker search');



insert into marker_type_group
(mtgrp_name, mtgrp_display_name, mtgrp_searchable, mtgrp_comments)
values ('SEARCHABLE_STR', 'Sequence Targeting Reagent', 't', 'Used for marker search');


insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('CRISPR','SEARCHABLE_STR');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('MRPHLNO','SEARCHABLE_STR');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('TALEN','SEARCHABLE_STR');


--insert into marker_type_group (mtgrp_name, mtgrp_display_name, mtgrp_searchable, mtgrp_comments)
--values ('SEARCHABLE_ANTIBODY', 'Antibody', 't', 'Used for marker search');

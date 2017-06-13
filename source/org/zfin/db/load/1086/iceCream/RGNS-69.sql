--liquibase formated sql
--changeset kevin:rgns-69

insert into marker_type_group
(mtgrp_name, mtgrp_display_name, mtgrp_searchable, mtgrp_comments)
values ('GENE_SUPER_TYPE', 'Gene', 'f', 'Used for type tree in faceted search');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('GENE','GENE');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('GENEP','GENE');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('EFG','GENE');




insert into marker_type_group
(mtgrp_name, mtgrp_display_name, mtgrp_searchable, mtgrp_comments)
values ('NON_PROTEIN_CODING_GENE', 'Non-protein coding gene', 'f', 'Used for type tree in faceted search');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('LINCRNAG','NON_PROTEIN_CODING_GENE');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('LNCRNAG','NON_PROTEIN_CODING_GENE');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('MIRNAG','NON_PROTEIN_CODING_GENE');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('NCRNAG','NON_PROTEIN_CODING_GENE');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('PIRNAG','NON_PROTEIN_CODING_GENE');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('RRNAG','NON_PROTEIN_CODING_GENE');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('SCRNAG','NON_PROTEIN_CODING_GENE');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('SNORNAG','NON_PROTEIN_CODING_GENE');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('SRPRNAG','NON_PROTEIN_CODING_GENE');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('TRNAG','NON_PROTEIN_CODING_GENE');


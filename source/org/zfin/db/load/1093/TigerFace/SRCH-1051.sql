--liquibase formatted sql
--changeset kschaper:SRCH-1051.sql

delete from marker_type_group_member where mtgrpmem_mrkr_type_group like 'SEARCHABLE%';

delete from marker_type_group where mtgrp_name like 'SEARCHABLE%';



insert into marker_type_group
(mtgrp_name, mtgrp_display_name, mtgrp_searchable, mtgrp_comments)
values ('SEARCHABLE_PROTEIN_CODING_GENE', 'Protein Coding Gene', 't', 'Used for marker search');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('GENE','SEARCHABLE_PROTEIN_CODING_GENE');



insert into marker_type_group
(mtgrp_name, mtgrp_display_name, mtgrp_searchable, mtgrp_comments)
values ('SEARCHABLE_NON_PROTEIN_CODING_GENE', 'Non-Protein Coding Gene', 't', 'Used for marker search');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('SCRNAG','SEARCHABLE_NON_PROTEIN_CODING_GENE');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('LINCRNAG','SEARCHABLE_NON_PROTEIN_CODING_GENE');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('LNCRNAG','SEARCHABLE_NON_PROTEIN_CODING_GENE');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('RRNAG','SEARCHABLE_NON_PROTEIN_CODING_GENE');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('PIRNAG','SEARCHABLE_NON_PROTEIN_CODING_GENE');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('GENEP','SEARCHABLE_NON_PROTEIN_CODING_GENE');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('MIRNAG','SEARCHABLE_NON_PROTEIN_CODING_GENE');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('TRNAG','SEARCHABLE_NON_PROTEIN_CODING_GENE');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('SNORNAG','SEARCHABLE_NON_PROTEIN_CODING_GENE');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('SRPRNAG','SEARCHABLE_NON_PROTEIN_CODING_GENE');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('NCRNAG','SEARCHABLE_NON_PROTEIN_CODING_GENE');


insert into marker_type_group
(mtgrp_name, mtgrp_display_name, mtgrp_searchable, mtgrp_comments)
values ('SEARCHABLE_EFG', 'Foreign Gene', 't', 'Used for marker search');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('EFG','SEARCHABLE_EFG');


insert into marker_type_group
(mtgrp_name, mtgrp_display_name, mtgrp_searchable, mtgrp_comments)
values ('SEARCHABLE_STR', 'Sequence Targeting Reagent', 't', 'Used for marker search');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('CRISPR','SEARCHABLE_STR');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('MRPHLNO','SEARCHABLE_STR');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('TALEN','SEARCHABLE_STR');

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
values ('SEARCHABLE_NTR', 'Non-Transcribed Region', 't', 'Used for marker search');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('EMR','SEARCHABLE_NTR');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('EBS','SEARCHABLE_NTR');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('DNAMO','SEARCHABLE_NTR');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('BR','SEARCHABLE_NTR');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('HMR','SEARCHABLE_NTR');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('LCR','SEARCHABLE_NTR');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('LIGANDBS','SEARCHABLE_NTR');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('MDNAB','SEARCHABLE_NTR');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('BINDSITE','SEARCHABLE_NTR');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('RNAMO','SEARCHABLE_NTR');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('TRR','SEARCHABLE_NTR');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('TLNRR','SEARCHABLE_NTR');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('TFBS','SEARCHABLE_NTR');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('NUCMO','SEARCHABLE_NTR');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('PROMOTER','SEARCHABLE_NTR');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('NCBS','SEARCHABLE_NTR');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('NCCR','SEARCHABLE_NTR');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('ENHANCER','SEARCHABLE_NTR');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('RR','SEARCHABLE_NTR');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('PROTBS','SEARCHABLE_NTR');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('CPGISLAND','SEARCHABLE_NTR');


insert into marker_type_group
(mtgrp_name, mtgrp_display_name, mtgrp_searchable, mtgrp_comments)
values ('SEARCHABLE_TRANSCRIPT', 'Transcript', 't', 'Used for marker search');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('TSCRIPT','SEARCHABLE_TRANSCRIPT');


insert into marker_type_group
(mtgrp_name, mtgrp_display_name, mtgrp_searchable, mtgrp_comments)
values ('SEARCHABLE_CNDA_EST', 'cDNA/EST', 't', 'Used for marker search');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('CDNA','SEARCHABLE_CNDA_EST');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('EST','SEARCHABLE_CNDA_EST');


insert into marker_type_group
(mtgrp_name, mtgrp_display_name, mtgrp_searchable, mtgrp_comments)
values ('SEARCHABLE_GENOMIC_CLONE', 'Genomic Clone', 't', 'Used for marker search');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('BAC','SEARCHABLE_GENOMIC_CLONE');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('PAC','SEARCHABLE_GENOMIC_CLONE');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('FOSMID','SEARCHABLE_GENOMIC_CLONE');



insert into marker_type_group
(mtgrp_name, mtgrp_display_name, mtgrp_searchable, mtgrp_comments)
values ('SEARCHABLE_MAPPING_MARKER', 'Mapping Marker', 't', 'Used for marker search');

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('BAC_END','SEARCHABLE_MAPPING_MARKER');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('PAC_END','SEARCHABLE_MAPPING_MARKER');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('RAPD','SEARCHABLE_MAPPING_MARKER');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('SSLP','SEARCHABLE_MAPPING_MARKER');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('STS','SEARCHABLE_MAPPING_MARKER');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('SNP','SEARCHABLE_MAPPING_MARKER');


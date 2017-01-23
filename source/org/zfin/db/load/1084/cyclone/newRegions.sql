--liquibase formatted sql
--changeset sierra:newRegions

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('LNCRNA','2','');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('LINCRNA','2','');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('MIRNA','2','');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('PIRNA','2','');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('SCRNA','2','');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('SNORNA','2','');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('TRNA','2','');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('RRNA','2','');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('CNCREGION','22','');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('HISTBS','22','');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('PROTBS','22','');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('CPGISLAND','22','');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('SRPRNA','22','');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('TSCRIPTREGREGION','22','');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('TSCRIPTREGREGION', 'transcript region');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('SRPRNA', 'srp_rna');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('TRNA', 't_rna');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('RRNA', 'r_rna');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('CNCREGION', 'cn_region');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('HISTBS', 'histone binding site');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('PROTBS', 'protein binding site');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('CPGISLAND', 'cpg_island');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('LNCRNA', 'lnc_rna');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('LINCRNA', 'li_rna');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('MIRNA', 'mi_rna');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('PIRNA', 'pi_rna');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('SCRNA', 'sc_rna');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('SNORNA', 'sno_rna');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('ENGINEERED_REGION', 'Group containing engineered regions.');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('GENEDOM_PRODUCES_PROTEIN', 'Group containing transcribed elements that produce proteins.');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('NONTRANSCRIBED_REGIONS','Group containing non-transcribed regions.');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('GENE','GENEDOM_PRODUCES_PROTEIN');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('GENEP','GENEDOM_PRODUCES_PROTEIN');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('GENEFAMILY','GENEDOM_PRODUCES_PROTEIN');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
 values ('TSCRIPT','GENEDOM_PRODUCES_PROTEIN');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('SRPRNA','NONTRANSCRIBED_REGIONS');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('TSCRIPTREGREGION','NONTRANSCRIBED_REGIONS');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('PROTBS','NONTRANSCRIBED_REGIONS');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('CPGISLAND','NONTRANSCRIBED_REGIONS');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('CNCREGION','NONTRANSCRIBED_REGIONS');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('HISTBS','NONTRANSCRIBED_REGIONS');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('LNCRNA','GENEDOM');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('LINCRNA','GENEDOM');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('MIRNA','GENEDOM');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('PIRNA','GENEDOM');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('SCRNA','GENEDOM');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('SNORNA','GENEDOM');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('TRNA','GENEDOM');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('RRNA','GENEDOM');

update marker_relationship_type 
 set mreltype_mrkr_type_group_1 = 'GENEDOM_PRODUCES_PROTEIN'
where mreltype_name = 'gene product recognized by antibody'; 

update marker_relationship_type 
 set mreltype_mrkr_type_group_1 = 'NONTRANSCRIBED_REGION'
where mreltype_name = 'clone contains gene'; 

update marker_relationship_type 
 set mreltype_mrkr_type_group_1 = 'GENEDOM_PRODUCES_PROTEIN'
where mreltype_name = 'gene encodes small segment'; 

update marker_relationship_type 
 set mreltype_mrkr_type_group_1 = 'NONTRANSCRIBED_REGION'
where mreltype_name = 'gene contains small segment'; 

update marker_relationship_type 
 set mreltype_mrkr_type_group_1 = 'NONTRANSCRIBED_REGION'
where mreltype_name = 'gene has artifact'; 

update marker_relationship_type 
 set mreltype_mrkr_type_group_1 = 'NONTRANSCRIBED_REGION'
where mreltype_name = 'gene hybridized by small segment'; 

update marker_relationship_type 
 set mreltype_mrkr_type_group_1 = 'NONTRANSCRIBED_REGION'
where mreltype_name = 'knockdown reagen targets gene'; 

update marker_relationship_type 
 set mreltype_mrkr_type_group_1 = 'GENEDOM_PRODUCES_PROTEIN'
where mreltype_name = 'transcript targets gene'; 

update marker_relationship_type 
 set mreltype_mrkr_type_group_1 = 'GENEDOM_PRODUCES_PROTEIN'
where mreltype_name = 'gene produces transcript'; 


--update marker_relationship_type 
-- set mreltype_mrkr_type_group_2 = 'ENGINEERED_REGION'
--where mreltype_name = 'contains engineered region'; 

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('LNCRNA','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('LINCRNA','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('MIRNA','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('PIRNA','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('SCRNA','SEARCH_MKSEG');


insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('SNORNA','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('TRNA','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('RRNA','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('HISTBS','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('PROTBS','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('CPGISLAND','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('CNCREGION','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('SRPRNA','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('TSCRIPTREGREGION','SEARCH_MKSEG');


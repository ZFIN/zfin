--liquibase formatted sql
--changeset sierra:newRegions

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('LNCRNAG','2','LncRNA Gene');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('LINCRNAG','2','LincRNA Gene');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('MIRNAG','2','miRNA Gene');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('PIRNAG','2','piRNA Gene');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('SCRNAG','2','scRNA Gene');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('SNORNAG','2','snoRNA Gene');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('TRNAG','2','tRNA Gene');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('RRNAG','2','rRNA Gene');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('NCRNAG','22','ncRNA Gene');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('HISTBS','22','Histone Binding Site');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('PROTBS','22','Protein Binding Site');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('CPGISLAND','22','CpG island');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('SRPRNA','22','srpRNA');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('TSCRIPTREGREGION','22','Transcript Regulatory Region');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('TSCRIPTREGREGION', 'transcript region');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('SRPRNA', 'srp_rna');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('TRNAG', 't_rna');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('RRNAG', 'r_rna');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('NCRNAG','ncRNA');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('HISTBS', 'histone binding site');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('PROTBS', 'protein binding site');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('CPGISLAND', 'cpg_island');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('LNCRNAG', 'lnc_rna');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('LINCRNAG', 'li_rna');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('MIRNAG', 'mi_rna');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('PIRNAG', 'pi_rna');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('SCRNAG', 'sc_rna');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('SNORNAG', 'sno_rna');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('ENGINEERED_REGION', 'Group containing engineered regions.');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('GENEDOM_PROD_PROTEIN', 'Group containing transcribed elements that produce proteins.');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('NONTSCRBD_REGIONS','Group containing non-transcribed regions.');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('GENE','GENEDOM_PROD_PROTEIN');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('GENEP','GENEDOM_PROD_PROTEIN');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('GENEFAMILY','GENEDOM_PROD_PROTEIN');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
 values ('TSCRIPT','GENEDOM_PROD_PROTEIN');



insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('TSCRIPTREGREGION','NONTSCRBD_REGIONS');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('PROTBS','NONTSCRBD_REGIONS');


insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('LNCRNAG',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence lncrna_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('LINCRNAG',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence lincrna_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('MIRNAG',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence mirna_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('PIRNAG',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence pirna_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('RRNAG',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence rrna_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('SNORNAG',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence snorna_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('SCRNAG',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence scrna_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('TRNAG',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence trna_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('NCRNAG',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence ncrna_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('SRPRNAG',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence srprna_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;


insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('HISTBS','NONTSCRBD_REGIONS');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('LNCRNAG','GENEDOM');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('LINCRNAG','GENEDOM');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('MIRNAG','GENEDOM');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('SRPRNAG','GENEDOM');


insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('PIRNAG','GENEDOM');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('SCRNAG','GENEDOM');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('SNORNAG','GENEDOM');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('TRNAG','GENEDOM');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('NCRNAG','GENEDOM');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('RRNAG','GENEDOM');

update marker_relationship_type 
 set mreltype_mrkr_type_group_1 = 'GENEDOM_PROD_PROTEIN'
where mreltype_name = 'gene product recognized by antibody'; 

update marker_relationship_type 
 set mreltype_mrkr_type_group_1 = 'NONTSCRBD_REGIONS'
where mreltype_name = 'clone contains gene'; 

update marker_relationship_type 
 set mreltype_mrkr_type_group_1 = 'GENEDOM_PROD_PROTEIN'
where mreltype_name = 'gene encodes small segment'; 

update marker_relationship_type 
 set mreltype_mrkr_type_group_1 = 'NONTSCRBD_REGIONS'
where mreltype_name = 'gene contains small segment'; 

update marker_relationship_type 
 set mreltype_mrkr_type_group_1 = 'NONTSCRBD_REGIONS'
where mreltype_name = 'gene has artifact'; 

update marker_relationship_type 
 set mreltype_mrkr_type_group_1 = 'NONTSCRBD_REGIONS'
where mreltype_name = 'gene hybridized by small segment'; 

update marker_relationship_type 
 set mreltype_mrkr_type_group_1 = 'NONTSCRBD_REGIONS'
where mreltype_name = 'knockdown reagen targets gene'; 


update marker_relationship_type 
 set mreltype_mrkr_type_group_1 = 'GENEDOM_PROD_PROTEIN'
where mreltype_name = 'transcript targets gene'; 

update marker_relationship_type 
 set mreltype_mrkr_type_group_1 = 'GENEDOM_PROD_PROTEIN'
where mreltype_name = 'gene produces transcript'; 


--update marker_relationship_type 
-- set mreltype_mrkr_type_group_2 = 'ENGINEERED_REGION'
--where mreltype_name = 'contains engineered region'; 

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('LNCRNAG','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('LINCRNAG','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('NCRNAG','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('MIRNAG','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('PIRNAG','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('SCRNAG','SEARCH_MKSEG');


insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('SNORNAG','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('TRNAG','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('RRNAG','SEARCH_MKSEG');


insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('LNCRNAG','GENEDOM_EFG_REGION_K');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('LINCRNAG','GENEDOM_EFG_REGION_K');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('NCRNAG','GENEDOM_EFG_REGION_K');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('MIRNAG','GENEDOM_EFG_REGION_K');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('PIRNAG','GENEDOM_EFG_REGION_K');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('SCRNAG','GENEDOM_EFG_REGION_K');


insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('SNORNAG','GENEDOM_EFG_REGION_K');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('TRNAG','GENEDOM_EFG_REGION_K');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('RRNAG','GENEDOM_EFG_REGION_K');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('HISTBS','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('PROTBS','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('CPGISLAND','SEARCH_MKSEG');

/*insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('CNCREGION','SEARCH_MKSEG');*/

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('SRPRNAG','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('TSCRIPTREGREGION','SEARCH_MKSEG');


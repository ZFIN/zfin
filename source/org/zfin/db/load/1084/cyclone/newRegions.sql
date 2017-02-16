--liquibase formatted sql
--changeset sierra:newRegions

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('LNCRNA','2','LncRNA');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('LINCRNA','2','LincRNA');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('MIRNA','2','miRNA');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('PIRNA','2','piRNA');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('SCRNA','2','scRNA');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('SNORNA','2','snoRNA');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('TRNA','2','tRNA');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('RRNA','2','rRNA');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('NCRNA','22','ncRNA');

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
 values ('TRNA', 't_rna');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('RRNA', 'r_rna');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('NCRNA','ncRNA');

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
 values ('GENEDOM_PROD_PROTEIN', 'Group containing transcribed elements that produce proteins.');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('NONTRANSCRIBED_REGIONS','Group containing non-transcribed regions.');

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
values ('TSCRIPTREGREGION','NONTRANSCRIBED_REGIONS');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('PROTBS','NONTRANSCRIBED_REGIONS');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('CPGISLAND','NONTRANSCRIBED_REGIONS');



insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('LNCRNA',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence lncrna_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('LINCRNA',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence lincrna_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('MIRNA',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence mirna_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('PIRNA',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence pirna_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('RRNA',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence rrna_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('SNORNA',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence snorna_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('SCRNA',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence scrna_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('TRNA',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence trna_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('NCRNA',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence ncrna_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('SRPRNA',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence srprna_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;


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
values ('SRPRNA','GENEDOM');


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
values ('NCRNA','GENEDOM');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('RRNA','GENEDOM');

update marker_relationship_type 
 set mreltype_mrkr_type_group_1 = 'GENEDOM_PROD_PROTEIN'
where mreltype_name = 'gene product recognized by antibody'; 

update marker_relationship_type 
 set mreltype_mrkr_type_group_1 = 'NONTRANSCRIBED_REGION'
where mreltype_name = 'clone contains gene'; 

update marker_relationship_type 
 set mreltype_mrkr_type_group_1 = 'GENEDOM_PROD_PROTEIN'
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
values ('LNCRNA','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('LINCRNA','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('NCRNA','SEARCH_MKSEG');

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

/*insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('CNCREGION','SEARCH_MKSEG');*/

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('SRPRNA','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('TSCRIPTREGREGION','SEARCH_MKSEG');


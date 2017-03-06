--liquibase formatted sql
--changeset sierra:newRegions

alter table marker_type_group_member
 drop constraint mtgrpmem_mrkr_type_group_foreign_key;

drop index mtgrpmem_mrkr_type_foreign_key_index;

alter table marker_relationship_type
drop constraint mreltype_mrkr_type_group_2_foreign_key;

alter table marker_relationship_type
drop constraint mreltype_mrkr_type_group_1_foreign_key;

drop index mreltype_mrkr_type_group_1_index;
drop index mreltype_mrkr_type_group_2_index;

alter table marker_type_group
 modify (mtgrp_name varchar(60) not null constraint mtgrp_name_not_null);

    
alter table marker_type_group add constraint primary 
    key (mtgrp_name) constraint marker_type_group_primary_key 
     ;

--create index mtgrpmem_mrkr_type_foreign_key_index 
--    on marker_type_group_member (mtgrpmem_mrkr_type) 
--    using btree  in idxdbs1;

alter table marker_type_group_member 
 modify (mtgrpmem_mrkr_type_group varchar(60) not null constraint mtgrpmem_mrkr_type_group_not_null);

alter table marker_type_group_member add constraint 
    (foreign key (mtgrpmem_mrkr_type_group) references marker_type_group constraint mtgrpmem_mrkr_type_group_foreign_key);



alter table marker_relationship_type 
 modify (mreltype_mrkr_type_group_1 varchar(60) not null constraint mreltype_mrkr_type_group_1_not_null);

alter table marker_relationship_type 
 modify (mreltype_mrkr_type_group_2 varchar(60) not null constraint mreltype_mrkr_type_group_2_not_null);

create index mreltype_mrkr_type_group_1_index on marker_relationship_type (mreltype_mrkr_type_group_1) using 
    btree  in idxdbs2;
create index mreltype_mrkr_type_group_2_index on marker_relationship_type (mreltype_mrkr_type_group_2) using 
    btree  in idxdbs2;

alter table marker_relationship_type add constraint 
    (foreign key (mreltype_mrkr_type_group_2) references
    marker_type_group  constraint mreltype_mrkr_type_group_2_foreign_key);
    
alter table marker_relationship_type add constraint 
    (foreign key (mreltype_mrkr_type_group_1) references 
   marker_type_group  constraint mreltype_mrkr_type_group_1_foreign_key);


alter table feature_marker_relationship_type
  modify (fmreltype_mrkr_type_group varchar(60) not null constraint fmreltype_mrkr_type_group_not_null);


alter table feature_marker_relationship_type add constraint 
    (foreign key (fmreltype_mrkr_type_group) references marker_type_group  constraint fmreltype_ftr_type_group_mrkr_foreign_key);


insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('LNCRNAG','2','lncRNA Gene');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('LINCRNAG','2','lincRNA Gene');

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
  values ('SRPRNAG','22','srpRNA Gene');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('TSCRIPTNREGREGION','22','Transcript Regulatory Region');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('TSCRIPTREGION', 'Transcript Region');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('SRPRNAG', 'srp_rna');

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
 values ('NONTSCRBD_REGION', 'Group containing nontranscribed elements');

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

values ('TSCRIPTNREGREGION','NONTSCRBD_REGION');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
values ('PROTBS','NONTSCRBD_REGION');


insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('LNCRNAG',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence lncrnag_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('LINCRNAG',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence lincrnag_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('MIRNAG',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence mirnag_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('PIRNAG',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence pirnag_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('RRNAG',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence rrnag_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('SNORNAG',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence snornag_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('SCRNAG',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence scrnag_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('TRNAG',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence trnag_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('NCRNAG',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence ncrnag_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('SRPRNAG',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence srprnag_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;


--insert into marker_type_group_member(mtgrpmem_mrkr_type,
 --   mtgrpmem_mrkr_type_group)
--values ('HISTBS','NONTSCRBD_REGION');

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
 set mreltype_mrkr_type_group_1 = 'GENEDOM_PROD_PROTEIN'
where mreltype_name = 'gene encodes small segment'; 

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
values ('TSCRIPTNREGREGION','SEARCH_MKSEG');


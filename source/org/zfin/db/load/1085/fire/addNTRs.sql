--liquibase formatted sql
--changeset sierra:RGNS-35

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('NCCR','10','NC Conserved Region (CNE, CNS)');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('TLNRR','10','Translation Regulatory Region');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('BR','10','Biological Region');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('BINDSITE','10','Binding Site');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('LIGANDBS','10','Ligand Binding Site');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('TFBS','10','TF Binding Site');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('EBS','10','Enhancer Binding Site');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('NCBS','10','Nucleotide Binding Site');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('EMR','10','Epigenetically Modified Region');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('HMR','10','Histone Modification Region');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('MDNAB','10','Modified DNA Base');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('RR','10','Regulatory Region');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('TRR','10','Transcriptional Regulatory Region');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('PROMOTER','10','Promoter');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('ENHANCER','10','Enhancer');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('LCR','10','Locus Control Region');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('NUCMO','10','Nucleotide Motif');


insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('DNAMO','10','DNA Motif');

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('RNAMO','10','RNA Motif');

create sequence nccr_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('NCCR',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence tlnrr_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('TLNRR',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence br_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('BR',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence bindsite_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('BINDSITE',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence ligandbs_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('LIGANDBS',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence tfbs_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('TFBS',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence ebs_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('EBS',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence ncbs_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('NCBS',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence emr_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('EMR',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence hmr_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('HMR',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence mdnab_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('MDNAB',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence rr_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('RR',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence trr_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('TRR',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence promoter_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('PROMOTER',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence enhancer_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('ENHANCER',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence _seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence lcr_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('LCR',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence nucmo_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('NUCMO',current,'marker','marker','mrkr_zdb_id','t','f');


create sequence dnamo_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('DNAMO',current,'marker','marker','mrkr_zdb_id','t','f');

create sequence rnamo_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('RNAMO',current,'marker','marker','mrkr_zdb_id','t','f');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('NCCR', 'nccr');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('TLNRR', 'translation regulatory region');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('BR', 'br');


insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('BINDSITE', 'binding site');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('LIGANDBS', 'ligand binding site');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('TFBS', 'TF binding site');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('EBS', 'Enhancer binding site');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('NCBS', 'Nucleotide binding site');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('EMR', 'Epigenetically modified region');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('HMR', 'Histone modification region');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('MDNAB', 'Modified dna base');


insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('RR', 'Regulatory region');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('TRR', 'Transcriptional regulatory region');


insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('PROMOTER', 'Promoter');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('ENHANCER', 'Enhancer');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('LCR', 'Locus control region');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('NUCMO', 'Nucleotide motif');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('DNAMO', 'DNA motif');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
 values ('RNAMO', 'RNA motif');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('NCCR','NONTSCRBD_REGION');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('TLNRR','NONTSCRBD_REGION');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('BR','NONTSCRBD_REGION');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('BINDSITE','NONTSCRBD_REGION');
insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('LIGANDBS','NONTSCRBD_REGION');


insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('TFBS','NONTSCRBD_REGION');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('EBS','NONTSCRBD_REGION');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('NCBS','NONTSCRBD_REGION');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('EMR','NONTSCRBD_REGION');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('HMR','NONTSCRBD_REGION');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('MDNAB','NONTSCRBD_REGION');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('RR','NONTSCRBD_REGION');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('TRR','NONTSCRBD_REGION');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('PROMOTER','NONTSCRBD_REGION');
insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('ENHANCER','NONTSCRBD_REGION');


insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('LCR','NONTSCRBD_REGION');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('NUCMO','NONTSCRBD_REGION');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('DNAMO','NONTSCRBD_REGION');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('RNAMO','NONTSCRBD_REGION');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('TLNRR','SEARCH_MKSEG');


insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('NCCR','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('BR','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('BINDSITE','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('LIGANDBS','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('TFBS','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('EBS','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('NCBS','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('EMR','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('HMR','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('MDNAB','SEARCH_MKSEG');


insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('RR','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('TRR','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('PROMOTER','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('ENHANCER','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('LCR','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('NUCMO','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('DNAMO','SEARCH_MKSEG');

insert into marker_type_group_member(mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)

values ('RNAMO','SEARCH_MKSEG');

insert into so_zfin_mapping(szm_object_type, szm_term_ont_id, szm_term_name) 
values ('NCCR','SO:0000334','nc_conserved_region');

insert into so_zfin_mapping(szm_object_type, szm_term_ont_id, szm_term_name) 
values ('BR','SO:0001411','biological_region');

insert into so_zfin_mapping(szm_object_type, szm_term_ont_id, szm_term_name) 
values ('BINDSITE','SO:0000409','binding_site');

insert into so_zfin_mapping(szm_object_type, szm_term_ont_id, szm_term_name) 
values ('LIGANDBS','SO:0001657','ligand_binding_site');

insert into so_zfin_mapping(szm_object_type, szm_term_ont_id, szm_term_name) 
values ('TFBS','SO:0000235','tf_binding_site');

insert into so_zfin_mapping(szm_object_type, szm_term_ont_id, szm_term_name) 
values ('EBS','SO:0001461','enhancer_binding_site');

insert into so_zfin_mapping(szm_object_type, szm_term_ont_id, szm_term_name) 
values ('NCBS','SO:0001655','nucleotide_binding_site'); 

insert into so_zfin_mapping(szm_object_type, szm_term_ont_id, szm_term_name) 
values ('EMR','SO:0001720','epigenetically_modified_region'); 

insert into so_zfin_mapping(szm_object_type, szm_term_ont_id, szm_term_name) 
values ('HMR','SO:0001700','histone_modification');

insert into so_zfin_mapping(szm_object_type, szm_term_ont_id, szm_term_name) 
values ('MDNAB','SO:0000305','modified_dna_base');

insert into so_zfin_mapping(szm_object_type, szm_term_ont_id, szm_term_name) 
values ('RR','SO:0005836','regulatory_region');

insert into so_zfin_mapping(szm_object_type, szm_term_ont_id, szm_term_name) 
values ('TRR','SO:0001679','transcriptional_regulatory_region');

insert into so_zfin_mapping(szm_object_type, szm_term_ont_id, szm_term_name) 
values ('PROMOTER','SO:0000167','promoter');

insert into so_zfin_mapping(szm_object_type, szm_term_ont_id, szm_term_name) 
values ('ENHANCER','SO:0000165','enhancer');

insert into so_zfin_mapping(szm_object_type, szm_term_ont_id, szm_term_name) 
values ('LCR','SO:0000037','locus_control_region');

insert into so_zfin_mapping(szm_object_type, szm_term_ont_id, szm_term_name) 
values ('NUCMO','SO:0000714','nucleotide motif');

insert into so_zfin_mapping(szm_object_type, szm_term_ont_id, szm_term_name) 
values ('DNAMO','SO:0000713','dna_motif');

insert into so_zfin_mapping(szm_object_type, szm_term_ont_id, szm_term_name) 
values ('RNAMO','SO:0000710','rna_motif');

insert into so_zfin_mapping(szm_object_type, szm_term_ont_id, szm_term_name) 
values ('TLNRR','SO:0001680','translation_regulatory_region');



--liquibase formatted sql
--changeset xiang:ZFIN-6016.sql

create temp table small_nuclear_rna_id (
  id text
);

insert into small_nuclear_rna_id
select get_id('NCRNAG') from single;

insert into zdb_active_data select id from small_nuclear_rna_id;

insert into marker (mrkr_zdb_id, mrkr_name, mrkr_abbrev, mrkr_type, mrkr_owner)
select id, 'temp1', 'temp1', 'NCRNAG', 'ZDB-PERS-030520-3'
  from small_nuclear_rna_id;

update clean_expression_fast_search 
                                set cefs_mrkr_zdb_id = (select id from small_nuclear_rna_id)
                              where cefs_mrkr_zdb_id = 'ZDB-GENE-081002-2';

update expression_experiment 
                                set xpatex_gene_zdb_id = (select id from small_nuclear_rna_id)
                              where xpatex_gene_zdb_id = 'ZDB-GENE-081002-2';

update expression_experiment2 
                                set xpatex_gene_zdb_id = (select id from small_nuclear_rna_id)
                              where xpatex_gene_zdb_id = 'ZDB-GENE-081002-2';

update marker_go_term_evidence 
                                set mrkrgoev_mrkr_zdb_id = (select id from small_nuclear_rna_id)
                              where mrkrgoev_mrkr_zdb_id = 'ZDB-GENE-081002-2';

update marker_history 
                                set mhist_mrkr_zdb_id = (select id from small_nuclear_rna_id)
                              where mhist_mrkr_zdb_id = 'ZDB-GENE-081002-2';

update marker_history_audit 
                                set mha_mrkr_zdb_id = (select id from small_nuclear_rna_id)
                              where mha_mrkr_zdb_id = 'ZDB-GENE-081002-2';

update marker_relationship 
                                set mrel_mrkr_2_zdb_id = (select id from small_nuclear_rna_id)
                              where mrel_mrkr_2_zdb_id = 'ZDB-GENE-081002-2';

update mutant_fast_search 
                                set mfs_mrkr_zdb_id = (select id from small_nuclear_rna_id)
                              where mfs_mrkr_zdb_id = 'ZDB-GENE-081002-2';

update ortholog 
                                set ortho_zebrafish_gene_zdb_id = (select id from small_nuclear_rna_id)
                              where ortho_zebrafish_gene_zdb_id = 'ZDB-GENE-081002-2';

update data_note 
                                set dnote_data_zdb_id = (select id from small_nuclear_rna_id)
                              where dnote_data_zdb_id = 'ZDB-GENE-081002-2';

update db_link 
                                set dblink_linked_recid = (select id from small_nuclear_rna_id)
                              where dblink_linked_recid = 'ZDB-GENE-081002-2';
   
  
delete from record_attribution where recattrib_pk_id = '320864';

update record_attribution set recattrib_data_zdb_id = (select id from small_nuclear_rna_id) where recattrib_data_zdb_id = 'ZDB-GENE-081002-2';

delete from zdb_active_data where zactvd_zdb_id = 'ZDB-GENE-081002-2';

update marker set mrkr_name = 'RNA, U12 small nuclear', mrkr_abbrev = 'rnu12' where mrkr_zdb_id = (select id from small_nuclear_rna_id);

insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id) select 'ZDB-GENE-081002-2', id from small_nuclear_rna_id;

delete from small_nuclear_rna_id;

insert into small_nuclear_rna_id
select get_id('NCRNAG') from single;

insert into zdb_active_data select id from small_nuclear_rna_id;

insert into marker (mrkr_zdb_id, mrkr_name, mrkr_abbrev, mrkr_type, mrkr_owner)
select id, 'temp2', 'temp2', 'NCRNAG', 'ZDB-PERS-030520-3'
  from small_nuclear_rna_id;

update clean_expression_fast_search 
                                set cefs_mrkr_zdb_id = (select id from small_nuclear_rna_id)
                              where cefs_mrkr_zdb_id = 'ZDB-GENE-081003-2';


update expression_experiment
                                set xpatex_gene_zdb_id = (select id from small_nuclear_rna_id)
                              where xpatex_gene_zdb_id = 'ZDB-GENE-081003-2';

update expression_experiment2 
                                set xpatex_gene_zdb_id = (select id from small_nuclear_rna_id)
                              where xpatex_gene_zdb_id = 'ZDB-GENE-081003-2';

update marker_go_term_evidence 
                                set mrkrgoev_mrkr_zdb_id = (select id from small_nuclear_rna_id)
                              where mrkrgoev_mrkr_zdb_id = 'ZDB-GENE-081003-2';

update marker_history 
                                set mhist_mrkr_zdb_id = (select id from small_nuclear_rna_id)
                              where mhist_mrkr_zdb_id = 'ZDB-GENE-081003-2';

update marker_history_audit 
                                set mha_mrkr_zdb_id = (select id from small_nuclear_rna_id)
                              where mha_mrkr_zdb_id = 'ZDB-GENE-081003-2';

update marker_relationship 
                                set mrel_mrkr_1_zdb_id = (select id from small_nuclear_rna_id)
                              where mrel_mrkr_1_zdb_id = 'ZDB-GENE-081003-2';

update marker_relationship 
                                set mrel_mrkr_2_zdb_id = (select id from small_nuclear_rna_id)
                              where mrel_mrkr_2_zdb_id = 'ZDB-GENE-081003-2';

update mutant_fast_search 
                                set mfs_mrkr_zdb_id = (select id from small_nuclear_rna_id)
                              where mfs_mrkr_zdb_id = 'ZDB-GENE-081003-2';

update data_note 
                                set dnote_data_zdb_id = (select id from small_nuclear_rna_id)
                              where dnote_data_zdb_id = 'ZDB-GENE-081003-2';

update db_link 
                                set dblink_linked_recid = (select id from small_nuclear_rna_id)
                              where dblink_linked_recid = 'ZDB-GENE-081003-2';

update sequence_feature_chromosome_location_generated 
                                set sfclg_data_zdb_id = (select id from small_nuclear_rna_id)
                              where sfclg_data_zdb_id = 'ZDB-GENE-081003-2';


delete from record_attribution where recattrib_pk_id = '320872';

update record_attribution set recattrib_data_zdb_id = (select id from small_nuclear_rna_id) where recattrib_data_zdb_id = 'ZDB-GENE-081003-2';

delete from zdb_active_data where zactvd_zdb_id = 'ZDB-GENE-081003-2';

update marker set mrkr_name = 'RNA, U2 small nuclear 1', mrkr_abbrev = 'rnu2' where mrkr_zdb_id = (select id from small_nuclear_rna_id);

insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id) select 'ZDB-GENE-081003-2', id from small_nuclear_rna_id;


delete from small_nuclear_rna_id;


insert into small_nuclear_rna_id
select get_id('NCRNAG') from single;

insert into zdb_active_data select id from small_nuclear_rna_id;

insert into marker (mrkr_zdb_id, mrkr_name, mrkr_abbrev, mrkr_type, mrkr_owner)
select id, 'temp3', 'temp3', 'NCRNAG', 'ZDB-PERS-030520-3'
  from small_nuclear_rna_id;

update clean_expression_fast_search 
                                set cefs_mrkr_zdb_id = (select id from small_nuclear_rna_id)
                              where cefs_mrkr_zdb_id = 'ZDB-GENE-090323-1';


update expression_experiment
                                set xpatex_gene_zdb_id = (select id from small_nuclear_rna_id)
                              where xpatex_gene_zdb_id = 'ZDB-GENE-090323-1';

update expression_experiment2 
                                set xpatex_gene_zdb_id = (select id from small_nuclear_rna_id)
                              where xpatex_gene_zdb_id = 'ZDB-GENE-090323-1';

update marker_history 
                                set mhist_mrkr_zdb_id = (select id from small_nuclear_rna_id)
                              where mhist_mrkr_zdb_id = 'ZDB-GENE-090323-1';

update marker_history_audit 
                                set mha_mrkr_zdb_id = (select id from small_nuclear_rna_id)
                              where mha_mrkr_zdb_id = 'ZDB-GENE-090323-1';

update marker_relationship 
                                set mrel_mrkr_2_zdb_id = (select id from small_nuclear_rna_id)
                              where mrel_mrkr_2_zdb_id = 'ZDB-GENE-090323-1';
                              
update ortholog 
                                set ortho_zebrafish_gene_zdb_id = (select id from small_nuclear_rna_id)
                              where ortho_zebrafish_gene_zdb_id = 'ZDB-GENE-090323-1';


update db_link 
                                set dblink_linked_recid = (select id from small_nuclear_rna_id)
                              where dblink_linked_recid = 'ZDB-GENE-090323-1';

update data_alias               set dalias_data_zdb_id = (select id from small_nuclear_rna_id)
                              where dalias_data_zdb_id = 'ZDB-GENE-090323-1';

update sequence_feature_chromosome_location_generated 
                                set sfclg_data_zdb_id = (select id from small_nuclear_rna_id)
                              where sfclg_data_zdb_id = 'ZDB-GENE-090323-1';
                              
update snp_download             set snpd_mrkr_zdb_id = (select id from small_nuclear_rna_id)
                              where snpd_mrkr_zdb_id = 'ZDB-GENE-090323-1';                              

delete from record_attribution where recattrib_pk_id = '322129';

update record_attribution set recattrib_data_zdb_id = (select id from small_nuclear_rna_id) where recattrib_data_zdb_id = 'ZDB-GENE-090323-1';

delete from zdb_active_data where zactvd_zdb_id = 'ZDB-GENE-090323-1';

update marker set mrkr_name = 'RNA, 7SK small nuclear', mrkr_abbrev = 'rn7sk' where mrkr_zdb_id = (select id from small_nuclear_rna_id);

insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id) select 'ZDB-GENE-090323-1', id from small_nuclear_rna_id;

delete from small_nuclear_rna_id;

insert into small_nuclear_rna_id
select get_id('NCRNAG') from single;

insert into zdb_active_data select id from small_nuclear_rna_id;

insert into marker (mrkr_zdb_id, mrkr_name, mrkr_abbrev, mrkr_type, mrkr_owner)
select id, 'temp4', 'temp4', 'NCRNAG', 'ZDB-PERS-050429-23'
  from small_nuclear_rna_id;

update marker_history 
                                set mhist_mrkr_zdb_id = (select id from small_nuclear_rna_id)
                              where mhist_mrkr_zdb_id = 'ZDB-GENE-140912-2';

update marker_history_audit 
                                set mha_mrkr_zdb_id = (select id from small_nuclear_rna_id)
                              where mha_mrkr_zdb_id = 'ZDB-GENE-140912-2';

update marker_relationship 
                                set mrel_mrkr_2_zdb_id = (select id from small_nuclear_rna_id)
                              where mrel_mrkr_2_zdb_id = 'ZDB-GENE-140912-2';

update data_note 
                                set dnote_data_zdb_id = (select id from small_nuclear_rna_id)
                              where dnote_data_zdb_id = 'ZDB-GENE-140912-2';

update db_link 
                                set dblink_linked_recid = (select id from small_nuclear_rna_id)
                              where dblink_linked_recid = 'ZDB-GENE-140912-2';

update sequence_feature_chromosome_location_generated 
                                set sfclg_data_zdb_id = (select id from small_nuclear_rna_id)
                              where sfclg_data_zdb_id = 'ZDB-GENE-140912-2';

update record_attribution set recattrib_data_zdb_id = (select id from small_nuclear_rna_id) where recattrib_data_zdb_id = 'ZDB-GENE-140912-2';

delete from zdb_active_data where zactvd_zdb_id = 'ZDB-GENE-140912-2';

update marker set mrkr_name = 'small nuclear RNA u11', mrkr_abbrev = 'rnu11', mrkr_comments = 'corresponds to ENSDARG00000080410 in ensembl' where mrkr_zdb_id = (select id from small_nuclear_rna_id);

insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id) select 'ZDB-GENE-140912-2', id from small_nuclear_rna_id;

delete from small_nuclear_rna_id;

insert into small_nuclear_rna_id
select get_id('NCRNAG') from single;

insert into zdb_active_data select id from small_nuclear_rna_id;

insert into marker (mrkr_zdb_id, mrkr_name, mrkr_abbrev, mrkr_type, mrkr_owner)
select id, 'temp5', 'temp5', 'NCRNAG', 'ZDB-PERS-030520-3'
  from small_nuclear_rna_id;

update marker_history 
                                set mhist_mrkr_zdb_id = (select id from small_nuclear_rna_id)
                              where mhist_mrkr_zdb_id = 'ZDB-GENE-120314-1';

update marker_history_audit 
                                set mha_mrkr_zdb_id = (select id from small_nuclear_rna_id)
                              where mha_mrkr_zdb_id = 'ZDB-GENE-120314-1';

                              
update ortholog 
                                set ortho_zebrafish_gene_zdb_id = (select id from small_nuclear_rna_id)
                              where ortho_zebrafish_gene_zdb_id = 'ZDB-GENE-120314-1';


update db_link 
                                set dblink_linked_recid = (select id from small_nuclear_rna_id)
                              where dblink_linked_recid = 'ZDB-GENE-120314-1';

update data_alias               set dalias_data_zdb_id = (select id from small_nuclear_rna_id)
                              where dalias_data_zdb_id = 'ZDB-GENE-120314-1'; 
                              
update record_attribution set recattrib_data_zdb_id = (select id from small_nuclear_rna_id) where recattrib_data_zdb_id = 'ZDB-GENE-120314-1';

delete from zdb_active_data where zactvd_zdb_id = 'ZDB-GENE-120314-1';

update marker set mrkr_name = 'small nucleolar RNA host gene 1', mrkr_abbrev = 'snhg1' where mrkr_zdb_id = (select id from small_nuclear_rna_id);

insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id) select 'ZDB-GENE-120314-1', id from small_nuclear_rna_id;


--liquibase formatted sql
--changeset xshao:ZFIN-6249

set session_replication_role = 'replica';

update zdb_replaced_data
 set zrepld_old_zdb_id = replace(zrepld_old_zdb_id, 'LINCRNAG', 'GENE')
 where zrepld_old_zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update zdb_replaced_data
 set zrepld_new_zdb_id = replace(zrepld_new_zdb_id, 'LINCRNAG', 'GENE')
 where zrepld_new_zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update withdrawn_data 
 set wd_old_zdb_id = replace(wd_old_Zdb_id, 'LINCRNAG', 'GENE')
 where wd_old_zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update withdrawn_data 
 set wd_new_zdb_id = replace(wd_new_Zdb_id, 'LINCRNAG', 'GENE')
 where wd_new_zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');


update zdb_active_data
 set zactvd_zdb_id = replace(zactvd_zdb_id, 'LINCRNAG', 'GENE')
 where zactvd_zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

insert into zdb_replaced_data (zrepld_new_zdb_id, zrepld_old_zdb_id)
 values('ZDB-GENE-130530-538', 'ZDB-LINCRNAG-130530-538');
 
insert into zdb_replaced_data (zrepld_new_zdb_id, zrepld_old_zdb_id)
 values('ZDB-GENE-070615-1', 'ZDB-LINCRNAG-070615-1');
 
insert into zdb_replaced_data (zrepld_new_zdb_id, zrepld_old_zdb_id)
 values('ZDB-GENE-070912-336', 'ZDB-LINCRNAG-070912-336');
 
insert into zdb_replaced_data (zrepld_new_zdb_id, zrepld_old_zdb_id)
 values('ZDB-GENE-131121-265', 'ZDB-LINCRNAG-131121-265');
 
insert into zdb_replaced_data (zrepld_new_zdb_id, zrepld_old_zdb_id)
 values('ZDB-GENE-110419-3', 'ZDB-LINCRNAG-110419-3');
 
insert into zdb_replaced_data (zrepld_new_zdb_id, zrepld_old_zdb_id)
 values('ZDB-GENE-060421-7397', 'ZDB-LINCRNAG-060421-7397'); 

insert into withdrawn_data (wd_new_zdb_id, wd_old_zdb_id)
 values('ZDB-GENE-130530-538', 'ZDB-LINCRNAG-130530-538');
 
insert into withdrawn_data (wd_new_zdb_id, wd_old_zdb_id)
 values('ZDB-GENE-070615-1', 'ZDB-LINCRNAG-070615-1');
 
insert into withdrawn_data (wd_new_zdb_id, wd_old_zdb_id)
 values('ZDB-GENE-070912-336', 'ZDB-LINCRNAG-070912-336');
 
insert into withdrawn_data (wd_new_zdb_id, wd_old_zdb_id)
 values('ZDB-GENE-131121-265', 'ZDB-LINCRNAG-131121-265');
 
insert into withdrawn_data (wd_new_zdb_id, wd_old_zdb_id)
 values('ZDB-GENE-110419-3', 'ZDB-LINCRNAG-110419-3');
 
insert into withdrawn_data (wd_new_zdb_id, wd_old_zdb_id)
 values('ZDB-GENE-060421-7397', 'ZDB-LINCRNAG-060421-7397'); 
  
update marker
 set mrkr_type = 'GENE'
 where mrkr_type = 'LINCRNAG' 
 and mrkr_zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update marker
 set mrkr_zdb_id = replace(mrkr_Zdb_id, 'LINCRNAG', 'GENE')
 where mrkr_zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update zmap_pub_pan_mark
 set zdb_id = replace(zdb_id, 'LINCRNAG', 'GENE')
 where  zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update external_note
 set extnote_data_zdb_id = replace(extnote_data_zdb_id, 'LINCRNAG', 'GENE')
 where extnote_data_zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update paneled_markers
   set zdb_id = replace(zdb_id, 'LINCRNAG', 'GENE')
 where zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update sequence_feature_chromosome_location
   set sfcl_feature_Zdb_id = replace( sfcl_feature_Zdb_id, 'LINCRNAG', 'GENE')
 where sfcl_feature_Zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update record_attribution
 set recattrib_data_zdb_id = replace(recattrib_data_zdb_id, 'LINCRNAG', 'GENE')
 where recattrib_data_zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update expression_experiment2
 set xpatex_gene_zdb_id = replace(xpatex_gene_zdb_id, 'LINCRNAG', 'GENE')
 where xpatex_gene_zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update feature_marker_relationship
 set fmrel_mrkr_zdb_id = replace(fmrel_mrkr_zdb_id , 'LINCRNAG', 'GENE')
 where fmrel_mrkr_zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update marker_relationship
 set mrel_mrkr_1_zdb_id = replace(mrel_mrkr_1_zdb_id, 'LINCRNAG', 'GENE')
 where mrel_mrkr_1_zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update construct_component
 set cc_component_zdb_id  = replace(cc_component_zdb_id, 'LINCRNAG', 'GENE')
 where cc_component_zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update marker_relationship
 set mrel_mrkr_2_zdb_id = replace(mrel_mrkr_2_zdb_id, 'LINCRNAG', 'GENE')
 where mrel_mrkr_2_zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update data_alias
 set dalias_data_zdb_id = replace(dalias_data_zdb_id, 'LINCRNAG', 'GENE')
 where dalias_data_zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update marker_history
 set mhist_mrkr_zdb_id = replace(mhist_mrkr_zdb_id,  'LINCRNAG', 'GENE')
 where mhist_mrkr_zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update marker_history_audit
 set mha_mrkr_zdb_id = replace(mha_mrkr_zdb_id, 'LINCRNAG', 'GENE')
 where mha_mrkr_zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update zdb_orphan_data
 set zorphand_zdb_id = replace(zorphand_zdb_id, 'LINCRNAG', 'GENE')
 where zorphand_zdb_id  in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update external_reference
 set exref_data_zdb_id = replace(exref_data_zdb_id, 'LINCRNAG', 'GENE')
 where exref_data_zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update int_data_source
 set ids_data_zdb_id = replace(ids_data_zdb_id, 'LINCRNAG', 'GENE')
 where ids_data_zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update linkage_membership_Search
 set lms_member_1_zdb_id = replace(lms_member_1_zdb_id, 'LINCRNAG', 'GENE')
 where lms_member_1_zdb_id  in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update linkage_membership_Search
 set lms_member_2_zdb_id = replace(lms_member_2_zdb_id, 'LINCRNAG', 'GENE')
 where lms_member_2_zdb_id  in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update mapped_marker
 set marker_id  = replace(marker_id, 'LINCRNAG', 'GENE')
 where marker_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update db_link
 set dblink_linked_recid = replace(dblink_linked_recid, 'LINCRNAG', 'GENE')
 where dblink_linked_recid in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update data_note
 set dnote_data_zdb_id = replace(dnote_data_zdb_id, 'LINCRNAG', 'GENE')
 where dnote_data_zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update unique_location 
  set ul_data_zdb_id = replace(ul_data_zdb_id, 'LINCRNAG', 'GENE')
 where ul_data_zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update ortholog
  set ortho_zebrafish_gene_zdb_id = replace(ortho_zebrafish_gene_zdb_id, 'LINCRNAG', 'GENE')
 where ortho_zebrafish_gene_zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update snp_download
  set snpd_mrkr_zdb_id = replace(snpd_mrkr_zdb_id, 'LINCRNAG', 'GENE')
 where snpd_mrkr_zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update marker_go_term_evidence
  set mrkrgoev_mrkr_zdb_id = replace(mrkrgoev_mrkr_zdb_id, 'LINCRNAG', 'GENE')
 where mrkrgoev_mrkr_zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update linkage_member
 set lnkgmem_member_zdb_id = replace(lnkgmem_member_zdb_id, 'LINCRNAG', 'GENE')
where lnkgmem_member_zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update linkage_membership
 set lnkgm_member_1_zdb_id = replace(lnkgm_member_1_zdb_id, 'LINCRNAG', 'GENE')
 where lnkgm_member_1_zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update linkage_membership
 set lnkgm_member_2_zdb_id = replace(lnkgm_member_2_zdb_id, 'LINCRNAG', 'GENE')
 where lnkgm_member_2_zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update linkage_pair_member
  set lpmem_member_zdb_id = replace(lpmem_member_zdb_id, 'LINCRNAG', 'GENE')
 where lpmem_member_zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update linkage_single
 set lsingle_member_zdb_id = replace(lsingle_member_zdb_id, 'LINCRNAG', 'GENE')
 where lsingle_member_zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update updates
  set rec_id = replace(rec_id, 'LINCRNAG', 'GENE')
 where rec_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update primer_set
  set marker_id = replace(marker_id, 'GENE','LINCRNAG' )
where  marker_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update genedom_family_member
  set gfammem_mrkr_zdb_id = replace(gfammem_mrkr_zdb_id , 'LINCRNAG', 'GENE')
where gfammem_mrkr_zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update clone
  set clone_mrkr_zdb_id = replace(clone_mrkr_zdb_id , 'LINCRNAG', 'GENE')
where clone_mrkr_zdb_id  in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update construct_marker_relationship
  set conmrkrrel_mrkr_zdb_id = replace( conmrkrrel_mrkr_zdb_id, 'LINCRNAG', 'GENE')
where conmrkrrel_mrkr_zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update marker_sequence
  set seq_mrkr_Zdb_id = replace(seq_mrkr_Zdb_id , 'LINCRNAG', 'GENE')
where seq_mrkr_Zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update clean_expression_fast_search
  set cefs_mrkr_zdb_id = replace(cefs_mrkr_zdb_id , 'LINCRNAG', 'GENE')
where cefs_mrkr_zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');

update mutant_fast_search
  set mfs_mrkr_zdb_id = replace(mfs_mrkr_zdb_id , 'LINCRNAG', 'GENE')
where mfs_mrkr_zdb_id in ('ZDB-LINCRNAG-130530-538', 'ZDB-LINCRNAG-070615-1', 'ZDB-LINCRNAG-070912-336', 'ZDB-LINCRNAG-131121-265', 'ZDB-LINCRNAG-110419-3', 'ZDB-LINCRNAG-060421-7397');


set session_replication_role = 'origin';

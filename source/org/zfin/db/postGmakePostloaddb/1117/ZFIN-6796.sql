--liquibase formatted sql
--changeset pm:ZFIN-6249

set session_replication_role = 'replica';

update zdb_replaced_data
 set zrepld_old_zdb_id = replace(zrepld_old_zdb_id, 'GENE', 'LNCRNAG')
 where zrepld_old_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update zdb_replaced_data
 set zrepld_new_zdb_id = replace(zrepld_new_zdb_id, 'GENE', 'LNCRNAG')
 where zrepld_new_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update withdrawn_data 
 set wd_old_zdb_id = replace(wd_old_Zdb_id, 'GENE', 'LNCRNAG')
 where wd_old_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update withdrawn_data 
 set wd_new_zdb_id = replace(wd_new_Zdb_id, 'GENE', 'LNCRNAG')
 where wd_new_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');


update zdb_active_data
 set zactvd_zdb_id = replace(zactvd_zdb_id, 'GENE', 'LNCRNAG')
 where zactvd_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');


insert into zdb_replaced_data (zrepld_new_zdb_id, zrepld_old_zdb_id)
 values('ZDB-LNCRNAG-131127-227', 'ZDB-GENE-131127-227');
 
insert into zdb_replaced_data (zrepld_new_zdb_id, zrepld_old_zdb_id)
 values('ZDB-LNCRNAG-081104-166', 'ZDB-GENE-081104-166');
 
insert into zdb_replaced_data (zrepld_new_zdb_id, zrepld_old_zdb_id)
 values('ZDB-LNCRNAG-091125-1', 'ZDB-GENE-091125-1');
 
insert into zdb_replaced_data (zrepld_new_zdb_id, zrepld_old_zdb_id)
 values('ZDB-LNCRNAG-141216-212', 'ZDB-GENE-141216-212');
 
insert into zdb_replaced_data (zrepld_new_zdb_id, zrepld_old_zdb_id)
 values('ZDB-LNCRNAG-140804-1', 'ZDB-GENE-140804-1');
 


insert into withdrawn_data (wd_new_zdb_id, wd_old_zdb_id)
 values('ZDB-LNCRNAG-131127-227', 'ZDB-GENE-131127-227');
 
insert into withdrawn_data (wd_new_zdb_id, wd_old_zdb_id)
 values('ZDB-LNCRNAG-081104-166', 'ZDB-GENE-081104-166');
 
insert into withdrawn_data (wd_new_zdb_id, wd_old_zdb_id)
 values('ZDB-LNCRNAG-091125-1', 'ZDB-GENE-091125-1');
 
insert into withdrawn_data (wd_new_zdb_id, wd_old_zdb_id)
 values('ZDB-LNCRNAG-141216-212', 'ZDB-GENE-141216-212');
 
insert into withdrawn_data (wd_new_zdb_id, wd_old_zdb_id)
 values('ZDB-LNCRNAG-140804-1', 'ZDB-GENE-140804-1');
 

  
update marker
 set mrkr_type = 'LNCRNAG'
 where mrkr_type = 'GENE' 
 and mrkr_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update marker
 set mrkr_zdb_id = replace(mrkr_Zdb_id, 'GENE', 'LNCRNAG')
 where mrkr_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update zmap_pub_pan_mark
 set zdb_id = replace(zdb_id, 'GENE', 'LNCRNAG')
 where  zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update external_note
 set extnote_data_zdb_id = replace(extnote_data_zdb_id, 'GENE', 'LNCRNAG')
 where extnote_data_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update paneled_markers
   set zdb_id = replace(zdb_id, 'GENE', 'LNCRNAG')
 where zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update sequence_feature_chromosome_location
   set sfcl_feature_Zdb_id = replace( sfcl_feature_Zdb_id, 'GENE', 'LNCRNAG')
 where sfcl_feature_Zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update record_attribution
 set recattrib_data_zdb_id = replace(recattrib_data_zdb_id, 'GENE', 'LNCRNAG')
 where recattrib_data_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update expression_experiment2
 set xpatex_gene_zdb_id = replace(xpatex_gene_zdb_id, 'GENE', 'LNCRNAG')
 where xpatex_gene_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update feature_marker_relationship
 set fmrel_mrkr_zdb_id = replace(fmrel_mrkr_zdb_id , 'GENE', 'LNCRNAG')
 where fmrel_mrkr_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update marker_relationship
 set mrel_mrkr_1_zdb_id = replace(mrel_mrkr_1_zdb_id, 'GENE', 'LNCRNAG')
 where mrel_mrkr_1_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update construct_component
 set cc_component_zdb_id  = replace(cc_component_zdb_id, 'GENE', 'LNCRNAG')
 where cc_component_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update marker_relationship
 set mrel_mrkr_2_zdb_id = replace(mrel_mrkr_2_zdb_id, 'GENE', 'LNCRNAG')
 where mrel_mrkr_2_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update data_alias
 set dalias_data_zdb_id = replace(dalias_data_zdb_id, 'GENE', 'LNCRNAG')
 where dalias_data_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update marker_history
 set mhist_mrkr_zdb_id = replace(mhist_mrkr_zdb_id,  'GENE', 'LNCRNAG')
 where mhist_mrkr_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update marker_history_audit
 set mha_mrkr_zdb_id = replace(mha_mrkr_zdb_id, 'GENE', 'LNCRNAG')
 where mha_mrkr_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update zdb_orphan_data
 set zorphand_zdb_id = replace(zorphand_zdb_id, 'GENE', 'LNCRNAG')
 where zorphand_zdb_id  in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update external_reference
 set exref_data_zdb_id = replace(exref_data_zdb_id, 'GENE', 'LNCRNAG')
 where exref_data_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update int_data_source
 set ids_data_zdb_id = replace(ids_data_zdb_id, 'GENE', 'LNCRNAG')
 where ids_data_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update linkage_membership_Search
 set lms_member_1_zdb_id = replace(lms_member_1_zdb_id, 'GENE', 'LNCRNAG')
 where lms_member_1_zdb_id  in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update linkage_membership_Search
 set lms_member_2_zdb_id = replace(lms_member_2_zdb_id, 'GENE', 'LNCRNAG')
 where lms_member_2_zdb_id  in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update mapped_marker
 set marker_id  = replace(marker_id, 'GENE', 'LNCRNAG')
 where marker_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update db_link
 set dblink_linked_recid = replace(dblink_linked_recid, 'GENE', 'LNCRNAG')
 where dblink_linked_recid in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update data_note
 set dnote_data_zdb_id = replace(dnote_data_zdb_id, 'GENE', 'LNCRNAG')
 where dnote_data_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update unique_location 
  set ul_data_zdb_id = replace(ul_data_zdb_id, 'GENE', 'LNCRNAG')
 where ul_data_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update ortholog
  set ortho_zebrafish_gene_zdb_id = replace(ortho_zebrafish_gene_zdb_id, 'GENE', 'LNCRNAG')
 where ortho_zebrafish_gene_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update snp_download
  set snpd_mrkr_zdb_id = replace(snpd_mrkr_zdb_id, 'GENE', 'LNCRNAG')
 where snpd_mrkr_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update marker_go_term_evidence
  set mrkrgoev_mrkr_zdb_id = replace(mrkrgoev_mrkr_zdb_id, 'GENE', 'LNCRNAG')
 where mrkrgoev_mrkr_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update linkage_member
 set lnkgmem_member_zdb_id = replace(lnkgmem_member_zdb_id, 'GENE', 'LNCRNAG')
where lnkgmem_member_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update linkage_membership
 set lnkgm_member_1_zdb_id = replace(lnkgm_member_1_zdb_id, 'GENE', 'LNCRNAG')
 where lnkgm_member_1_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update linkage_membership
 set lnkgm_member_2_zdb_id = replace(lnkgm_member_2_zdb_id, 'GENE', 'LNCRNAG')
 where lnkgm_member_2_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update linkage_pair_member
  set lpmem_member_zdb_id = replace(lpmem_member_zdb_id, 'GENE', 'LNCRNAG')
 where lpmem_member_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update linkage_single
 set lsingle_member_zdb_id = replace(lsingle_member_zdb_id, 'GENE', 'LNCRNAG')
 where lsingle_member_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update updates
  set rec_id = replace(rec_id, 'GENE', 'LNCRNAG')
 where rec_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update primer_set
  set marker_id = replace(marker_id, 'LNCRNAG','GENE' )
where  marker_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update genedom_family_member
  set gfammem_mrkr_zdb_id = replace(gfammem_mrkr_zdb_id , 'GENE', 'LNCRNAG')
where gfammem_mrkr_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update clone
  set clone_mrkr_zdb_id = replace(clone_mrkr_zdb_id , 'GENE', 'LNCRNAG')
where clone_mrkr_zdb_id  in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update construct_marker_relationship
  set conmrkrrel_mrkr_zdb_id = replace( conmrkrrel_mrkr_zdb_id, 'GENE', 'LNCRNAG')
where conmrkrrel_mrkr_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update marker_sequence
  set seq_mrkr_Zdb_id = replace(seq_mrkr_Zdb_id , 'GENE', 'LNCRNAG')
where seq_mrkr_Zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update clean_expression_fast_search
  set cefs_mrkr_zdb_id = replace(cefs_mrkr_zdb_id , 'GENE', 'LNCRNAG')
where cefs_mrkr_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update mutant_fast_search
  set mfs_mrkr_zdb_id = replace(mfs_mrkr_zdb_id , 'GENE', 'LNCRNAG')
where mfs_mrkr_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

update sequence_feature_chromosome_location_generated
   set sfclg_data_zdb_id = replace(sfclg_data_zdb_id , 'GENE', 'LNCRNAG')
 where sfclg_data_zdb_id in ('ZDB-GENE-131127-227', 'ZDB-GENE-081104-166', 'ZDB-GENE-091125-1', 'ZDB-GENE-141216-212', 'ZDB-GENE-140804-1');

set session_replication_role = 'origin';

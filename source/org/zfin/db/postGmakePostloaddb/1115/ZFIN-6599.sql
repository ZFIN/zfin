--liquibase formatted sql
--changeset pm:ZFIN-6599


set session_replication_role = 'replica';




update zdb_active_data
 set zactvd_zdb_id = replace(zactvd_zdb_id,  'GENE','LINCRNAG')
 where zactvd_zdb_id in ('ZDB-GENE-130530-538');

insert into zdb_replaced_data (zrepld_new_zdb_id, zrepld_old_zdb_id)
 values('ZDB-LINCRNAG-130530-538', 'ZDB-GENE-130530-538');
 


insert into withdrawn_data (wd_new_zdb_id, wd_old_zdb_id)
 values('ZDB-LINCRNAG-130530-538', 'ZDB-GENE-130530-538');
 

  
update marker
 set mrkr_type = 'LINCRNAG'
 where mrkr_type = 'GENE' 
 and mrkr_zdb_id ='ZDB-GENE-130530-538';

update marker
 set mrkr_zdb_id = replace(mrkr_Zdb_id, 'GENE','LINCRNAG')
 where mrkr_zdb_id ='ZDB-GENE-130530-538';


update zmap_pub_pan_mark
 set zdb_id = replace(zdb_id, 'GENE','LINCRNAG')
 where  zdb_id ='ZDB-GENE-130530-538';

update external_note
 set extnote_data_zdb_id = replace(extnote_data_zdb_id, 'GENE','LINCRNAG')
 where extnote_data_zdb_id ='ZDB-GENE-130530-538';

update paneled_markers
   set zdb_id = replace(zdb_id, 'GENE','LINCRNAG')
 where zdb_id ='ZDB-GENE-130530-538';

update sequence_feature_chromosome_location
   set sfcl_feature_Zdb_id = replace( sfcl_feature_Zdb_id, 'GENE','LINCRNAG')
 where sfcl_feature_Zdb_id ='ZDB-GENE-130530-538';

update record_attribution
 set recattrib_data_zdb_id = replace(recattrib_data_zdb_id, 'GENE','LINCRNAG')
 where recattrib_data_zdb_id ='ZDB-GENE-130530-538';

update expression_experiment2
 set xpatex_gene_zdb_id = replace(xpatex_gene_zdb_id, 'GENE','LINCRNAG')
 where xpatex_gene_zdb_id ='ZDB-GENE-130530-538';

update feature_marker_relationship
 set fmrel_mrkr_zdb_id = replace(fmrel_mrkr_zdb_id , 'GENE','LINCRNAG')
 where fmrel_mrkr_zdb_id ='ZDB-GENE-130530-538';

update marker_relationship
 set mrel_mrkr_1_zdb_id = replace(mrel_mrkr_1_zdb_id, 'GENE','LINCRNAG')
 where mrel_mrkr_1_zdb_id ='ZDB-GENE-130530-538';

update construct_component
 set cc_component_zdb_id  = replace(cc_component_zdb_id, 'GENE','LINCRNAG')
 where cc_component_zdb_id ='ZDB-GENE-130530-538';

update marker_relationship
 set mrel_mrkr_2_zdb_id = replace(mrel_mrkr_2_zdb_id, 'GENE','LINCRNAG')
 where mrel_mrkr_2_zdb_id ='ZDB-GENE-130530-538';

update data_alias
 set dalias_data_zdb_id = replace(dalias_data_zdb_id, 'GENE','LINCRNAG')
 where dalias_data_zdb_id='ZDB-GENE-130530-538';

update marker_history
 set mhist_mrkr_zdb_id = replace(mhist_mrkr_zdb_id,  'GENE','LINCRNAG')
 where mhist_mrkr_zdb_id ='ZDB-GENE-130530-538';

update marker_history_audit
 set mha_mrkr_zdb_id = replace(mha_mrkr_zdb_id, 'GENE','LINCRNAG')
 where mha_mrkr_zdb_id ='ZDB-GENE-130530-538';

update zdb_orphan_data
 set zorphand_zdb_id = replace(zorphand_zdb_id, 'GENE','LINCRNAG')
 where zorphand_zdb_id ='ZDB-GENE-130530-538';

update external_reference
 set exref_data_zdb_id = replace(exref_data_zdb_id, 'GENE','LINCRNAG')
 where exref_data_zdb_id ='ZDB-GENE-130530-538';

update int_data_source
 set ids_data_zdb_id = replace(ids_data_zdb_id, 'GENE','LINCRNAG')
 where ids_data_zdb_id ='ZDB-GENE-130530-538';

update linkage_membership_Search
 set lms_member_1_zdb_id = replace(lms_member_1_zdb_id, 'GENE','LINCRNAG')
 where lms_member_1_zdb_id  ='ZDB-GENE-130530-538';

update linkage_membership_Search
 set lms_member_2_zdb_id = replace(lms_member_2_zdb_id, 'GENE','LINCRNAG')
 where lms_member_2_zdb_id  ='ZDB-GENE-130530-538';

update mapped_marker
 set marker_id  = replace(marker_id, 'GENE','LINCRNAG')
 where marker_id='ZDB-GENE-130530-538';

update db_link
 set dblink_linked_recid = replace(dblink_linked_recid, 'GENE','LINCRNAG')
 where dblink_linked_recid ='ZDB-GENE-130530-538';

update data_note
 set dnote_data_zdb_id = replace(dnote_data_zdb_id, 'GENE','LINCRNAG')
 where dnote_data_zdb_id ='ZDB-GENE-130530-538';

update unique_location 
  set ul_data_zdb_id = replace(ul_data_zdb_id, 'GENE','LINCRNAG')
 where ul_data_zdb_id ='ZDB-GENE-130530-538';

update ortholog
  set ortho_zebrafish_gene_zdb_id = replace(ortho_zebrafish_gene_zdb_id, 'GENE','LINCRNAG')
 where ortho_zebrafish_gene_zdb_id ='ZDB-GENE-130530-538';

update snp_download
  set snpd_mrkr_zdb_id = replace(snpd_mrkr_zdb_id, 'GENE','LINCRNAG')
 where snpd_mrkr_zdb_id ='ZDB-GENE-130530-538';

update marker_go_term_evidence
  set mrkrgoev_mrkr_zdb_id = replace(mrkrgoev_mrkr_zdb_id, 'GENE','LINCRNAG')
 where mrkrgoev_mrkr_zdb_id ='ZDB-GENE-130530-538';

update linkage_member
 set lnkgmem_member_zdb_id = replace(lnkgmem_member_zdb_id, 'GENE','LINCRNAG')
where lnkgmem_member_zdb_id ='ZDB-GENE-130530-538';

update linkage_membership
 set lnkgm_member_1_zdb_id = replace(lnkgm_member_1_zdb_id, 'GENE','LINCRNAG')
 where lnkgm_member_1_zdb_id ='ZDB-GENE-130530-538';

update linkage_membership
 set lnkgm_member_2_zdb_id = replace(lnkgm_member_2_zdb_id, 'GENE','LINCRNAG')
 where lnkgm_member_2_zdb_id ='ZDB-GENE-130530-538';

update linkage_pair_member
  set lpmem_member_zdb_id = replace(lpmem_member_zdb_id, 'GENE','LINCRNAG')
 where lpmem_member_zdb_id ='ZDB-GENE-130530-538';

update linkage_single
 set lsingle_member_zdb_id = replace(lsingle_member_zdb_id, 'GENE','LINCRNAG')
 where lsingle_member_zdb_id ='ZDB-GENE-130530-538';

update updates
  set rec_id = replace(rec_id, 'GENE','LINCRNAG')
 where rec_id ='ZDB-GENE-130530-538';

update primer_set
  set marker_id = replace(marker_id, 'GENE','LINCRNAG' )
where  marker_id ='ZDB-GENE-130530-538';

update genedom_family_member
  set gfammem_mrkr_zdb_id = replace(gfammem_mrkr_zdb_id , 'GENE','LINCRNAG')
where gfammem_mrkr_zdb_id ='ZDB-GENE-130530-538';

update clone
  set clone_mrkr_zdb_id = replace(clone_mrkr_zdb_id , 'GENE','LINCRNAG')
where clone_mrkr_zdb_id  ='ZDB-GENE-130530-538';

update construct_marker_relationship
  set conmrkrrel_mrkr_zdb_id = replace( conmrkrrel_mrkr_zdb_id, 'GENE','LINCRNAG')
where conmrkrrel_mrkr_zdb_id ='ZDB-GENE-130530-538';

update marker_sequence
  set seq_mrkr_Zdb_id = replace(seq_mrkr_Zdb_id , 'GENE','LINCRNAG')
where seq_mrkr_Zdb_id ='ZDB-GENE-130530-538';

update clean_expression_fast_search
  set cefs_mrkr_zdb_id = replace(cefs_mrkr_zdb_id , 'GENE','LINCRNAG')
where cefs_mrkr_zdb_id ='ZDB-GENE-130530-538';

update mutant_fast_search
  set mfs_mrkr_zdb_id = replace(mfs_mrkr_zdb_id , 'GENE','LINCRNAG')
where mfs_mrkr_zdb_id ='ZDB-GENE-130530-538';

update sequence_feature_chromosome_location_generated
   set sfclg_data_zdb_id = replace(sfclg_data_zdb_id , 'GENE','LINCRNAG')
 where sfclg_data_zdb_id ='ZDB-GENE-130530-538';

set session_replication_role = 'origin';


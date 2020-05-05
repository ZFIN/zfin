--liquibase formatted sql
--changeset pm:ZFIN-6513


set session_replication_role = 'replica';




update zdb_active_data
 set zactvd_zdb_id = replace(zactvd_zdb_id,  'GENE','BR')
 where zactvd_zdb_id in ('ZDB-GENE-071004-117');

insert into zdb_replaced_data (zrepld_new_zdb_id, zrepld_old_zdb_id)
 values('ZDB-BR-071004-117', 'ZDB-GENE-071004-117');
 


insert into withdrawn_data (wd_new_zdb_id, wd_old_zdb_id)
 values('ZDB-BR-071004-117', 'ZDB-GENE-071004-117');
 

  
update marker
 set mrkr_type = 'BR'
 where mrkr_type = 'GENE' 
 and mrkr_zdb_id ='ZDB-GENE-071004-117';

update marker
 set mrkr_zdb_id = replace(mrkr_Zdb_id, 'GENE', 'BR')
 where mrkr_zdb_id ='ZDB-GENE-071004-117';

update marker
set mrkr_abbrev='br.line.zfl2-1'
where mrkr_zdb_id='ZDB-BR-071004-117';

update marker
set mrkr_name='line element zfl2-1'
where mrkr_zdb_id='ZDB-BR-071004-117';

update zmap_pub_pan_mark
 set zdb_id = replace(zdb_id, 'GENE', 'BR')
 where  zdb_id ='ZDB-GENE-071004-117';

update external_note
 set extnote_data_zdb_id = replace(extnote_data_zdb_id, 'GENE', 'BR')
 where extnote_data_zdb_id ='ZDB-GENE-071004-117';

update paneled_markers
   set zdb_id = replace(zdb_id, 'GENE', 'BR')
 where zdb_id ='ZDB-GENE-071004-117';

update sequence_feature_chromosome_location
   set sfcl_feature_Zdb_id = replace( sfcl_feature_Zdb_id, 'GENE', 'BR')
 where sfcl_feature_Zdb_id ='ZDB-GENE-071004-117';

update record_attribution
 set recattrib_data_zdb_id = replace(recattrib_data_zdb_id, 'GENE', 'BR')
 where recattrib_data_zdb_id ='ZDB-GENE-071004-117';

 update marker_to_protein
 set mtp_mrkr_zdb_id=replace(mtp_mrkr_zdb_id, 'GENE', 'BR')
 where mtp_mrkr_zdb_id ='ZDB-GENE-071004-117';

update expression_experiment2
 set xpatex_gene_zdb_id = replace(xpatex_gene_zdb_id, 'GENE', 'BR')
 where xpatex_gene_zdb_id ='ZDB-GENE-071004-117';

update feature_marker_relationship
 set fmrel_mrkr_zdb_id = replace(fmrel_mrkr_zdb_id , 'GENE', 'BR')
 where fmrel_mrkr_zdb_id ='ZDB-GENE-071004-117';

update marker_relationship
 set mrel_mrkr_1_zdb_id = replace(mrel_mrkr_1_zdb_id, 'GENE', 'BR')
 where mrel_mrkr_1_zdb_id ='ZDB-GENE-071004-117';

update construct_component
 set cc_component_zdb_id  = replace(cc_component_zdb_id, 'GENE', 'BR')
 where cc_component_zdb_id ='ZDB-GENE-071004-117';

update marker_relationship
 set mrel_mrkr_2_zdb_id = replace(mrel_mrkr_2_zdb_id, 'GENE', 'BR')
 where mrel_mrkr_2_zdb_id ='ZDB-GENE-071004-117';

update data_alias
 set dalias_data_zdb_id = replace(dalias_data_zdb_id, 'GENE', 'BR')
 where dalias_data_zdb_id='ZDB-GENE-071004-117';

update marker_history
 set mhist_mrkr_zdb_id = replace(mhist_mrkr_zdb_id,  'GENE', 'BR')
 where mhist_mrkr_zdb_id ='ZDB-GENE-071004-117';

update marker_history_audit
 set mha_mrkr_zdb_id = replace(mha_mrkr_zdb_id, 'GENE', 'BR')
 where mha_mrkr_zdb_id ='ZDB-GENE-071004-117';

update zdb_orphan_data
 set zorphand_zdb_id = replace(zorphand_zdb_id, 'GENE', 'BR')
 where zorphand_zdb_id ='ZDB-GENE-071004-117';

update external_reference
 set exref_data_zdb_id = replace(exref_data_zdb_id, 'GENE', 'BR')
 where exref_data_zdb_id ='ZDB-GENE-071004-117';

update int_data_source
 set ids_data_zdb_id = replace(ids_data_zdb_id, 'GENE', 'BR')
 where ids_data_zdb_id ='ZDB-GENE-071004-117';

update linkage_membership_Search
 set lms_member_1_zdb_id = replace(lms_member_1_zdb_id, 'GENE', 'BR')
 where lms_member_1_zdb_id  ='ZDB-GENE-071004-117';

update linkage_membership_Search
 set lms_member_2_zdb_id = replace(lms_member_2_zdb_id, 'GENE', 'BR')
 where lms_member_2_zdb_id  ='ZDB-GENE-071004-117';

update mapped_marker
 set marker_id  = replace(marker_id, 'GENE', 'BR')
 where marker_id='ZDB-GENE-071004-117';

update db_link
 set dblink_linked_recid = replace(dblink_linked_recid, 'GENE', 'BR')
 where dblink_linked_recid ='ZDB-GENE-071004-117';

update data_note
 set dnote_data_zdb_id = replace(dnote_data_zdb_id, 'GENE', 'BR')
 where dnote_data_zdb_id ='ZDB-GENE-071004-117';

update unique_location 
  set ul_data_zdb_id = replace(ul_data_zdb_id, 'GENE', 'BR')
 where ul_data_zdb_id ='ZDB-GENE-071004-117';

update ortholog
  set ortho_zebrafish_gene_zdb_id = replace(ortho_zebrafish_gene_zdb_id, 'GENE', 'BR')
 where ortho_zebrafish_gene_zdb_id ='ZDB-GENE-071004-117';

update snp_download
  set snpd_mrkr_zdb_id = replace(snpd_mrkr_zdb_id, 'GENE', 'BR')
 where snpd_mrkr_zdb_id ='ZDB-GENE-071004-117';

update marker_go_term_evidence
  set mrkrgoev_mrkr_zdb_id = replace(mrkrgoev_mrkr_zdb_id, 'GENE', 'BR')
 where mrkrgoev_mrkr_zdb_id ='ZDB-GENE-071004-117';

update linkage_member
 set lnkgmem_member_zdb_id = replace(lnkgmem_member_zdb_id, 'GENE', 'BR')
where lnkgmem_member_zdb_id ='ZDB-GENE-071004-117';

update linkage_membership
 set lnkgm_member_1_zdb_id = replace(lnkgm_member_1_zdb_id, 'GENE', 'BR')
 where lnkgm_member_1_zdb_id ='ZDB-GENE-071004-117';

update linkage_membership
 set lnkgm_member_2_zdb_id = replace(lnkgm_member_2_zdb_id, 'GENE', 'BR')
 where lnkgm_member_2_zdb_id ='ZDB-GENE-071004-117';

update linkage_pair_member
  set lpmem_member_zdb_id = replace(lpmem_member_zdb_id, 'GENE', 'BR')
 where lpmem_member_zdb_id ='ZDB-GENE-071004-117';

update linkage_single
 set lsingle_member_zdb_id = replace(lsingle_member_zdb_id, 'GENE', 'BR')
 where lsingle_member_zdb_id ='ZDB-GENE-071004-117';

update updates
  set rec_id = replace(rec_id, 'GENE', 'BR')
 where rec_id ='ZDB-GENE-071004-117';

update primer_set
  set marker_id = replace(marker_id, 'GENE','LINCRNAG' )
where  marker_id ='ZDB-GENE-071004-117';

update genedom_family_member
  set gfammem_mrkr_zdb_id = replace(gfammem_mrkr_zdb_id , 'GENE', 'BR')
where gfammem_mrkr_zdb_id ='ZDB-GENE-071004-117';

update clone
  set clone_mrkr_zdb_id = replace(clone_mrkr_zdb_id , 'GENE', 'BR')
where clone_mrkr_zdb_id  ='ZDB-GENE-071004-117';

update construct_marker_relationship
  set conmrkrrel_mrkr_zdb_id = replace( conmrkrrel_mrkr_zdb_id, 'GENE', 'BR')
where conmrkrrel_mrkr_zdb_id ='ZDB-GENE-071004-117';

update marker_sequence
  set seq_mrkr_Zdb_id = replace(seq_mrkr_Zdb_id , 'GENE', 'BR')
where seq_mrkr_Zdb_id ='ZDB-GENE-071004-117';

update clean_expression_fast_search
  set cefs_mrkr_zdb_id = replace(cefs_mrkr_zdb_id , 'GENE', 'BR')
where cefs_mrkr_zdb_id ='ZDB-GENE-071004-117';

update mutant_fast_search
  set mfs_mrkr_zdb_id = replace(mfs_mrkr_zdb_id , 'GENE', 'BR')
where mfs_mrkr_zdb_id ='ZDB-GENE-071004-117';

update sequence_feature_chromosome_location_generated
   set sfclg_data_zdb_id = replace(sfclg_data_zdb_id , 'GENE', 'BR')
 where sfclg_data_zdb_id ='ZDB-GENE-071004-117';

set session_replication_role = 'origin';

begin work;

set constraints all deferred;

update marker_relationship_type
 set mreltype_mrkr_type_group_1 = 'GENEDOM'
 where mreltype_mrkr_type_group_1 = 'GENEDOM_PROD_PROTEIN'
and mreltype_name = 'gene produces transcript';

select mrkr_zdb_id as gene_id
 from marker
 where mrkr_abbrev in ('rnu6-1','rnu6-4',
'rnu6atac',
'rnu6-z2',
'rnu6-z3', 'rnu6-32')
into temp tmp_to_convert_start;

select get_id('NCRNAG') as rna_id, gene_id
  from tmp_to_convert_start
into tmp_to_convert;

update zdb_replaced_data
 set zrepld_old_zdb_id = (select rna_id from tmp_to_convert where zrepld_old_zdb_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where gene_id = zrepld_old_zdb_id);

update marker
 set mrkr_type = 'NCRNAG'
 where mrkr_type = 'GENE'
 and exists (Select 'x' from tmp_to_convert1 where mrkr_zdb_id = gene_id);

update marker
 set mrkr_zdb_id = (select rna_id from tmp_to_convert where mrkr_zdb_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where gene_id = mrkr_zdb_id);

update zdb_replaced_data
 set zrepld_new_zdb_id = (select rna_id from tmp_to_convert where zrepld_new_zdb_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where zrepld_new_zdb_id = gene_id);


update withdrawn_data 
 set wd_old_zdb_id = (select rna_id from tmp_to_convert where wd_old_zdb_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where wd_old_zdb_id = gene_id);


update withdrawn_data 
 set wd_new_zdb_id = (select rna_id from tmp_to_convert where wd_new_zdb_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where wd_new_zdb_id = gene_id);

insert into zdb_replaced_data (zrepld_new_zdb_id, zrepld_old_zdb_id)
 select rna_id, gene_id
  from tmp_to_convert;

insert into withdrawn_data (wd_old_zdb_id, wd_new_zdb_id)
 select rna_id, gene_id
  from tmp_to_convert;

update zdb_active_data
 set zactvd_zdb_id = (select rna_id from tmp_to_convert where zactvd_zdb_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where zactvd_zdb_id = gene_id);

update marker
 set mrkr_type = 'NCRNAG'
 where mrkr_type = 'GENE'
 and exists (Select 'x' from tmp_to_convert where mrkr_zdb_id = gene_id);

update zmap_pub_pan_mark
 set zdb_id = (select rna_id from tmp_to_convert where zdb_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where  zdb_id = gene_id);

update external_note
 set extnote_data_zdb_id = (select rna_id from tmp_to_convert where extnote_data_zdb_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where extnote_data_zdb_id = gene_id);


update paneled_markers
 set  zdb_id = (select rna_id from tmp_to_convert where zdb_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where zdb_id = gene_id);

update all_map_names
 set allmapnm_zdb_id = (select rna_id from tmp_to_convert where allmapnm_zdb_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where allmapnm_zdb_id = gene_id);

update sequence_feature_chromosome_location
 set sfcl_feature_Zdb_id = (select rna_id from tmp_to_convert where sfcl_feature_zdb_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where  sfcl_feature_Zdb_id = gene_id);


update record_attribution
 set recattrib_data_zdb_id = (select rna_id from tmp_to_convert where recattrib_data_zdb_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where recattrib_data_zdb_id = gene_id)
   ;

update expression_experiment2
 set xpatex_gene_zdb_id = (select rna_id from tmp_to_convert where xpatex_gene_zdb_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where xpatex_gene_zdb_id = gene_id);

update construct_component
 set cc_component_zdb_id  = (select rna_id from tmp_to_convert where cc_component_zdb_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where cc_component_zdb_id = gene_id);

--CONSTRUCT_COMPONENTS
--GENEDOM

update feature_marker_relationship
 set fmrel_mrkr_zdb_id = (select rna_id from tmp_to_convert where fmrel_mrkr_zdb_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where fmrel_mrkr_zdb_id = gene_id);

update marker_Relationship
 set mrel_mrkr_1_zdb_id = (select rna_id from tmp_to_convert where mrel_mrkr_1_zdb_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where mrel_mrkr_1_zdb_id = gene_id);

update marker_relationship
 set mrel_mrkr_2_zdb_id = (select rna_id from tmp_to_convert where mrel_mrkr_2_zdb_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where mrel_mrkr_2_zdb_id = gene_id);

update data_alias
 set dalias_data_zdb_id = (select rna_id from tmp_to_convert where dalias_data_zdb_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where dalias_data_zdb_id = gene_id);

update marker_history
 set mhist_mrkr_zdb_id = (select rna_id from tmp_to_convert where mhist_mrkr_zdb_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where mhist_mrkr_zdb_id = gene_id);

update marker_history_audit
 set mha_mrkr_zdb_id = (select rna_id from tmp_to_convert where mha_mrkr_zdb_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where mha_mrkr_zdb_id = gene_id);

update zdb_orphan_data
 set zorphand_zdb_id = (select rna_id from tmp_to_convert where zorphand_zdb_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where zorphand_zdb_id  = gene_id);

update external_reference
 set exref_data_zdb_id = (select rna_id from tmp_to_convert where exref_data_zdb_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where exref_data_zdb_id = gene_id);

update int_data_source
 set ids_data_zdb_id = (select rna_id from tmp_to_convert where ids_data_zdb_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where ids_data_zdb_id = gene_id);

update linkage_membership_Search
 set lms_member_1_zdb_id = (select rna_id from tmp_to_convert where lms_member_1_zdb_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where lms_member_1_zdb_id  = gene_id);

update linkage_membership_Search
 set lms_member_2_zdb_id = (select rna_id from tmp_to_convert where lms_member_2_zdb_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where lms_member_2_zdb_id  = gene_id);

update mapped_marker
 set marker_id  = (select rna_id from tmp_to_convert where marker_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where marker_id = gene_id);



update db_link
 set dblink_linked_recid = (select rna_id from tmp_to_convert where dblink_linked_recid = gene_id)
 where exists (Select 'x' from tmp_to_convert where dblink_linked_recid = gene_id);

update data_note
 set dnote_data_zdb_id = (select rna_id from tmp_to_convert where dnote_data_zdb_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where dnote_data_zdb_id = gene_id);

update unique_location 
  set ul_data_zdb_id = (select rna_id from tmp_to_convert where ul_data_zdb_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where ul_data_zdb_id = gene_id);

update ortholog
  set ortho_zebrafish_gene_zdb_id = (select rna_id from tmp_to_convert where ortho_zebrafish_gene_zdb_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where ortho_zebrafish_gene_zdb_id = gene_id);

update snp_download
  set snpd_mrkr_zdb_id = (select rna_id from tmp_to_convert where snpd_mrkr_Zdb_id = gene_id)
 where exists (select 'x' from tmp_to_convert where snpd_mrkr_zdb_id = gene_id);

update marker_go_term_evidence
  set mrkrgoev_mrkr_zdb_id = (select rna_id from tmp_to_convert where mrkrgoev_mrkr_Zdb_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where mrkrgoev_mrkr_zdb_id = gene_id);

update linkage_member
 set lnkgmem_member_zdb_id = (select rna_id from tmp_to_convert where lnkgmem_member_Zdb_id = gene_id)
where exists (Select 'x' from tmp_to_convert where lnkgmem_member_zdb_id = gene_id);

update linkage_membership
 set lnkgm_member_1_zdb_id = (select rna_id from tmp_to_convert where lnkgm_member_1_zdb_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where lnkgm_member_1_zdb_id = gene_id);

update linkage_membership
 set lnkgm_member_2_zdb_id = (select rna_id from tmp_to_convert where lnkgm_member_2_zdb_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where lnkgm_member_2_zdb_id = gene_id);

update linkage_pair_member
  set lpmem_member_zdb_id = (select rna_id from tmp_to_convert where lpmem_member_zdb_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where lpmem_member_zdb_id = gene_id);

update linkage_single
 set lsingle_member_zdb_id = (select rna_id from tmp_to_convert where lsingle_member_zdb_id = gene_id)
 where exists (Select 'x' from tmp_to_convert where lsingle_member_zdb_id = gene_id);

update updates
  set rec_id = (select rna_id from tmp_to_convert where rec_id = gene_id)
 where exists (select 'x' from tmp_to_Convert where rec_id = gene_id);

update primer_set
  set marker_id = (select rna_id from tmp_to_convert where marker_id = gene_id)
where exists (Select 'x' from tmp_to_Convert where  marker_id = gene_id);

update genedom_family_member
  set gfammem_mrkr_zdb_id = (select rna_id from tmp_to_convert where gfammem_mrkr_zdb_id = gene_id)
where exists (Select 'x' from tmp_to_Convert where gfammem_mrkr_zdb_id  = gene_id);

update clone
  set clone_mrkr_zdb_id = (select rna_id from tmp_to_convert where clone_mrkr_zdb_id = gene_id)
where exists (Select 'x' from tmp_to_Convert where clone_mrkr_zdb_id  = gene_id);

update construct_marker_relationship
  set conmrkrrel_mrkr_zdb_id = (select rna_id from tmp_to_convert where conmrkrrel_mrkr_zdb_id = gene_id)
where exists (Select 'x' from tmp_to_Convert where conmrkrrel_mrkr_zdb_id  = gene_id);

update marker_sequence
  set seq_mrkr_Zdb_id = (select rna_id from tmp_to_convert where seq_mrkr_zdb_id = gene_id)
where exists (Select 'x' from tmp_to_Convert where seq_mrkr_Zdb_id  = gene_id);


update mutant_fast_search
  set mfs_mrkr_Zdb_id = (select rna_id from tmp_to_convert where mfs_mrkr_zdb_id = gene_id)
where exists (Select 'x' from tmp_to_Convert where mfs_mrkr_Zdb_id  = gene_id);

update clean_expression_fast_search
 set cefs_mrkr_zdb_id = (select rna_id from tmp_to_convert where cefs_mrkr_zdb_id = gene_id)
where exists (Select 'x' from tmp_to_Convert where cefs_mrkr_Zdb_id  = gene_id);



set constraints all immediate;

--rollback work;

commit work;

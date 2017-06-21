begin work;

set constraints all deferred;


update marker_relationship_type
 set mreltype_mrkr_type_group_1 = 'GENEDOM'
 where mreltype_mrkr_type_group_1 = 'GENEDOM_PROD_PROTEIN'
and mreltype_name = 'gene produces transcript';

select mrkr_zdb_id as gene_id
 from marker
 where mrkr_zdb_id in (
'ZDB-GENE-990714-17',
'ZDB-GENE-990714-18',
'ZDB-GENE-011205-25',
'ZDB-GENE-011205-27',
'ZDB-GENE-011205-30',
'ZDB-GENE-011205-37',
'ZDB-GENE-011205-2',
'ZDB-GENE-011205-32',
'ZDB-GENE-011205-34',
'ZDB-GENE-011205-21',
'ZDB-GENE-011205-31',
'ZDB-GENE-011205-23',
'ZDB-GENE-011205-26',
'ZDB-GENE-011205-38',
'ZDB-GENE-011205-22',
'ZDB-GENE-011205-33',
'ZDB-GENE-011205-39',
'ZDB-GENE-011205-5',
'ZDB-GENE-011205-24',
'ZDB-GENE-011205-28',
'ZDB-GENE-011205-6',
'ZDB-GENE-011205-36',
'ZDB-GENE-011205-29',
'ZDB-GENE-011205-35')
into temp tmp_to_convert1;


update zdb_replaced_data
 set zrepld_old_zdb_id = replace(zrepld_old_zdb_id, 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where zrepld_old_zdb_id = gene_id);

update zdb_replaced_data
 set zrepld_new_zdb_id = replace(zrepld_new_zdb_id, 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where zrepld_new_zdb_id = gene_id);

update withdrawn_data 
 set wd_old_zdb_id = replace(wd_old_Zdb_id, 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where wd_old_zdb_id = gene_id);

update withdrawn_data 
 set wd_new_zdb_id = replace(wd_new_Zdb_id, 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where wd_new_zdb_id = gene_id);

insert into zdb_replaced_data (zrepld_new_zdb_id, zrepld_old_zdb_id)
 select replace(gene_id, 'GENE','SNORNAG'), gene_id
  from tmp_to_convert1;

insert into withdrawn_data (wd_old_zdb_id, wd_new_zdb_id)
 select replace(gene_id, 'GENE','SNORNAG'), gene_id
  from tmp_to_convert1;

update zdb_active_data
 set zactvd_zdb_id = replace(zactvd_zdb_id, 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where zactvd_zdb_id = gene_id);

update marker
 set mrkr_zdb_id = replace(mrkr_Zdb_id, 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where mrkr_zdb_id = gene_id);

update marker
 set mrkr_type = 'SNORNAG'
 where mrkr_type = 'GENE'
 and exists (Select 'x' from tmp_to_convert1 where mrkr_zdb_id = gene_id);

update zmap_pub_pan_mark
 set zdb_id = replace(zdb_id, 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where  zdb_id = gene_id);

update external_note
 set extnote_data_zdb_id = replace(extnote_data_zdb_id, 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where extnote_data_zdb_id = gene_id);


update paneled_markers
 set  zdb_id = replace(zdb_id, 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where zdb_id = gene_id);

update all_map_names
 set allmapnm_zdb_id = replace(allmapnm_zdb_id, 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where allmapnm_zdb_id = gene_id);

update sequence_feature_chromosome_location
 set sfcl_feature_Zdb_id = replace( sfcl_feature_Zdb_id, 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where  sfcl_feature_Zdb_id = gene_id);


update record_attribution
 set recattrib_data_zdb_id = replace(recattrib_data_zdb_id, 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where recattrib_data_zdb_id = gene_id);

update expression_experiment2
 set xpatex_gene_zdb_id = replace(xpatex_gene_zdb_id, 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where xpatex_gene_zdb_id = gene_id);

update feature_marker_relationship
 set fmrel_mrkr_zdb_id = replace(fmrel_mrkr_zdb_id , 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where fmrel_mrkr_zdb_id = gene_id);

update marker_Relationship
 set mrel_mrkr_1_zdb_id = replace(mrel_mrkr_1_zdb_id, 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where mrel_mrkr_1_zdb_id = gene_id);

select distinct mrel_type from marker_relationship
 where mrel_mrkr_1_zdb_id in (select gene_id from tmp_to_convert1 where mrel_mrkr_1_zdb_id = gene_id);

select count(*) from marker_relationship
 where mrel_mrkr_1_zdb_id in (select gene_id from tmp_to_convert1 where mrel_mrkr_1_zdb_id = gene_id);


update construct_component
 set cc_component_zdb_id  = replace(cc_component_zdb_id, 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where cc_component_zdb_id = gene_id);

--CONSTRUCT_COMPONENTS
--GENEDOM


update marker_Relationship
 set mrel_mrkr_2_zdb_id = replace(mrel_mrkr_2_zdb_id, 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where mrel_mrkr_2_zdb_id = gene_id);

update data_alias
 set dalias_data_zdb_id = replace(dalias_data_zdb_id, 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where dalias_data_zdb_id = gene_id);

update marker_history
 set mhist_mrkr_zdb_id = replace(mhist_mrkr_zdb_id,  'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where mhist_mrkr_zdb_id = gene_id);

update marker_history_audit
 set mha_mrkr_zdb_id = replace(mha_mrkr_zdb_id, 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where mha_mrkr_zdb_id = gene_id);

update zdb_orphan_data
 set zorphand_zdb_id = replace(zorphand_zdb_id, 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where zorphand_zdb_id  = gene_id);

update external_reference
 set exref_data_zdb_id = replace(exref_data_zdb_id, 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where exref_data_zdb_id = gene_id);

update int_data_source
 set ids_data_zdb_id = replace(ids_data_zdb_id, 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where ids_data_zdb_id = gene_id);

update linkage_membership_Search
 set lms_member_1_zdb_id = replace(lms_member_1_zdb_id, 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where lms_member_1_zdb_id  = gene_id);

update linkage_membership_Search
 set lms_member_2_zdb_id = replace(lms_member_2_zdb_id, 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where lms_member_2_zdb_id  = gene_id);

update mapped_marker
 set marker_id  = replace(marker_id, 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where marker_id = gene_id);



update db_link
 set dblink_linked_recid = replace(dblink_linked_recid, 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where dblink_linked_recid = gene_id);

update data_note
 set dnote_data_zdb_id = replace(dnote_data_zdb_id, 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where dnote_data_zdb_id = gene_id);

update unique_location 
  set ul_data_zdb_id = replace(ul_data_zdb_id, 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where ul_data_zdb_id = gene_id);

update ortholog
  set ortho_zebrafish_gene_zdb_id = replace(ortho_zebrafish_gene_zdb_id, 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where ortho_zebrafish_gene_zdb_id = gene_id);

update snp_download
  set snpd_mrkr_zdb_id = replace(snpd_mrkr_zdb_id, 'GENE','SNORNAG')
 where exists (select 'x' from tmp_to_convert1 where snpd_mrkr_zdb_id = gene_id);

update marker_go_term_evidence
  set mrkrgoev_mrkr_zdb_id = replace(mrkrgoev_mrkr_zdb_id, 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where mrkrgoev_mrkr_zdb_id = gene_id);

update linkage_member
 set lnkgmem_member_zdb_id = replace(lnkgmem_member_zdb_id, 'GENE','SNORNAG')
where exists (Select 'x' from tmp_to_convert1 where lnkgmem_member_zdb_id = gene_id);

update linkage_membership
 set lnkgm_member_1_zdb_id = replace(lnkgm_member_1_zdb_id, 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where lnkgm_member_1_zdb_id = gene_id);

update linkage_membership
 set lnkgm_member_2_zdb_id = replace(lnkgm_member_2_zdb_id, 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where lnkgm_member_2_zdb_id = gene_id);

update linkage_pair_member
  set lpmem_member_zdb_id = replace(lpmem_member_zdb_id, 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where lpmem_member_zdb_id = gene_id);

update linkage_single
 set lsingle_member_zdb_id = replace(lsingle_member_zdb_id, 'GENE','SNORNAG')
 where exists (Select 'x' from tmp_to_convert1 where lsingle_member_zdb_id = gene_id);

update updates
  set rec_id = replace(rec_id, 'GENE','SNORNAG')
 where exists (select 'x' from tmp_to_convert1 where rec_id = gene_id);

update primer_set
  set marker_id = replace(marker_id, 'GENE','SNORNAG' )
where exists (Select 'x' from tmp_to_convert1 where  marker_id = gene_id);

update genedom_family_member
  set gfammem_mrkr_zdb_id = replace(gfammem_mrkr_zdb_id , 'GENE','SNORNAG')
where exists (Select 'x' from tmp_to_convert1 where gfammem_mrkr_zdb_id  = gene_id);

update clone
  set clone_mrkr_zdb_id = replace(clone_mrkr_zdb_id , 'GENE','SNORNAG')
where exists (Select 'x' from tmp_to_convert1 where clone_mrkr_zdb_id  = gene_id);

update construct_marker_relationship
  set conmrkrrel_mrkr_zdb_id = replace( conmrkrrel_mrkr_zdb_id, 'GENE','SNORNAG')
where exists (Select 'x' from tmp_to_convert1 where conmrkrrel_mrkr_zdb_id  = gene_id);

update marker_sequence
  set seq_mrkr_Zdb_id = replace(seq_mrkr_Zdb_id , 'GENE','SNORNAG')
where exists (Select 'x' from tmp_to_convert1 where seq_mrkr_Zdb_id  = gene_id);

update clean_expression_fast_search
  set cefs_mrkr_zdb_id = replace(cefs_mrkr_zdb_id , 'GENE','SNORNAG')
where exists (Select 'x' from tmp_to_convert1 where cefs_mrkr_zdb_id  = gene_id);

update mutant_fast_search
  set mfs_mrkr_zdb_id = replace(mfs_mrkr_zdb_id , 'GENE','SNORNAG')
where exists (Select 'x' from tmp_to_convert1 where mfs_mrkr_zdb_id  = gene_id);


select geno_zdb_id
 from genotype
 where geno_handle is null;

select fish_zdb_id, fish_name from fish
 where fish_name_order is null;

select * from genotype
 where geno_display_name is null;



set constraints all immediate;

--rollback work;

commit work;

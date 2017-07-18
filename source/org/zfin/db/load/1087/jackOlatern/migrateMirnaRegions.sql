begin work;

set constraints all deferred;


select count(*) from zdb_replaced_data
where zrepld_new_zdb_id = zrepld_old_zdb_id;


update marker_relationship_type
 set mreltype_mrkr_type_group_1 = 'GENEDOM'
 where mreltype_mrkr_type_group_1 = 'GENEDOM_PROD_PROTEIN'
and mreltype_name = 'gene produces transcript';

select mrkr_zdb_id as gene_id
 from marker
 where mrkr_name like 'microRNA%'
and mrkr_type = 'GENE'
into temp tmp_to_convert;


update zdb_replaced_data
 set zrepld_old_zdb_id = replace(zrepld_old_zdb_id, 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where zrepld_old_zdb_id = gene_id);

update zdb_replaced_data
 set zrepld_new_zdb_id = replace(zrepld_new_zdb_id, 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where zrepld_new_zdb_id = gene_id);

update withdrawn_data 
 set wd_old_zdb_id = replace(wd_old_Zdb_id, 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where wd_old_zdb_id = gene_id);

update withdrawn_data 
 set wd_new_zdb_id = replace(wd_new_Zdb_id, 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where wd_new_zdb_id = gene_id);

insert into zdb_replaced_data (zrepld_new_zdb_id, zrepld_old_zdb_id)
 select replace(gene_id, 'GENE','MIRNAG'), gene_id
  from tmp_to_convert;

insert into withdrawn_data (wd_old_zdb_id, wd_new_zdb_id)
 select replace(gene_id, 'GENE','MIRNAG'), gene_id
  from tmp_to_convert;


select * from zdb_replaced_data, marker
where zrepld_new_zdb_id = zrepld_old_zdb_id
 and mrkr_zdb_id = zrepld_new_zdb_id;

update zdb_active_data
 set zactvd_zdb_id = replace(zactvd_zdb_id, 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where zactvd_zdb_id = gene_id);

update marker
 set mrkr_type = 'MIRNAG'
 where mrkr_type = 'GENE'
 and exists (Select 'x' from tmp_to_convert where mrkr_zdb_id = gene_id);

update marker
 set mrkr_zdb_id = replace(mrkr_Zdb_id, 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where mrkr_zdb_id = gene_id);

update marker
 set mrkr_type = 'MIRNAG'
 where mrkr_type = 'GENE'
 and exists (Select 'x' from tmp_to_convert where mrkr_zdb_id = gene_id);

update zmap_pub_pan_mark
 set zdb_id = replace(zdb_id, 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where  zdb_id = gene_id);

update external_note
 set extnote_data_zdb_id = replace(extnote_data_zdb_id, 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where extnote_data_zdb_id = gene_id);


update paneled_markers
 set  zdb_id = replace(zdb_id, 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where zdb_id = gene_id);

update all_map_names
 set allmapnm_zdb_id = replace(allmapnm_zdb_id, 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where allmapnm_zdb_id = gene_id);

update sequence_feature_chromosome_location
 set sfcl_feature_Zdb_id = replace( sfcl_feature_Zdb_id, 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where  sfcl_feature_Zdb_id = gene_id);


update record_attribution
 set recattrib_data_zdb_id = replace(recattrib_data_zdb_id, 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where recattrib_data_zdb_id = gene_id);

update expression_experiment2
 set xpatex_gene_zdb_id = replace(xpatex_gene_zdb_id, 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where xpatex_gene_zdb_id = gene_id);

update feature_marker_relationship
 set fmrel_mrkr_zdb_id = replace(fmrel_mrkr_zdb_id , 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where fmrel_mrkr_zdb_id = gene_id);

update marker_Relationship
 set mrel_mrkr_1_zdb_id = replace(mrel_mrkr_1_zdb_id, 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where mrel_mrkr_1_zdb_id = gene_id);

select distinct mrel_type from marker_relationship
 where mrel_mrkr_1_zdb_id in (select gene_id from tmp_to_convert where mrel_mrkr_1_zdb_id = gene_id);

select count(*) from marker_relationship
 where mrel_mrkr_1_zdb_id in (select gene_id from tmp_to_convert where mrel_mrkr_1_zdb_id = gene_id);


update construct_component
 set cc_component_zdb_id  = replace(cc_component_zdb_id, 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where cc_component_zdb_id = gene_id);

--CONSTRUCT_COMPONENTS
--GENEDOM


update marker_Relationship
 set mrel_mrkr_2_zdb_id = replace(mrel_mrkr_2_zdb_id, 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where mrel_mrkr_2_zdb_id = gene_id);

update data_alias
 set dalias_data_zdb_id = replace(dalias_data_zdb_id, 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where dalias_data_zdb_id = gene_id);

update marker_history
 set mhist_mrkr_zdb_id = replace(mhist_mrkr_zdb_id,  'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where mhist_mrkr_zdb_id = gene_id);

update marker_history_audit
 set mha_mrkr_zdb_id = replace(mha_mrkr_zdb_id, 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where mha_mrkr_zdb_id = gene_id);

update zdb_orphan_data
 set zorphand_zdb_id = replace(zorphand_zdb_id, 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where zorphand_zdb_id  = gene_id);

update external_reference
 set exref_data_zdb_id = replace(exref_data_zdb_id, 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where exref_data_zdb_id = gene_id);

update int_data_source
 set ids_data_zdb_id = replace(ids_data_zdb_id, 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where ids_data_zdb_id = gene_id);

update linkage_membership_Search
 set lms_member_1_zdb_id = replace(lms_member_1_zdb_id, 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where lms_member_1_zdb_id  = gene_id);

update linkage_membership_Search
 set lms_member_2_zdb_id = replace(lms_member_2_zdb_id, 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where lms_member_2_zdb_id  = gene_id);

update mapped_marker
 set marker_id  = replace(marker_id, 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where marker_id = gene_id);



update db_link
 set dblink_linked_recid = replace(dblink_linked_recid, 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where dblink_linked_recid = gene_id);

update data_note
 set dnote_data_zdb_id = replace(dnote_data_zdb_id, 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where dnote_data_zdb_id = gene_id);

update unique_location 
  set ul_data_zdb_id = replace(ul_data_zdb_id, 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where ul_data_zdb_id = gene_id);

update ortholog
  set ortho_zebrafish_gene_zdb_id = replace(ortho_zebrafish_gene_zdb_id, 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where ortho_zebrafish_gene_zdb_id = gene_id);

update snp_download
  set snpd_mrkr_zdb_id = replace(snpd_mrkr_zdb_id, 'GENE','MIRNAG')
 where exists (select 'x' from tmp_to_convert where snpd_mrkr_zdb_id = gene_id);

update marker_go_term_evidence
  set mrkrgoev_mrkr_zdb_id = replace(mrkrgoev_mrkr_zdb_id, 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where mrkrgoev_mrkr_zdb_id = gene_id);

update linkage_member
 set lnkgmem_member_zdb_id = replace(lnkgmem_member_zdb_id, 'GENE','MIRNAG')
where exists (Select 'x' from tmp_to_convert where lnkgmem_member_zdb_id = gene_id);

update linkage_membership
 set lnkgm_member_1_zdb_id = replace(lnkgm_member_1_zdb_id, 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where lnkgm_member_1_zdb_id = gene_id);

update linkage_membership
 set lnkgm_member_2_zdb_id = replace(lnkgm_member_2_zdb_id, 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where lnkgm_member_2_zdb_id = gene_id);

update linkage_pair_member
  set lpmem_member_zdb_id = replace(lpmem_member_zdb_id, 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where lpmem_member_zdb_id = gene_id);

update linkage_single
 set lsingle_member_zdb_id = replace(lsingle_member_zdb_id, 'GENE','MIRNAG')
 where exists (Select 'x' from tmp_to_convert where lsingle_member_zdb_id = gene_id);

update updates
  set rec_id = replace(rec_id, 'GENE','MIRNAG')
 where exists (select 'x' from tmp_to_Convert where rec_id = gene_id);

update primer_set
  set marker_id = replace(marker_id, 'GENE','MIRNAG' )
where exists (Select 'x' from tmp_to_Convert where  marker_id = gene_id);

update genedom_family_member
  set gfammem_mrkr_zdb_id = replace(gfammem_mrkr_zdb_id , 'GENE','MIRNAG')
where exists (Select 'x' from tmp_to_Convert where gfammem_mrkr_zdb_id  = gene_id);

update clone
  set clone_mrkr_zdb_id = replace(clone_mrkr_zdb_id , 'GENE','MIRNAG')
where exists (Select 'x' from tmp_to_Convert where clone_mrkr_zdb_id  = gene_id);

update construct_marker_relationship
  set conmrkrrel_mrkr_zdb_id = replace( conmrkrrel_mrkr_zdb_id, 'GENE','MIRNAG')
where exists (Select 'x' from tmp_to_Convert where conmrkrrel_mrkr_zdb_id  = gene_id);

update marker_sequence
  set seq_mrkr_Zdb_id = replace(seq_mrkr_Zdb_id , 'GENE','MIRNAG')
where exists (Select 'x' from tmp_to_Convert where seq_mrkr_Zdb_id  = gene_id);

update clean_expression_fast_search
  set cefs_mrkr_zdb_id = replace(cefs_mrkr_zdb_id , 'GENE','MIRNAG')
where exists (Select 'x' from tmp_to_Convert where cefs_mrkr_zdb_id  = gene_id);

update mutant_fast_search
  set mfs_mrkr_zdb_id = replace(mfs_mrkr_zdb_id , 'GENE','MIRNAG')
where exists (Select 'x' from tmp_to_Convert where mfs_mrkr_zdb_id  = gene_id);


update genotype
 set geno_handle = 'mq6[1,1,1]'
 where geno_zdb_id = 'ZDB-GENO-160609-8';

update genotype
 set geno_handle = 'AB'
 where geno_zdb_id = 'ZDB-GENO-960809-7';

update genotype
 set geno_handle = 'c442Tg[U,U,U]'
 where geno_zdb_id = 'ZDB-GENO-120820-3';

update genotype
 set geno_handle = 'w181[1,U,U]AB'
 where geno_zdb_id = 'ZDB-GENO-160613-1';

update genotype
 set geno_handle = 'zf643[2,1,1] zf660[2,1,1]TU'
 where geno_zdb_id = 'ZDB-GENO-161006-29';

update genotype
 set geno_handle = 'nz117Tg[U,U,U] zf643[2,1,1] zf660[2,1,1]TU'
 where geno_zdb_id = 'ZDB-GENO-161006-30';

update genotype
 set geno_handle = 'zf643[2,1,1]TU'
 where geno_zdb_id = 'ZDB-GENO-161006-28';

update genotype
 set geno_handle = 'ya301[2,1,1]'
 where geno_zdb_id = 'ZDB-GENO-150904-3';

select geno_zdb_id
 from genotype
 where geno_handle is null;

select fish_zdb_id, fish_name from fish
 where fish_name_order is null;

update fish
  set fish_name = 'mir142a<sup>zf643/zf643</sup> ; nz117Tg ; mir142b<sup>zf660/zf660</sup>'
 where fish_zdb_id = 'ZDB-FISH-161006-32';

update fish
  set fish_name = 'mir107<sup>ya301/ya301</sup>'
 where fish_zdb_id = 'ZDB-FISH-150904-3';

update fish
  set fish_name = 'mir142a<sup>zf643/zf643</sup>'
 where fish_zdb_id = 'ZDB-FISH-161006-30';

update fish
  set fish_name = 'mir142b<sup>zf660/zf660</sup> ; mir142a<sup>zf643/zf643</sup> + MO2-stat1a'
 where fish_zdb_id = 'ZDB-FISH-161007-8';

update fish
  set fish_name = 'mir142b<sup>zf660/zf660</sup> ; mir142a<sup>zf643/zf643</sup>'
 where fish_zdb_id = 'ZDB-FISH-161006-31';

update fish
  set fish_name = 'mir142b<sup>zf660/zf660</sup> ; mir142a<sup>zf643/zf643</sup> + MO2-irf1b + MO2-stat1a'
 where fish_zdb_id = 'ZDB-FISH-161007-9';

select * from genotype
 where geno_display_name is null;

update genotype
 set geno_display_name = 'mir107<sup>ya301/ya301</sup>'
where geno_zdb_id = 'ZDB-GENO-150904-3';

update genotype
 set geno_display_name = 'mir142a<sup>zf643/zf643</sup>'
where geno_zdb_id = 'ZDB-GENO-161006-28';

update genotype
 set geno_display_name = 'mir142a<sup>zf643/zf643</sup> ; nz117Tg ; mir142b<supzf660/zf660</sup>'
where geno_zdb_id = 'ZDB-GENO-161006-30';

update genotype
 set geno_display_name = 'mir142b<sup>zf660/zf660</sup> ; mir142a<sup>zf643/zf643</sup>'
where geno_zdb_id = 'ZDB-GENO-161006-29';


select count(*) from zdb_replaced_data
where zrepld_new_zdb_id = zrepld_old_zdb_id;

set constraints all immediate;

--rollback work;

commit work;

begin work;

set constraints all deferred;

create temp table tmp_linc (id varchar(50)) 
with no log;

load from lincrnagenes.txt
 insert into tmp_linc;

select id as gene_id
 from tmp_linc
into temp tmp_to_convert;

update marker_relationship_type
 set mreltype_mrkr_type_group_1 = 'GENEDOM'
 where mreltype_mrkr_type_group_1 = 'GENEDOM_PROD_PROTEIN'
and mreltype_name = 'gene produces transcript';


update zdb_replaced_data
 set zrepld_old_zdb_id = replace(zrepld_old_zdb_id, 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where zrepld_old_zdb_id = gene_id);

update zdb_replaced_data
 set zrepld_new_zdb_id = replace(zrepld_new_zdb_id, 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where zrepld_new_zdb_id = gene_id);

update withdrawn_data 
 set wd_old_zdb_id = replace(wd_old_Zdb_id, 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where wd_old_zdb_id = gene_id);

update withdrawn_data 
 set wd_new_zdb_id = replace(wd_new_Zdb_id, 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where wd_new_zdb_id = gene_id);

insert into zdb_replaced_data (zrepld_new_zdb_id, zrepld_old_zdb_id)
 select distinct replace(gene_id, 'GENE','LNCRNAG'), gene_id
  from tmp_to_convert;

insert into withdrawn_data (wd_new_zdb_id,wd_old_zdb_id)
 select distinct replace(gene_id, 'GENE','LNCRNAG'), gene_id
  from tmp_to_convert;

update zdb_active_data
 set zactvd_zdb_id = replace(zactvd_zdb_id, 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where zactvd_zdb_id = gene_id);

update marker
 set mrkr_type = 'LNCRNAG'
 where mrkr_type = 'GENE'
 and exists (Select 'x' from tmp_to_convert1 where mrkr_zdb_id = gene_id);

update marker
 set mrkr_zdb_id = replace(mrkr_Zdb_id, 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where mrkr_zdb_id = gene_id);

update marker
 set mrkr_type = 'LNCRNAG'
 where mrkr_type = 'GENE'
 and exists (Select 'x' from tmp_to_convert where mrkr_zdb_id = gene_id);

update zmap_pub_pan_mark
 set zdb_id = replace(zdb_id, 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where  zdb_id = gene_id);

update external_note
 set extnote_data_zdb_id = replace(extnote_data_zdb_id, 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where extnote_data_zdb_id = gene_id);


update paneled_markers
 set  zdb_id = replace(zdb_id, 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where zdb_id = gene_id);

update all_map_names
 set allmapnm_zdb_id = replace(allmapnm_zdb_id, 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where allmapnm_zdb_id = gene_id);

update sequence_feature_chromosome_location
 set sfcl_feature_Zdb_id = replace( sfcl_feature_Zdb_id, 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where  sfcl_feature_Zdb_id = gene_id);


update record_attribution
 set recattrib_data_zdb_id = replace(recattrib_data_zdb_id, 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where recattrib_data_zdb_id = gene_id);

update expression_experiment2
 set xpatex_gene_zdb_id = replace(xpatex_gene_zdb_id, 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where xpatex_gene_zdb_id = gene_id);

update feature_marker_relationship
 set fmrel_mrkr_zdb_id = replace(fmrel_mrkr_zdb_id , 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where fmrel_mrkr_zdb_id = gene_id);

update marker_Relationship
 set mrel_mrkr_1_zdb_id = replace(mrel_mrkr_1_zdb_id, 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where mrel_mrkr_1_zdb_id = gene_id);

select distinct mrel_type from marker_relationship
 where mrel_mrkr_1_zdb_id in (select gene_id from tmp_to_convert where mrel_mrkr_1_zdb_id = gene_id);

select count(*) from marker_relationship
 where mrel_mrkr_1_zdb_id in (select gene_id from tmp_to_convert where mrel_mrkr_1_zdb_id = gene_id);


update construct_component
 set cc_component_zdb_id  = replace(cc_component_zdb_id, 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where cc_component_zdb_id = gene_id);

--CONSTRUCT_COMPONENTS
--GENEDOM


update marker_Relationship
 set mrel_mrkr_2_zdb_id = replace(mrel_mrkr_2_zdb_id, 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where mrel_mrkr_2_zdb_id = gene_id);

update data_alias
 set dalias_data_zdb_id = replace(dalias_data_zdb_id, 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where dalias_data_zdb_id = gene_id);

update marker_history
 set mhist_mrkr_zdb_id = replace(mhist_mrkr_zdb_id,  'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where mhist_mrkr_zdb_id = gene_id);

update marker_history_audit
 set mha_mrkr_zdb_id = replace(mha_mrkr_zdb_id, 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where mha_mrkr_zdb_id = gene_id);

update zdb_orphan_data
 set zorphand_zdb_id = replace(zorphand_zdb_id, 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where zorphand_zdb_id  = gene_id);

update external_reference
 set exref_data_zdb_id = replace(exref_data_zdb_id, 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where exref_data_zdb_id = gene_id);

update int_data_source
 set ids_data_zdb_id = replace(ids_data_zdb_id, 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where ids_data_zdb_id = gene_id);

update linkage_membership_Search
 set lms_member_1_zdb_id = replace(lms_member_1_zdb_id, 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where lms_member_1_zdb_id  = gene_id);

update linkage_membership_Search
 set lms_member_2_zdb_id = replace(lms_member_2_zdb_id, 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where lms_member_2_zdb_id  = gene_id);

update mapped_marker
 set marker_id  = replace(marker_id, 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where marker_id = gene_id);



update db_link
 set dblink_linked_recid = replace(dblink_linked_recid, 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where dblink_linked_recid = gene_id);

update data_note
 set dnote_data_zdb_id = replace(dnote_data_zdb_id, 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where dnote_data_zdb_id = gene_id);

update unique_location 
  set ul_data_zdb_id = replace(ul_data_zdb_id, 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where ul_data_zdb_id = gene_id);

update ortholog
  set ortho_zebrafish_gene_zdb_id = replace(ortho_zebrafish_gene_zdb_id, 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where ortho_zebrafish_gene_zdb_id = gene_id);

update snp_download
  set snpd_mrkr_zdb_id = replace(snpd_mrkr_zdb_id, 'GENE','LNCRNAG')
 where exists (select 'x' from tmp_to_convert where snpd_mrkr_zdb_id = gene_id);

update marker_go_term_evidence
  set mrkrgoev_mrkr_zdb_id = replace(mrkrgoev_mrkr_zdb_id, 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where mrkrgoev_mrkr_zdb_id = gene_id);

update linkage_member
 set lnkgmem_member_zdb_id = replace(lnkgmem_member_zdb_id, 'GENE','LNCRNAG')
where exists (Select 'x' from tmp_to_convert where lnkgmem_member_zdb_id = gene_id);

update linkage_membership
 set lnkgm_member_1_zdb_id = replace(lnkgm_member_1_zdb_id, 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where lnkgm_member_1_zdb_id = gene_id);

update linkage_membership
 set lnkgm_member_2_zdb_id = replace(lnkgm_member_2_zdb_id, 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where lnkgm_member_2_zdb_id = gene_id);

update linkage_pair_member
  set lpmem_member_zdb_id = replace(lpmem_member_zdb_id, 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where lpmem_member_zdb_id = gene_id);

update linkage_single
 set lsingle_member_zdb_id = replace(lsingle_member_zdb_id, 'GENE','LNCRNAG')
 where exists (Select 'x' from tmp_to_convert where lsingle_member_zdb_id = gene_id);

update updates
  set rec_id = replace(rec_id, 'GENE','LNCRNAG')
 where exists (select 'x' from tmp_to_Convert where rec_id = gene_id);

update primer_set
  set marker_id = replace(marker_id, 'GENE','LNCRNAG' )
where exists (Select 'x' from tmp_to_Convert where  marker_id = gene_id);

update genedom_family_member
  set gfammem_mrkr_zdb_id = replace(gfammem_mrkr_zdb_id , 'GENE','LNCRNAG')
where exists (Select 'x' from tmp_to_Convert where gfammem_mrkr_zdb_id  = gene_id);

update clone
  set clone_mrkr_zdb_id = replace(clone_mrkr_zdb_id , 'GENE','LNCRNAG')
where exists (Select 'x' from tmp_to_Convert where clone_mrkr_zdb_id  = gene_id);

update construct_marker_relationship
  set conmrkrrel_mrkr_zdb_id = replace( conmrkrrel_mrkr_zdb_id, 'GENE','LNCRNAG')
where exists (Select 'x' from tmp_to_Convert where conmrkrrel_mrkr_zdb_id  = gene_id);

update marker_sequence
  set seq_mrkr_Zdb_id = replace(seq_mrkr_Zdb_id , 'GENE','LNCRNAG')
where exists (Select 'x' from tmp_to_Convert where seq_mrkr_Zdb_id  = gene_id);

update clean_expression_fast_search
  set cefs_mrkr_zdb_id = replace(cefs_mrkr_zdb_id , 'GENE','LNCRNAG')
where exists (Select 'x' from tmp_to_Convert where cefs_mrkr_zdb_id  = gene_id);

update mutant_fast_search
  set mfs_mrkr_zdb_id = replace(mfs_mrkr_zdb_id , 'GENE','LNCRNAG')
where exists (Select 'x' from tmp_to_Convert where mfs_mrkr_zdb_id  = gene_id);


update genotype
 set geno_handle = 'nz117Tg[U,U,U] zf643[2,1,1] zf660[2,1,1]TU'
 where geno_zdb_id = 'ZDB-GENO-161006-30';

update genotype
 set geno_handle = 'ct815[2,1,1]'
 where geno_zdb_id = 'ZDB-GENO-131125-22';

update genotype
 set geno_handle = 'zf643[2,1,1] zf660[2,1,1]TU'
 where geno_zdb_id = 'ZDB-GENO-161006-29';

update genotype
 set geno_handle = 'tpl12Gt[U,U,U] tpl2Tg[U,U,U]'
 where geno_zdb_id = 'ZDB-GENO-131120-13';

update genotype
 set geno_handle = 'tpl12Gt[1,U,U] tpl2Tg[1,U,U]AB'
 where geno_zdb_id = 'ZDB-GENO-140109-11';

update genotype
 set geno_display_name = 'mir142a<sup>zf643/zf643</sup> ; nz117Tg ; mir142b<sup>zf660/zf660</sup>'
 where geno_zdb_id = 'ZDB-GENO-161006-30';

update genotype
 set geno_display_name = 'qrfp<sup>ct815/ct815</sup>'
 where geno_zdb_id = 'ZDB-GENO-131125-22';

update genotype
 set geno_display_name = 'mir142b<sup>zf660/zf660</sup> ; mir142a<sup>zf643/zf643</sup>'
 where geno_zdb_id = 'ZDB-GENO-161006-29';

update genotype
 set geno_display_name = 'si:dkey-24m12.2<sup>tpl12Gt</sup>; tpl2Tg'
 where geno_zdb_id = 'ZDB-GENO-131120-13';

update genotype
 set geno_display_name = 'si:dkey-24m12.2<sup>tpl12Gt/+</sup>; tpl2Tg/+'
 where geno_zdb_id = 'ZDB-GENO-140109-11';


select geno_zdb_id
 from genotype
 where geno_handle is null;

select fish_zdb_id, fish_name from fish
 where fish_name_order is null;



select * from genotype
 where geno_display_name is null;


select wd_new_zdb_id, wd_old_zdb_id, count(*) from withdrawn_data
group by wd_new_zdb_id, wd_old_zdb_id
having count(*) >1 ;


set constraints all immediate;

--rollback work;



commit work;

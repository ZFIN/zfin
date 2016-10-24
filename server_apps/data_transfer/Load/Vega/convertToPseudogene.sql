begin work;


select mrel_mrkr_2_zdb_id as tscript, mrel_mrkr_1_zdb_id as gene from marker, 
       transcript, marker_relationship
 where mrkr_zdb_id = tscript_mrkr_zdb_id
 and mrel_mrkr_2_zdb_id = mrkr_zdb_id
and tscript_type_id = 4
 and mrel_type = 'gene produces transcript'
and mrel_mrkr_1_zdb_id like 'ZDB-GENE-%'
and not exists (Select 'x' from foreign_db_data_type, db_link, foreign_db_contains
    	       	       where dblink_linked_recid = mrel_mrkr_1_zdb_id
		       and fdbdt_pk_id = fdbcont_fdbdt_id
		       and fdbcont_zdb_id = dblink_fdbcont_zdb_id
		       and fdbdt_data_type = 'Polypeptide')
into temp tmp_to_change;

select count(*), gene
 from record_Attribution, tmp_to_change
 where gene = recattrib_data_zdb_id
 group by gene 
order by count(*) desc;

set constraints all deferred;

update marker
 set mrkr_type = 'GENEP'
 where mrkr_type = 'GENE'
and exists (Select 'x' from tmp_to_change
    	   	   where mrkr_zdb_id = gene);

update marker
  set mrkr_zdb_id = replace(mrkr_zdb_id, 'GENE', 'GENEP')
 where exists (Select 'x' from tmp_to_change
    	   	   where mrkr_zdb_id = gene);

update marker 
  set mrkr_abbrev = mrkr_abbrev||'p'
 where exists (Select 'x' from tmp_to_change
    	   	   where mrkr_zdb_id = gene);

update marker
 set mrkr_name = mrkr_name||' pseudogene'
  where exists (Select 'x' from tmp_to_change
    	   	   where mrkr_zdb_id = gene);

update db_link
  set dblink_linked_recid = replace(dblink_linked_recid, 'GENE','GENEP')
  where exists (Select 'x' from tmp_to_change
    	   	   where dblink_linked_recid = gene);


update record_attribution
  set recattrib_data_zdb_id = replace(recattrib_data_zdb_id, 'GENE','GENEP')
  where exists (Select 'x' from tmp_to_change
    	   	   where recattrib_data_zdb_id = gene);


update marker_relationship
  set mrel_mrkr_1_zdb_id = replace(mrel_mrkr_1_zdb_id, 'GENE','GENEP')
 where exists (Select 'x' from tmp_to_change
    	   	   where mrel_mrkr_1_zdb_id = gene);
update marker_relationship
  set mrel_mrkr_2_zdb_id = replace(mrel_mrkr_2_zdb_id,'GENE','GENEP')
 where exists (Select 'x' from tmp_to_change
			where mrel_mrkr_2_zdb_id = gene);

update zdb_active_data
  set zactvd_zdb_id = replace(zactvd_zdb_id, 'GENE','GENEP')
 where exists (Select 'x' from tmp_to_change
    	   	   where zactvd_zdb_id = gene);

update feature_marker_relationship
 set fmrel_mrkr_zdb_id = replace(fmrel_mrkr_zdb_id, 'GENE','GENEP')
 where exists (Select 'x' from tmp_to_change
    	   	   where fmrel_mrkr_zdb_id = gene);

update data_alias
 set dalias_data_zdb_id = replace(dalias_data_zdb_id, 'GENE','GENEP')
 where exists (select 'x' from tmp_to_change
       	      	      where dalias_data_zdb_id = gene);

update updates
  set rec_id = replace (rec_id, 'GENE','GENEP')
 where exists (select 'x' from tmp_to_change
       	      	      where rec_id = gene);

update marker_history
  set mhist_mrkr_zdb_id = replace(mhist_mrkr_zdb_id, 'GENE','GENEP')
 where exists (Select 'x' from tmp_to_change
       	      	      	  where mhist_mrkr_zdb_id = gene);


update ortholog
  set ortho_zebrafish_gene_zdb_id = replace (ortho_zebrafish_gene_zdb_id, 'GENE','GENEP')
 where exists (Select 'x' from tmp_to_change
       	      	      	  where ortho_zebrafish_gene_zdb_id = gene);

update expression_Experiment2
  set xpatex_gene_zdb_id = replace(xpatex_gene_zdb_id, 'GENE','GENEP')
 where exists (Select 'x' from tmp_to_change
       	      	      	  where xpatex_gene_zdb_id = gene);

update construct_marker_relationship
 set conmrkrrel_mrkr_zdb_id = replace(conmrkrrel_mrkr_zdb_id, 'GENE','GENEP')
 where exists (Select 'x' from tmp_to_change
       	      	      	  where conmrkrrel_mrkr_zdb_id = gene);

update marker_go_term_evidence
  set mrkrgoev_mrkr_zdb_id = replace(mrkrgoev_mrkr_Zdb_id, 'GENE','GENEP')
 where exists (Select 'x' from tmp_to_change
       	      	      where mrkrgoev_mrkr_zdb_id = gene);

update clean_expression_fast_search
  set cefs_mrkr_zdb_id = replace(cefs_mrkr_zdb_id,  'GENE','GENEP')
 where exists (Select 'x' from tmp_to_change
       	      	      where cefs_mrkr_zdb_id = gene);


update clone
 set clone_mrkr_zdb_id = replace(clone_mrkr_zdb_id,  'GENE','GENEP')
 where exists (Select 'x' from tmp_to_change
       	      	      where clone_mrkr_zdb_id = gene);


update fish_str
 set fishstr_str_zdb_id = replace(fishstr_str_zdb_id,  'GENE','GENEP')
 where exists (Select 'x' from tmp_to_change
       	      	      where fishstr_str_zdb_id = gene);

update genedom_family_member
  set gfammem_mrkr_zdb_id = replace(gfammem_mrkr_zdb_id, 'GENE','GENEP')
 where exists (Select 'x' from tmp_to_change
       	      	      where gfammem_mrkr_zdb_id = gene);

update fish_Search
 set gene_id = replace(gene_id, 'GENE','GENEP')
 where exists (select 'x' from tmp_to_change
			where gene_id = gene);

update marker_sequence
  set seq_mrkr_zdb_id = replace(seq_mrkr_zdb_id, 'GENE','GENEP')
 where exists (Select 'x' from tmp_to_change
       	      	      where seq_mrkr_zdb_id = gene);

update mutant_fast_search
 set mfs_mrkr_zdb_id = replace(mfs_mrkr_zdb_id, 'GENE','GENEP')
 where exists (select 'x' from tmp_to_change
       	      	      where mfs_mrkr_Zdb_id = gene);

update primer_set
  set marker_id = replace(marker_id, 'GENE','GENEP')
 where exists (select 'x' from tmp_to_change
       	      	      where marker_id = gene);

update all_map_names 
  set allmapnm_zdb_id = replace(allmapnm_zdb_id, 'GENE','GENEP')
 where exists (select 'x' from tmp_to_change
       	      	      where allmapnm_zdb_id = gene);

update data_note
 set dnote_data_zdb_id = replace (dnote_data_zdb_id, 'GENE','GENEP')
 where exists (select 'x' from tmp_to_change
       	      	      where dnote_data_zdb_id = gene);

update external_note
 set extnote_data_zdb_id = replace (extnote_data_zdb_id, 'GENE','GENEP')
 where exists (select 'x' from tmp_to_change
       	      	      where extnote_data_zdb_id = gene);

update linkage_membership
  set lnkgm_member_1_zdb_id = replace (lnkgm_member_1_zdb_id, 'GENE','GENEP')
 where exists (select 'x' from tmp_to_change
       	      	      where lnkgm_member_1_zdb_id = gene);

update linkage_membership
  set lnkgm_member_2_zdb_id = replace (lnkgm_member_2_zdb_id, 'GENE','GENEP')
 where exists (select 'x' from tmp_to_change       	      	      where lnkgm_member_2_zdb_id = gene);

update zdb_replaced_data
  set zrepld_old_zdb_id = replace (zrepld_old_zdb_id, 'GENE','GENEP')
 where exists (select 'x' from tmp_to_change
       	      	      where zrepld_old_zdb_id  = gene);

update zdb_replaced_data
  set zrepld_new_zdb_id = replace (zrepld_new_zdb_id, 'GENE','GENEP')
 where exists (select 'x' from tmp_to_change
       	      	      where zrepld_new_zdb_id  = gene);

update marker_history_audit
  set mha_mrkr_zdb_id = replace(mha_mrkr_zdb_id, 'GENE','GENEP')
 where exists (select 'x' from tmp_to_change
       	      	      where mha_mrkr_zdb_id  = gene);

update snp_download
  set snpd_mrkr_zdb_id = replace(snpd_mrkr_zdb_id, 'GENE','GENEP')
 where exists (select 'x' from tmp_to_change
       	      	      	  where snpd_mrkr_zdb_id = gene);

update paneled_markers
  set zdb_id = replace(zdb_id, 'GENE','GENEP')
 where exists (select 'x' from tmp_to_change
       	      	      	  where zdb_id = gene);

update linkage_single
  set lsingle_member_zdb_id = replace(lsingle_member_zdb_id, 'GENE','GENEP')
 where exists (select 'x' from tmp_to_change
       	      	      where lsingle_member_zdb_id = gene);

update mapped_marker
 set marker_id = replace(marker_id, 'GENE','GENEP')
 where exists (Select 'x' from tmp_to_change
       	      	      where marker_id = gene);

update zdb_orphan_data
 set zorphand_zdb_id = replace(zorphand_zdb_id, 'GENE','GENEP')
 where exists (Select 'x' from tmp_to_change
       	      	      where zorphand_zdb_id = gene);

update zmap_pub_pan_mark
  set zdb_id = replace(zdb_id, 'GENE','GENEP')
 where exists (Select 'x' from tmp_to_change
       	      	      where zdb_id = gene);

update int_data_source
 set ids_data_zdb_id = replace(ids_data_zdb_id, 'GENE','GENEP')
 where exists (Select 'x' from tmp_to_change
       	      	      where ids_data_zdb_id = gene);

update external_reference
 set exref_data_zdb_id = replace(exref_data_zdb_id, 'GENE','GENEP')
 where exists (select 'x' from tmp_to_change
       	      	      where exref_data_Zdb_id = gene);

update int_data_supplier
 set idsup_data_zdb_id = replace(idsup_data_zdb_id, 'GENE','GENEP')
 where exists (Select 'x' from tmp_to_change
       	      	      where idsup_data_zdb_id = gene);

update sequence_feature_chromosome_location_generated
 set sfclg_data_zdb_id = replace(sfclg_data_zdb_id, 'GENE','GENEP')
 where exists (Select 'x' from tmp_to_change
       	      	      where sfclg_data_zdb_id = gene);

set constraints all immediate;


commit work;

--rollback work;

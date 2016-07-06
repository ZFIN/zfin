unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/fish/1fish.txt"
select fish_zdb_id, fish_name, fish_handle, fish_is_wildtype, fish_order, fish_functional_affected_gene_count, fish_genotype_zdb_id, ""
from fish
 where not exists (Select 'x' from fish_str
       	   	  	  where fish_Zdb_id = fishstr_fish_zdb_id)
union
select fish_zdb_id, fish_name, fish_handle, fish_is_wildtype, fish_order, fish_functional_affected_gene_count, fish_genotype_zdb_id, fishstr_str_zdb_id
from fish, fish_str
   where fish_Zdb_id = fishstr_fish_zdb_id;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/dataDate/dateUnloaded.txt"
select di_pk_id, di_date_unloaded
  from database_info;


unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/mutagenMutagee/mutagenMutagee.txt"
select featassay_feature_zdb_id, featassay_mutagen, featassay_mutagee, feature_type
  from feature_assay,
 outer (feature)
 where feature_zdb_id = featassay_feature_zdb_id;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/cleanPhenotype/cleanPhenotype.txt"
select mfs_mrkr_zdb_id, genox_fish_zdb_id, genox_exp_zdb_id
 from mutant_fast_search, fish_experiment
  where mfs_mrkr_zdb_id like 'ZDB-GENE%'
 and mfs_genox_zdb_id = genox_zdb_id
;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/images/images.txt"
select img_zdb_id, img_fig_zdb_id, img_label
  from image;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/ontologySubset/1ontologySubset.txt"
select osubset_pk_id, osubset_subset_name, osubset_subset_definition,
       osubset_subset_type, osubset_ont_id, term_ont_id, term_ontology
 from ontology_subset, term_subset, term
  where osubset_pk_id = termsub_subset_id
  and termsub_term_zdb_id = term_zdb_id
  and term_is_obsolete = 'f'
  and term_is_secondary ='f';

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/dataSourceSupplier/1dataSourceSupplier.txt"
select idsup_data_zdb_id, idsup_supplier_zdb_id, idsup_acc_num, idsup_avail_state, "supplier", get_obj_type(idsup_data_zdb_id), feature_type
 from int_data_supplier, feature
where idsup_data_zdb_id like 'ZDB-ALT%'
and feature_zdb_id = idsup_data_zdb_id
union
select idsup_data_zdb_id, idsup_supplier_zdb_id, idsup_acc_num, idsup_avail_state, "supplier", get_obj_type(idsup_data_zdb_id), ""
 from int_data_supplier
where idsup_data_zdb_id not like 'ZDB-ALT%'
union all
select ids_data_zdb_id, ids_source_zdb_id, "","","source", get_obj_type(ids_data_zdb_id), feature_type
from int_data_source, feature
where ids_data_zdb_id like 'ZDB-ALT%'
and feature_zdb_id = ids_data_zdb_id
union 
select ids_data_zdb_id, ids_source_zdb_id, "","","source", get_obj_type(ids_data_zdb_id), ""
from int_data_source
where ids_data_zdb_id not like 'ZDB-ALT%'
;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_features/labOfOrigin.txt"
select ids_data_zdb_id, ids_source_zdb_id, feature_type
from int_data_source, feature
where ids_data_zdb_id like 'ZDB-ALT%'
and feature_zdb_id = ids_data_zdb_id
;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/markerSequences/1sequences.txt"
 select mrkr_zdb_id, seq_sequence, seq_offset_start, seq_offset_stop, "", get_obj_type(mrkr_zdb_id),mrkr_zdb_id, seq_sequence_2 from marker, marker_Sequence
where mrkr_Zdb_id = seq_mrkr_zdb_id
union
 select mrkr_zdb_id, seq_sequence, seq_offset_start, seq_offset_stop, seq_variation, get_obj_type(mrkr_zdb_id),mrkr_zdb_id, seq_sequence_2 from marker, snp_Sequence
where mrkr_Zdb_id = seq_mrkr_zdb_id
;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/omimPhenotype/1omimphenotype.txt"
 select ortho_zebrafish_gene_zdb_id,omimp_name,omimp_omim_id from omim_phenotype, ortholog
 where omimp_ortho_zdb_id = ortho_Zdb_id
 and ortho_zebrafish_gene_zdb_id not like 'ZDB-GENEP%';

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/featureCrossReferences/1featureCrossReferences.txt"
 select feature_zdb_id, feature_type, dblink_acc_num, fdb_db_name, fdb_db_query from feature, db_link, foreign_db, foreign_db_contains
 where feature_zdb_id = dblink_linked_recid
 and fdb_db_pk_id = fdbcont_Fdb_db_id
and fdbcont_zdb_id = dblink_fdbcont_zdb_id;


unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/chromosome/1chromosome.txt"
select distinct sfclg_chromosome, sfclg_data_zdb_id from sequence_feature_chromosome_location_generated
 where sfclg_data_zdb_id like 'ZDB-GENE-%';

--all identifiers;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/identifiers/1identifiers.txt"
select mrkr_zdb_id, mrkr_name
 from marker
 union
 select mrkr_Zdb_id, mrkr_abbrev
 from marker
union 
 select mrkr_zdb_id, dalias_alias
  from data_alias, marker
 where mrkr_Zdb_id = dalias_data_zdb_id
union
select mrkr_zdb_id, dblink_acc_num
  from marker, db_link
where mrkr_Zdb_id = dblink_linked_recid;


--featurePubs

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/featurePubs/1featurePub.txt"
select record_attribution.*, feature_type from record_attribution, feature
 where recattrib_data_zdb_id = feature_zdb_id
;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/genotypePubs/1genoPubs.txt"
select recattrib_source_zdb_id, recattrib_Data_zdb_id from record_Attribution,
  genotype 
 where recattrib_Data_zdb_id = geno_zdb_id;


unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/lab/1lab.txt"
  select zdb_id, name, contact_person, url, email,fax,phone
    from lab;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/people/1person.txt"
  select zdb_id as person_id, first_name, last_name, full_name, email
    from person
   order by zdb_id;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/company/1company.txt"
  select zdb_id, name, contact_person, url,email,fax,phone
    from company;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/people/1person_associations.txt"
  select source_id, target_id
    from int_person_lab
union
select source_id, target_id from int_person_company
union
select source_id, target_id from int_person_pub;


--unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_expression/1xpat.txt"
-- select * from expression_experiment
--   where exists (select 'x' from expression_result where xpatres_xpatex_zdb_id =xpatex_zdb_id) ;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_expression/2xpatres.txt"
 select res.xpatres_pk_id, res.xpatres_expression_found, anat.term_ont_id, a.stg_obo_id, b.stg_obo_id,xpatex.xpatex_source_zdb_id,xpatex.xpatex_assay_name,xpatex.xpatex_probe_feature_zdb_id, xpatex.xpatex_gene_zdb_id, xpatex.xpatex_dblink_zdb_id, xpatex.xpatex_genox_zdb_id, xpatex.xpatex_atb_zdb_id, xpatfig.efs_fig_Zdb_id, termt.term_ont_id, fish_zdb_id, genox_exp_zdb_id
  from expression_experiment2 xpatex, expression_figure_stage xpatfig, expression_result2 res, stage a, stage b, term anat, outer term termt,fish_experiment,fish
  where res.xpatres_superterm_zdb_id = anat.term_zdb_id
  and res.xpatres_subterm_zdb_id = termt.term_zdb_id
  and xpatfig.efs_start_stg_zdb_id = a.stg_zdb_id
  and xpatfig.efs_end_stg_zdb_id = b.stg_zdb_id
  and res.xpatres_efs_id = xpatfig.efs_pk_id
  and xpatfig.efs_xpatex_zdb_id = xpatex.xpatex_zdb_id
  and genox_zdb_id = xpatex_genox_zdb_id
  and fish_zdb_id = genox_fish_zdb_id;

--unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_expression/3xpatfig.txt"
-- select * from expression_pattern_figure;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_figures/1figs.txt"
 select fig_zdb_id,fig_label,replace(fig_caption,'
',''),fig_source_zdb_id from figure;

--phenotype

create temp table tmp_pato (id int8,
       genox_id varchar(50),
       superterm varchar(50),
       subterm varchar(50),
       superterm2 varchar(50),
       subterm2 varchar(50),
       startstg varchar(50),
       endstg varchar(50),
       fig varchar(50),
       tag varchar(20),
       quality varchar(50),
       geno_id varchar(50),
       exp_id varchar(50),
       clean boolean,
       mrkr_id varchar(50),
       short_name varchar(255))
with no log;

insert into tmp_pato (id, genox_id, superterm, subterm, superterm2, subterm2, quality, startstg, endstg, fig, tag, geno_id, exp_id, mrkr_id, short_name)
  select psg_id,
  	 pg_genox_zdb_id,
	 a.term_ont_id,
	 b.term_ont_id,
	 c.term_ont_id,
	 d.term_ont_id,
	 e.term_ont_id,
	 f.stg_obo_id,
	 g.stg_obo_id,
	 pg_fig_zdb_id, 
          psg_tag,
	  fish_zdb_id,
	  genox_exp_zdb_id,
	  psg_mrkr_Zdb_id,
	  psg_short_name
   from phenotype_source_generated, phenotype_observation_generated, stage f, stage g,term a, outer term b, outer term c, outer term d, term e, fish_experiment, fish
   where pg_start_Stg_zdb_id = f.stg_zdb_id
   and pg_end_stg_zdb_id = g.stg_zdb_id
   and psg_e1a_Zdb_id = a.term_Zdb_id
   and psg_e1b_zdb_id = b.term_zdb_id
   and psg_e2a_zdb_id = c.term_Zdb_id
   and psg_e2b_zdb_id = d.term_zdb_id
   and psg_quality_Zdb_id = e.term_zdb_id
   and pg_id = psg_pg_id
   and genox_zdb_id = pg_genox_zdb_id
   and genox_fish_zdb_id = fish_zdb_id
;

update tmp_pato
 set clean = 't'
 where exists (Select 'x' from mutant_fast_search
       	      	      where mfs_genox_zdb_id = genox_id);

update tmp_pato
 set clean = 'f'
 where clean is null;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_phenotypes/1apato.txt"
  select * from tmp_pato;

--unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_phenotypes/2apatofig.txt"
--  select * from apato_figure;

--genotypesFeatures


unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_genotypes/1genos.txt"
  select distinct geno.*,(select a.zyg_name||","||b.zyg_name from zygocity a, zygocity b where a.zyg_zdb_id = genofeat_dad_zygocity and b.zyg_zdb_id = genofeat_mom_zygocity ),get_genotype_backgrounds(geno_zdb_id)
     from genotype geno, genotype_feature
     where geno.geno_zdb_id = genofeat_geno_zdb_id 
     and geno.geno_is_wildtype = 'f'
     and genofeat_mom_Zygocity is not null
     and genofeat_dad_zygocity is not null
union
  select distinct geno.*,'',get_genotype_backgrounds(geno_zdb_id)
     from genotype geno, genotype_feature
     where geno.geno_zdb_id = genofeat_geno_zdb_id 
     and geno.geno_is_wildtype = 'f'
     and genofeat_mom_Zygocity is  null
     and genofeat_dad_zygocity is  null
  union 
    select geno.*,'',get_genotype_backgrounds(geno_zdb_id)
      from genotype geno
      where geno_is_wildtype = 't'
      ;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_features/1features.txt"
  select feature_zdb_id, feature_name, feature_abbrev, feature_type, feature_lab_prefix_id, featassay_mutagen, featassay_mutagee from feature, feature_Assay
  where not exists (Select 'x' from genotype_Feature
  	 		where genofeat_feature_zdb_id = feature_zdb_id)
 and featassay_feature_zdb_id = feature_zdb_id
 ;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_fmrels/1fmrels.txt"
  select feature_marker_relationship.*,feature_type from feature_marker_relationship, feature, feature_assay
   where fmrel_ftr_zdb_id = feature_zdb_id
   and featassay_feature_zdb_id = feature_zdb_id
;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_genofeats/1genofeats.txt"
 select genofeat_zdb_id, genofeat_geno_Zdb_id, genofeat_feature_zdb_id,
 	(select zyg_name from zygocity where zyg_zdb_id = genofeat_zygocity), feature_type,feature_name, feature_abbrev, ids_source_zdb_id, featassay_mutagen, featassay_mutagee,feature_lab_prefix_id
  from genotype_feature, feature, int_data_source, feature_assay
  where genofeat_feature_zdb_id = feature_zdb_id
  and ids_datA_zdb_id = feature_zdb_id
and featassay_feature_zdb_id = feature_zdb_id
 union 
select genofeat_zdb_id, genofeat_geno_Zdb_id, genofeat_feature_zdb_id,
 	(select zyg_name from zygocity where zyg_zdb_id = genofeat_zygocity), feature_type,feature_name, feature_abbrev, "", featassay_mutagen, featassay_mutagee,feature_lab_prefix_id
  from genotype_feature, feature,feature_assay
  where genofeat_feature_zdb_id = feature_zdb_id
  and featassay_feature_zdb_id = feature_zdb_id
 ;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_genoenvs/1genoenvs.txt"
 select genox_zdb_id, fish_zdb_id, genox_exp_zdb_id
   from fish_experiment, fish
   where fish_zdb_id = genox_fish_zdb_id
;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_genoenvs/2envs.txt"
  select exp_zdb_id, exp_source_zdb_id 
    from experiment, experiment_condition
   where expcond_exp_Zdb_id = exp_zdb_id
   union
   select exp_zdb_id, exp_source_zdb_id
   from experiment
   where not exists (select 'x' from experiment_condition 
   	     	    	    where expcond_exp_zdb_id = exp_zdb_id);

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_experiments/1exps.txt"
  select expcond_zdb_id,
         expcond_exp_zdb_id,
         zeco.term_name,
         expcond_zeco_term_zdb_id,
         chebi.term_name,
         expcond_chebi_term_zdb_id, 
         zfa.term_name,
         expcond_ao_term_zdb_id,
         gocc.term_name,
         expcond_go_cc_term_zdb_id,
         taxon.term_name,
         expcond_taxon_term_zdb_id,
         exp_name  
    from experiment_condition
join experiment on exp_zdb_id = expcond_exp_zdb_id
left outer join term zeco on zeco.term_zdb_id = expcond_zeco_term_zdb_id  
left outer join term chebi on chebi.term_zdb_id = expcond_chebi_term_zdb_id
left outer join term zfa on zfa.term_zdb_id = expcond_ao_term_zdb_id
left outer join term gocc on gocc.term_zdb_id = expcond_go_cc_term_zdb_id
left outer join term taxon on taxon.term_zdb_id = expcond_taxon_term_zdb_id
union
  select exp_zdb_id,
         exp_zdb_id,
         '',
         '',
         '',
         '',
         '',
         '',
         '',
         '',
         '',
         '',
         ''
    from experiment 
  where not exists (select 'x' from experiment_condition
                           where expcond_exp_zdb_id = exp_zdb_id)
;

--markers

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_markers/1markers.txt"
select mrkr_zdb_id, mrkr_abbrev, mrkr_type, mrkr_name from marker;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_markers/2mrels.txt"
 select mrel.* from marker_relationship mrel, marker a, marker b
  where a.mrkr_Zdb_id = mrel_mrkr_1_zdb_id
  and b.mrkr_Zdb_id = mrel_mrkr_2_zdb_id 
  and mrel_type != 'clone overlap'

  ;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_markers/3dalias.txt"
  select data_alias.*,alias_group.aliasgrp_name from data_alias, alias_group
    where exists (select 'x' from marker where mrkr_zdb_id = dalias_data_zdb_id)
    and aliasgrp_pk_id = dalias_group_id;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/feature_alias/1dalias.txt"
  select data_alias.*,alias_group.aliasgrp_name,feature_type from data_alias, alias_group, feature
    where feature_zdb_id = dalias_data_zdb_id
    and aliasgrp_pk_id = dalias_group_id;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/genotype_alias/1dalias.txt"
  select data_alias.*,alias_group.aliasgrp_name from data_alias, alias_group
    where exists (select 'x' from genotype where geno_zdb_id = dalias_data_zdb_id)
    and aliasgrp_pk_id = dalias_group_id;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_markers/4replaceddata.txt"
  select zdb_replaced_data.* from zdb_replaced_data, marker
   where zrepld_new_zdb_id = mrkr_zdb_id;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_markers/5dblinks.txt"
select dblink_zdb_id, fdb_db_name, dblink_acc_num, dblink_linked_recid, fdbdt_data_type, fdb_db_query, fdb_db_name
  from db_link, marker, foreign_db_contains, foreign_db, foreign_Db_data_type
  where dblink_linked_recid = mrkr_zdb_id
  and fdbcont_zdb_id = dblink_fdbcont_Zdb_id
  and fdbcont_fdbdt_id = fdbdt_pk_id
  and fdbcont_fdb_db_id = fdb_db_pk_id
  and fdb_db_name not in ('miRBASE Mature','miRBASE Stem Loop','unreleaseRNA','ZFIN','ZFIN_PROT','Curated miRNA Mature','Curated miRNA Stem Loop')
union all
 select snpd_mrkr_zdb_id||snpd_rs_acc_num, "dbSNP",snpd_rs_acc_num, snpd_mrkr_zdb_id, "genomic", "", ""
   from snp_download;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_kegg/1keggMapping.txt"
  select dblink_acc_num, dblink_zdb_id, dblink_linked_recid 
    from db_link, foreign_db_contains, foreign_db
    where dblink_fdbcont_zdb_id = fdbcont_zdb_id
    and fdb_db_pk_id = fdbcont_fdb_db_id
    and fdbcont_organism_common_name ='Zebrafish'
    and fdb_db_name = 'Entrez Gene';

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/mutantAttributions/1recattrib.txt"
 select * from record_attribution
   where recattrib_data_zdb_id like 'ZDB-ALT%'
 or recattrib_data_zdb_id like 'ZDB-GENO%';

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_markers/6recattrib.txt"
select recattrib_data_zdb_id, recattrib_source_zdb_id
  from record_Attribution, marker
  where mrkr_zdb_id = recattrib_data_zdb_id;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_markers/7antibody.txt"
select * from antibody;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_markers/8transcript.txt"
select tscript_mrkr_Zdb_id, 
       tscriptt_type, 
       replace(tscriptt_definition,'
',''), 
       replace(ttsdef_definition,'
',''), 
       tscripts_status
  from transcript,
       outer transcript_status,
       outer transcript_type,
       outer tscript_type_status_definition
 where tscript_type_id = tscriptt_pk_id
  and tscript_status_id = tscripts_pk_id
  and tscript_status_id = ttsdef_tscript_type_id
  and tscript_type_id = ttsdef_tscript_status_id;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_markers/9clone.txt"
select clone_mrkr_zdb_id, replace(clone_comments,'
',''),clone_vector_name, clone_polymerase_name, clone_insert_size, clone_cloning_site,clone_digest,
			 clone_probelib_zdb_id, clone_sequence_type, replace(clone_pcr_amplification,'
',''), clone_rating, clone_problem_type,probe_library.*
 from clone,probe_library
  where clone_probelib_zdb_id = probelib_zdb_id
  and get_obj_type(clone_mrkr_Zdb_id) != 'GENE';

--ortholog

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_orthos/1orthos.txt"
 select ortho_other_species_ncbi_gene_id, ortho_zebrafish_gene_zdb_id, organism_common_name, ortho_other_species_symbol, current year to second,
 	ortho_other_species_name, replace(ortho_other_species_chromosome,"|",";"), replace(ortho_other_species_chromosome,"|",";"),
	oef_accession_number,fdb_db_name,fdbdt_data_type,oev_ortho_Zdb_id, oev_evidence_code, oev_pub_zdb_id,ortho_zdb_id||oef_accession_number
   from ortholog,ortholog_evidence,ortholog_external_reference,foreign_Db,foreign_db_data_type,foreign_db_Contains, organism
   where ortho_zdb_id = oef_ortho_zdb_id
   and ortho_other_species_taxid = organism_taxid
   and oef_fdbcont_Zdb_id =fdbcont_Zdb_id
   and fdbcont_Fdb_db_id = fdb_db_pk_id
   and fdbcont_fdbdt_id = fdbdt_pk_id
   and ortho_zdb_id = oev_ortho_zdb_id;

--stages

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_stages/1stages.txt"
  select stg_zdb_id, stg_name, stg_abbrev, stg_hours_start, stg_hours_end, stg_obo_id
   from stage;

--pubs

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_pubs/1pubs.txt"
  select zdb_id, replace(authors,'
',''), title, replace(accession_no,"none",""),
  	 jtype, pub_jrnl_zdb_id, pub_doi, pub_volume, pub_pages, substr(get_date_from_id(zdb_id,'YYYYMMDD'),1,4)
    from publication
where accession_no not in ('24135484','22615492','22071262','23603293','11581520','22328273','19700757');


unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_journals/1journals.txt"
  select jrnl_zdb_id, jrnl_name, jrnl_abbrev, jrnl_publisher
    from journal;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/go-annotation/evidence-codes.txt"
  select goev_code, goev_name
    from go_evidence_code;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/lab/feature-prefix-source.txt"
select sfp_prefix_id, sfp_source_zdb_id
From source_feature_prefix
 where get_obj_type(sfp_source_zdb_id) = "LAB"
 and sfp_current_designation = 't';

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/company/company-feature-prefix-source.txt"
select sfp_prefix_id, sfp_source_zdb_id
From source_feature_prefix
 where get_obj_type(sfp_source_zdb_id) = "COMPANY"
 and sfp_current_designation = 't';



unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/featurePrefix/feature-prefix.txt"
 select fp_pk_id, fp_prefix,fp_Institute_display
 from feature_prefix;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/disease/disease-annotation.txt"
 select dat_zdb_id,term_ont_id, dat_source_zdb_id, dat_evidence_code,  genox_fish_zdb_id, genox_exp_zdb_id
   from disease_annotation, disease_annotation_model, fish_experiment, term
   where dat_zdb_id = damo_dat_Zdb_id
 and damo_genox_zdb_id = genox_zdb_id
 and term_zdb_id = dat_term_zdb_id;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/eap/eap.txt"
  select ept_pk_id, ept_relational_term, ept_quality_term_zdb_id, ept_tag, ept_xpatres_id
    from expression_phenotype_term;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/phenoWarehouse/phenoWarehouse.txt"
  select pg_id, pg_genox_zdb_id, pg_fig_zdb_id, pg_start_stg_zdb_id, pg_end_stg_zdb_id, psg_id, psg_mrkr_zdb_id,
    psg_mrkr_abbrev,
    psg_mrkr_relation,
    psg_e1a_zdb_id,
    psg_e1a_name,
    psg_e1_relation_name,
    psg_e1b_zdb_id,
    psg_e1b_name,
    psg_e2a_zdb_id,
    psg_e2a_name,
    psg_e2_relation_name,
    psg_e2b_zdb_id,
    psg_e2b_name,
    psg_tag ,
    psg_quality_zdb_id,
    psg_quality_name,
    psg_short_name
from phenotype_source_generated, phenotype_observation_generated
where pg_id = psg_id;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/allele/allele.txt"
select feature_zdb_id, feature_name, feature_Type, feature_abbrev, fmrel_mrkr_zdb_id, mrkr_type
  from feature, feature_marker_relationship, marker
  where feature_zdb_id =fmrel_ftr_zdb_id
 and fmrel_mrkr_zdb_id = mrkr_zdb_id
 and fmrel_type = 'is allele of';

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/dnaMutationDetail/dnaMutationDetail.txt"
select fdmd_zdb_id,
    fdmd_feature_zdb_id,
    (select term_ont_id 
       from term where fdmd_dna_mutation_term_zdb_id = term_zdb_id),
    fdmd_dna_sequence_of_reference_accession_number,
    fdmd_fdbcont_zdb_id,
    fdmd_dna_position_start,
    fdmd_dna_position_end,
    fdmd_number_additional_dna_base_pairs,
    fdmd_number_removed_dna_base_pairs,
    fdmd_exon_number,
    fdmd_intron_number,
    (select term_ont_id 
       from term where fdmd_gene_localization_term_zdb_id =term_Zdb_id)
  from feature_dna_mutation_detail, feature
  where fdmd_feature_zdb_id = feature_zdb_id;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/transcriptMutationDetail/transcriptMutationDetail.txt"
select ftmd_zdb_id,
    (select term_ont_id from term where term_zdb_id = ftmd_transcript_consequence_term_zdb_id),
    ftmd_feature_zdb_id,
    ftmd_exon_number,
    ftmd_intron_number
  from feature_transcript_mutation_detail
  , feature
 where ftmd_feature_zdb_id = feature_zdb_id;

unload to "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/proteinMutationDetail/proteinMutationDetail.txt"
select fpmd_zdb_id,
    fpmd_feature_zdb_id,
    fpmd_sequence_of_reference_accession_number,
    fpmd_fdbcont_zdb_id,
    fpmd_protein_position_start,
    fpmd_protein_position_end,
    (select term_ont_id from term where fpmd_wt_protein_term_zdb_id = term_zdb_id),
    (select term_ont_id from term where fpmd_mutant_or_stop_protein_term_zdb_id = term_Zdb_id),
    fpmd_number_amino_acids_removed,
    fpmd_number_amino_acids_added,
    (select term_ont_id from term where fpmd_protein_consequence_term_zdb_id=term_Zdb_id)
 from feature_protein_mutation_detail, feature
      where fpmd_feature_zdb_id = feature_zdb_id;


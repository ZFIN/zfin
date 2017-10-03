create view thefish as
select fish_zdb_id, fish_name, fish_handle, fish_is_wildtype, fish_order, fish_functional_affected_gene_count, fish_genotype_zdb_id, ''
from fish
 where not exists (Select 'x' from fish_str
       	   	  	  where fish_Zdb_id = fishstr_fish_zdb_id)
union
select fish_zdb_id, fish_name, fish_handle, fish_is_wildtype, fish_order, fish_functional_affected_gene_count, fish_genotype_zdb_id, fishstr_str_zdb_id
from fish, fish_str
   where fish_Zdb_id = fishstr_fish_zdb_id;
\copy (select * from thefish) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/fish/1fish.txt' with delimiter as '|' null as '';
drop view thefish;

\copy (select di_pk_id, di_date_unloaded from database_info) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/dataDate/dateUnloaded.txt' with delimiter as '|' null as '';

create view mutagenMutagee as
select featassay_feature_zdb_id, featassay_mutagen, featassay_mutagee, feature_type
  from feature_assay
  full outer join feature
    on feature_zdb_id = featassay_feature_zdb_id;
\copy (select * from mutagenMutagee) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/mutagenMutagee/mutagenMutagee.txt' with delimiter as '|' null as '';
drop view mutagenMutagee;

create view cleanPhenotype as
select mfs_mrkr_zdb_id, genox_fish_zdb_id, genox_exp_zdb_id
 from mutant_fast_search, fish_experiment
  where mfs_mrkr_zdb_id like 'ZDB-GENE%'
 and mfs_genox_zdb_id = genox_zdb_id
;
\copy (select * from cleanPhenotype) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/cleanPhenotype/cleanPhenotype.txt' with delimiter as '|' null as '';
drop view cleanPhenotype;

\copy (select img_zdb_id, img_fig_zdb_id, img_label from image) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/images/images.txt' with delimiter as '|' null as '';

create view images as
select osubset_pk_id, osubset_subset_name, osubset_subset_definition,
       osubset_subset_type, osubset_ont_id, term_ont_id, term_ontology
 from ontology_subset, term_subset, term
  where osubset_pk_id = termsub_subset_id
  and termsub_term_zdb_id = term_zdb_id
  and term_is_obsolete = 'f'
  and term_is_secondary ='f';
\copy (select * from images) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/ontologySubset/1ontologySubset.txt' with delimiter as '|' null as '';
drop view images;

create view dataSourceSupplier as
select idsup_data_zdb_id, idsup_supplier_zdb_id, idsup_acc_num, idsup_avail_state, 'supplier', get_obj_type(idsup_data_zdb_id), feature_type
 from int_data_supplier, feature
where idsup_data_zdb_id like 'ZDB-ALT%'
and feature_zdb_id = idsup_data_zdb_id
union
select idsup_data_zdb_id, idsup_supplier_zdb_id, idsup_acc_num, idsup_avail_state, 'supplier', get_obj_type(idsup_data_zdb_id), ''
 from int_data_supplier
where idsup_data_zdb_id not like 'ZDB-ALT%'
union all
select ids_data_zdb_id, ids_source_zdb_id, '','','source', get_obj_type(ids_data_zdb_id), feature_type
from int_data_source, feature
where ids_data_zdb_id like 'ZDB-ALT%'
and feature_zdb_id = ids_data_zdb_id
union 
select ids_data_zdb_id, ids_source_zdb_id, '','','source', get_obj_type(ids_data_zdb_id), ''
from int_data_source
where ids_data_zdb_id not like 'ZDB-ALT%'
;
\copy (select * from dataSourceSupplier) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/dataSourceSupplier/1dataSourceSupplier.txt' with delimiter as '|' null as '';
drop view dataSourceSupplier;

create view labOfOrigin as
select ids_data_zdb_id, ids_source_zdb_id, feature_type
from int_data_source, feature
where ids_data_zdb_id like 'ZDB-ALT%'
and feature_zdb_id = ids_data_zdb_id
;
\copy (select * from labOfOrigin) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_features/labOfOrigin.txt' with delimiter as '|' null as '';
drop view labOfOrigin;

create view sequences as
 select mrkr_zdb_id, seq_sequence, seq_offset_start, seq_offset_stop, '', get_obj_type(mrkr_zdb_id),mrkr_zdb_id as id2, seq_sequence_2 from marker, marker_Sequence
where mrkr_Zdb_id = seq_mrkr_zdb_id
union
 select mrkr_zdb_id, seq_sequence, seq_offset_start, seq_offset_stop, seq_variation, get_obj_type(mrkr_zdb_id),mrkr_zdb_id, seq_sequence_2 from marker, snp_Sequence
where mrkr_Zdb_id = seq_mrkr_zdb_id
;
\copy (select * from sequences) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/markerSequences/1sequences.txt' with delimiter as '|' null as '';
drop view sequences;

create view omimphenotype as
 select ortho_zebrafish_gene_zdb_id,omimp_name,omimp_omim_id from omim_phenotype, ortholog
 where omimp_ortho_zdb_id = ortho_Zdb_id
 and ortho_zebrafish_gene_zdb_id not like 'ZDB-GENEP%';
\copy (select * from omimphenotype) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/omimPhenotype/1omimphenotype.txt' with delimiter as '|' null as '';
drop view omimphenotype;

create view featureCrossReferences as
 select feature_zdb_id, feature_type, dblink_acc_num, fdb_db_name, fdb_db_query from feature, db_link, foreign_db, foreign_db_contains
 where feature_zdb_id = dblink_linked_recid
 and fdb_db_pk_id = fdbcont_Fdb_db_id
and fdbcont_zdb_id = dblink_fdbcont_zdb_id;
\copy (select * from featureCrossReferences) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/featureCrossReferences/1featureCrossReferences.txt' with delimiter as '|' null as '';
drop view featureCrossReferences;

create view thechromosome as
select distinct sfclg_chromosome, sfclg_data_zdb_id from sequence_feature_chromosome_location_generated
 where sfclg_data_zdb_id like 'ZDB-GENE-%';
\copy (select * from thechromosome) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/chromosome/1chromosome.txt' with delimiter as '|' null as '';
drop view thechromosome;

--all identifiers;

create view allidentifiers as
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
\copy (select * from allidentifiers) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/identifiers/1identifiers.txt' with delimiter as '|' null as '';
drop view allidentifiers;

--featurePubs

create view featurePub as
select record_attribution.*, feature_type from record_attribution, feature
 where recattrib_data_zdb_id = feature_zdb_id
;
\copy (select * from featurePub) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/featurePubs/1featurePub.txt' with delimiter as '|' null as '';
drop view featurePub;

create view genoPubs as
select recattrib_source_zdb_id, recattrib_Data_zdb_id from record_Attribution,
  genotype 
 where recattrib_Data_zdb_id = geno_zdb_id;
\copy (select * from genoPubs) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/genotypePubs/1genoPubs.txt' with delimiter as '|' null as '';
drop view genoPubs;

create view thelab as
  select zdb_id, name, contact_person, url, email,fax,phone
    from lab;
\copy (select * from thelab) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/lab/1lab.txt' with delimiter as '|' null as '';
drop view thelab;

create view theperson as
  select zdb_id as person_id, first_name, last_name, full_name, email
    from person
   order by zdb_id;
\copy (select * from theperson) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/people/1person.txt' with delimiter as '|' null as '';
drop view theperson;

create view thecompany as
  select zdb_id, name, contact_person, url,email,fax,phone
    from company;
\copy (select * from thecompany) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/company/1company.txt' with delimiter as '|' null as '';
drop view thecompany;

create view personAssociations as
  select source_id, target_id
    from int_person_lab
union
select source_id, target_id from int_person_company
union
select source_id, target_id from int_person_pub;
\copy (select * from personAssociations) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/people/1person_associations.txt' with delimiter as '|' null as '';
drop view personAssociations;

--\copy (select * from xpat) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_expression/1xpat.txt' with delimiter as '|' null as '';
-- select * from expression_experiment
--   where exists (select 'x' from expression_result where xpatres_xpatex_zdb_id =xpatex_zdb_id) ;

create view xpatres as
 select res.xpatres_pk_id, res.xpatres_expression_found, anat.term_ont_id, a.stg_obo_id, b.stg_obo_id as id2,xpatex.xpatex_source_zdb_id,xpatex.xpatex_assay_name,xpatex.xpatex_probe_feature_zdb_id, xpatex.xpatex_gene_zdb_id, xpatex.xpatex_dblink_zdb_id, xpatex.xpatex_genox_zdb_id, xpatex.xpatex_atb_zdb_id, xpatfig.efs_fig_Zdb_id, termt.term_ont_id as id3, fish_zdb_id, genox_exp_zdb_id
  from expression_experiment2 xpatex
  join expression_figure_stage xpatfig
    on xpatfig.efs_xpatex_zdb_id = xpatex.xpatex_zdb_id
  join expression_result2 res
    on res.xpatres_efs_id = xpatfig.efs_pk_id
  join stage a
    on xpatfig.efs_start_stg_zdb_id = a.stg_zdb_id
  join stage b 
    on xpatfig.efs_end_stg_zdb_id = b.stg_zdb_id
  join term anat
    on res.xpatres_superterm_zdb_id = anat.term_zdb_id
  full outer join term termt 
    on res.xpatres_subterm_zdb_id = termt.term_zdb_id
  join fish_experiment
    on genox_zdb_id = xpatex_genox_zdb_id
  join fish
    on fish_zdb_id = genox_fish_zdb_id;
\copy (select * from xpatres) to './2xpatres.txt' with delimiter as '|' null as '';
drop view xpatres;

--\copy (select * from xpatfig) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_expression/3xpatfig.txt' with delimiter as '|' null as '';
-- select * from expression_pattern_figure;

create view figs as
 select fig_zdb_id,regexp_replace(fig_label,E'(^[\\n\\r]+)|([\\n\\r]+$)', '', 'g' ),fig_caption,fig_source_zdb_id from figure;
\copy (select * from figs) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_figures/1figs.txt' with delimiter as '|' null as '';
drop view figs;

--phenotype

create temp table tmp_pato (id int,
       genox_id text,
       superterm text,
       subterm text,
       superterm2 text,
       subterm2 text,
       startstg text,
       endstg text,
       fig text,
       tag text,
       quality text,
       geno_id text,
       exp_id text,
       clean boolean,
       mrkr_id text,
       short_name text)
;

insert into tmp_pato (id, genox_id, superterm, subterm, superterm2, subterm2, quality, startstg, endstg, fig, tag, geno_id, exp_id, mrkr_id, short_name)
  select psg_id,
  	 pg_genox_zdb_id,
	 a.term_ont_id,
	 b.term_ont_id as id2,
	 c.term_ont_id as id3,
	 d.term_ont_id as id4,
	 e.term_ont_id as id5,
	 f.stg_obo_id,
	 g.stg_obo_id as oboid2,
	 pg_fig_zdb_id, 
          psg_tag,
	  fish_zdb_id,
	  genox_exp_zdb_id,
	  psg_mrkr_Zdb_id,
	  psg_short_name
   from phenotype_source_generated
   join phenotype_observation_generated 
     on pg_id = psg_pg_id
   join stage f
     on pg_start_Stg_zdb_id = f.stg_zdb_id
   join stage g
     on pg_end_stg_zdb_id = g.stg_zdb_id
   join term a
     on psg_e1a_Zdb_id = a.term_Zdb_id
   full outer join term b
     on psg_e1b_zdb_id = b.term_zdb_id
   full outer join term c
     on psg_e2a_zdb_id = c.term_Zdb_id
   full outer join term d
     on psg_e2b_zdb_id = d.term_zdb_id
   join term e
     on psg_quality_Zdb_id = e.term_zdb_id
   join fish_experiment 
     on genox_zdb_id = pg_genox_zdb_id
   join fish
     on genox_fish_zdb_id = fish_zdb_id;

update tmp_pato
 set clean = 't'
 where exists (Select 'x' from mutant_fast_search
       	      	      where mfs_genox_zdb_id = genox_id);

update tmp_pato
 set clean = 'f'
 where clean is null;

\copy (select * from tmp_pato) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_phenotypes/1apato.txt' with delimiter as '|' null as '';

--\copy (select * from apatofig.txt) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_phenotypes/2apatofig.txt' with delimiter as '|' null as '';
--  select * from apato_figure;

--genotypesFeatures

create view genos as
  select distinct geno.*,(select a.zyg_name||','||b.zyg_name from zygocity a, zygocity b where a.zyg_zdb_id = genofeat_dad_zygocity and b.zyg_zdb_id = genofeat_mom_zygocity ),get_genotype_backgrounds(geno_zdb_id)
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
\copy (select * from genos) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_genotypes/1genos.txt' with delimiter as '|' null as '';
drop view genos;

create view features as
  select feature_zdb_id, feature_name, feature_abbrev, feature_type, feature_lab_prefix_id, featassay_mutagen, featassay_mutagee from feature, feature_Assay
  where not exists (Select 'x' from genotype_Feature
  	 		where genofeat_feature_zdb_id = feature_zdb_id)
 and featassay_feature_zdb_id = feature_zdb_id
 ;
\copy (select * from features) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_features/1features.txt' with delimiter as '|' null as '';
drop view features;

create view fmrels as 
  select feature_marker_relationship.*,feature_type from feature_marker_relationship, feature, feature_assay
   where fmrel_ftr_zdb_id = feature_zdb_id
   and featassay_feature_zdb_id = feature_zdb_id
;
\copy (select * from fmrels) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_fmrels/1fmrels.txt' with delimiter as '|' null as '';
drop view fmrels;

create view genofeats as 
 select genofeat_zdb_id, genofeat_geno_Zdb_id, genofeat_feature_zdb_id,
 	(select zyg_name from zygocity where zyg_zdb_id = genofeat_zygocity), feature_type,feature_name, feature_abbrev, ids_source_zdb_id, featassay_mutagen, featassay_mutagee,feature_lab_prefix_id
  from genotype_feature, feature, int_data_source, feature_assay
  where genofeat_feature_zdb_id = feature_zdb_id
  and ids_datA_zdb_id = feature_zdb_id
and featassay_feature_zdb_id = feature_zdb_id
 union 
select genofeat_zdb_id, genofeat_geno_Zdb_id, genofeat_feature_zdb_id,
 	(select zyg_name from zygocity where zyg_zdb_id = genofeat_zygocity), feature_type,feature_name, feature_abbrev, '', featassay_mutagen, featassay_mutagee,feature_lab_prefix_id
  from genotype_feature, feature,feature_assay
  where genofeat_feature_zdb_id = feature_zdb_id
  and featassay_feature_zdb_id = feature_zdb_id
 ;
\copy (select * from genofeats) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_genofeats/1genofeats.txt' with delimiter as '|' null as '';
drop view genofeats;

create view genoenvs as
 select genox_zdb_id, fish_zdb_id, genox_exp_zdb_id
   from fish_experiment, fish
   where fish_zdb_id = genox_fish_zdb_id
;
\copy (select * from genoenvs) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_genoenvs/1genoenvs.txt' with delimiter as '|' null as '';
drop view genoenvs;

create view envs as
  select exp_zdb_id, exp_source_zdb_id 
    from experiment, experiment_condition
   where expcond_exp_Zdb_id = exp_zdb_id
   union
   select exp_zdb_id, exp_source_zdb_id
   from experiment
   where not exists (select 'x' from experiment_condition 
   	     	    	    where expcond_exp_zdb_id = exp_zdb_id);
\copy (select * from envs) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_genoenvs/2envs.txt' with delimiter as '|' null as '';
drop view envs;  	     	    	    

create view exps as 
  select expcond_zdb_id,
         expcond_exp_zdb_id,
         zeco.term_ont_id,
         chebi.term_ont_id as id2,
	 zfa.term_ont_id as id3,
         gocc.term_ont_id as id4,
         taxon.term_ont_id as id5
    from experiment_condition
join experiment on exp_zdb_id = expcond_exp_zdb_id
left outer join term zeco on zeco.term_zdb_id = expcond_zeco_term_zdb_id  
left outer join term chebi on chebi.term_zdb_id = expcond_chebi_term_zdb_id
left outer join term zfa on zfa.term_zdb_id = expcond_ao_term_zdb_id
left outer join term gocc on gocc.term_zdb_id = expcond_go_cc_term_zdb_id
left outer join term taxon on taxon.term_zdb_id = expcond_taxon_term_zdb_id
union
  select exp_zdb_id,
         exp_zdb_id as expid2,
         '',
         '' as b2,
         '' as b3,
         '' as b4,
         '' as b5
    from experiment 
  where not exists (select 'x' from experiment_condition
                           where expcond_exp_zdb_id = exp_zdb_id)
;
\copy (select * from exps) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_experiments/1exps.txt' with delimiter as '|' null as '';
drop view exps;

--markers

\copy (select mrkr_zdb_id, mrkr_abbrev, mrkr_type, mrkr_name from marker) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_markers/1markers.txt' with delimiter as '|' null as '';


create view mrels as
 select mrel.* from marker_relationship mrel, marker a, marker b
  where a.mrkr_Zdb_id = mrel_mrkr_1_zdb_id
  and b.mrkr_Zdb_id = mrel_mrkr_2_zdb_id 
  and mrel_type != 'clone overlap'
  ;
\copy (select * from mrels) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_markers/2mrels.txt' with delimiter as '|' null as '';
drop view mrels;

create view dalias as
  select data_alias.*,alias_group.aliasgrp_name from data_alias, alias_group
    where exists (select 'x' from marker where mrkr_zdb_id = dalias_data_zdb_id)
    and aliasgrp_pk_id = dalias_group_id;
\copy (select * from dalias) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_markers/3dalias.txt' with delimiter as '|' null as '';
drop view dalias;

create view dalias2 as
  select data_alias.*,alias_group.aliasgrp_name,feature_type from data_alias, alias_group, feature
    where feature_zdb_id = dalias_data_zdb_id
    and aliasgrp_pk_id = dalias_group_id;
\copy (select * from dalias2) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/feature_alias/1dalias.txt' with delimiter as '|' null as '';
drop view dalias2;

create view dalias3 as
  select data_alias.*,alias_group.aliasgrp_name from data_alias, alias_group
    where exists (select 'x' from genotype where geno_zdb_id = dalias_data_zdb_id)
    and aliasgrp_pk_id = dalias_group_id;
\copy (select * from dalias3) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/genotype_alias/1dalias.txt' with delimiter as '|' null as '';
drop view dalias3;

create view replaceddata as
  select zdb_replaced_data.* from zdb_replaced_data, marker
   where zrepld_new_zdb_id = mrkr_zdb_id;
\copy (select * from replaceddata) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_markers/4replaceddata.txt' with delimiter as '|' null as '';
drop view replaceddata;

create view dblinks as
select dblink_zdb_id, fdb_db_name, dblink_acc_num, dblink_linked_recid, fdbdt_data_type, fdb_db_query, fdb_db_name as n2
  from db_link, marker, foreign_db_contains, foreign_db, foreign_Db_data_type
  where dblink_linked_recid = mrkr_zdb_id
  and fdbcont_zdb_id = dblink_fdbcont_Zdb_id
  and fdbcont_fdbdt_id = fdbdt_pk_id
  and fdbcont_fdb_db_id = fdb_db_pk_id
  and fdb_db_name not in ('miRBASE Mature','miRBASE Stem Loop','unreleaseRNA','ZFIN','ZFIN_PROT','Curated miRNA Mature','Curated miRNA Stem Loop')
union all
 select snpd_mrkr_zdb_id||snpd_rs_acc_num, 'dbSNP' as t2,snpd_rs_acc_num, snpd_mrkr_zdb_id, 'genomic' as t3, '' as b1, '' as b2
   from snp_download;
\copy (select * from dblinks) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_markers/5dblinks.txt' with delimiter as '|' null as '';
drop view dblinks;

create view keggMapping as
  select dblink_acc_num, dblink_zdb_id, dblink_linked_recid 
    from db_link, foreign_db_contains, foreign_db
    where dblink_fdbcont_zdb_id = fdbcont_zdb_id
    and fdb_db_pk_id = fdbcont_fdb_db_id
    and fdbcont_organism_common_name ='Zebrafish'
    and fdb_db_name = 'Entrez Gene';
\copy (select * from keggMapping) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_kegg/1keggMapping.txt' with delimiter as '|' null as '';
drop view keggMapping;

create view recAttr as
 select * from record_attribution
   where recattrib_data_zdb_id like 'ZDB-ALT%'
 or recattrib_data_zdb_id like 'ZDB-GENO%';
\copy (select * from recAttr) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/mutantAttributions/1recattrib.txt' with delimiter as '|' null as '';
drop view recAttr;

create view recAttr2 as
select recattrib_data_zdb_id, recattrib_source_zdb_id
  from record_Attribution, marker
  where mrkr_zdb_id = recattrib_data_zdb_id;
\copy (select * from recAttr2) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_markers/6recattrib.txt' with delimiter as '|' null as '';
drop view recAttr2;

\copy (select * from antibody) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_markers/7antibody.txt' with delimiter as '|' null as '';

create view trans as
select tscript_mrkr_Zdb_id,
       tscriptt_type,
       regexp_replace(tscriptt_definition,E'(^[\\n\\r]+)|([\\n\\r]+$)', '', 'g' ),
       regexp_replace(ttsdef_definition,E'(^[\\n\\r]+)|([\\n\\r]+$)', '', 'g' ) as r2,
       tscripts_status
  from transcript
  full outer join transcript_status on  tscript_status_id = tscripts_pk_id
  full outer join transcript_type on tscript_type_id = tscriptt_pk_id
  full outer join tscript_type_status_definition on tscript_type_id = ttsdef_tscript_status_id
;
\copy (select * from trans) to './8transcript3.txt' with delimiter as '|' null as '';
drop view trans;

create view clones as
select clone_mrkr_zdb_id, regexp_replace(clone_comments,E'(^[\\n\\r]+)|([\\n\\r]+$)', '', 'g' ),
    clone_vector_name, clone_polymerase_name, clone_insert_size, clone_cloning_site,clone_digest,
			 clone_probelib_zdb_id, clone_sequence_type, regexp_replace(clone_pcr_amplification,E'(^[\\n\\r]+)|([\\n\\r]+$)', '', 'g' ) as r2,
    clone_rating, clone_problem_type,probe_library.*
 from clone,probe_library
  where clone_probelib_zdb_id = probelib_zdb_id
  and get_obj_type(clone_mrkr_Zdb_id) != 'GENE';
\copy (select * from clones) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_markers/9clone.txt' with delimiter as '|' null as '';
drop view clones;

--ortholog
create view orthos as
 select ortho_other_species_ncbi_gene_id, ortho_zebrafish_gene_zdb_id, organism_common_name, ortho_other_species_symbol, now()::timestamp(0),
        ortho_other_species_name, replace(ortho_other_species_chromosome,'|',';'), replace(ortho_other_species_chromosome,'|',';') as r2,
        oef_accession_number,fdb_db_name,fdbdt_data_type,oev_ortho_Zdb_id, oev_evidence_code, oev_pub_zdb_id,ortho_zdb_id||oef_accession_number
   from ortholog,ortholog_evidence,ortholog_external_reference,foreign_Db,foreign_db_data_type,foreign_db_Contains, organism
   where ortho_zdb_id = oef_ortho_zdb_id
   and ortho_other_species_taxid = organism_taxid
   and oef_fdbcont_Zdb_id =fdbcont_Zdb_id
   and fdbcont_Fdb_db_id = fdb_db_pk_id
   and fdbcont_fdbdt_id = fdbdt_pk_id
   and ortho_zdb_id = oev_ortho_zdb_id;
\copy (select * from orthos) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_orthos/1orthos.txt' with delimiter as '|' null as '';
drop view orthos;

--stages

create view stages as
  select stg_zdb_id, stg_name, stg_abbrev, stg_hours_start, stg_hours_end, stg_obo_id
   from stage;
\copy (select * from stages) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_stages/1stages.txt' with delimiter as '|' null as '';
drop view stages;

--pubs

create view pubs as
  select zdb_id, regexp_replace(authors,E'(^[\\n\\r]+)|([\\n\\r]+$)', '', 'g' ), title, accession_no,
         jtype, pub_jrnl_zdb_id, pub_doi, pub_volume, pub_pages, substring(get_date_from_id(zdb_id,'YYYYMMDD') from 1 for 4)
    from publication
where accession_no not in ('24135484','22615492','22071262','23603293','11581520','22328273','19700757');
\copy (select * from pubs) to './1pubs.txt' with delimiter as '|' null as '';
drop view pubs;

create view journals as
  select jrnl_zdb_id, jrnl_name, jrnl_abbrev, jrnl_publisher
    from journal;
\copy (select * from journals) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_journals/1journals.txt' with delimiter as '|' null as '';
drop view journals;

\copy (select goev_code, goev_name from go_evidence_code) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/go-annotation/evidence-codes.txt' with delimiter as '|' null as '';

create view eaturePrefixSource as
select sfp_prefix_id, sfp_source_zdb_id
From source_feature_prefix
 where get_obj_type(sfp_source_zdb_id) = 'LAB'
 and sfp_current_designation = 't';
\copy (select * from eaturePrefixSource) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/lab/feature-prefix-source.txt' with delimiter as '|' null as '';
drop view eaturePrefixSource;

create view companyFeaturePrefixSrc as 
select sfp_prefix_id, sfp_source_zdb_id
From source_feature_prefix
 where get_obj_type(sfp_source_zdb_id) = 'COMPANY'
 and sfp_current_designation = 't';
\copy (select * from companyFeaturePrefixSrc) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/company/company-feature-prefix-source.txt' with delimiter as '|' null as '';
drop view companyFeaturePrefixSrc;

\copy (select fp_pk_id, fp_prefix,fp_Institute_display from feature_prefix) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/featurePrefix/feature-prefix.txt' with delimiter as '|' null as '';

create view diseaseAnnotation as
 select dat_zdb_id,term_ont_id, dat_source_zdb_id, dat_evidence_code,  genox_fish_zdb_id, genox_exp_zdb_id
   from disease_annotation, disease_annotation_model, fish_experiment, term
   where dat_zdb_id = damo_dat_Zdb_id
 and damo_genox_zdb_id = genox_zdb_id
 and term_zdb_id = dat_term_zdb_id;
\copy (select * from diseaseAnnotation) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/disease/disease-annotation.txt' with delimiter as '|' null as '';
drop view diseaseAnnotation;

create view eap as
  select ept_pk_id, ept_relational_term, ept_quality_term_zdb_id, ept_tag, ept_xpatres_id
    from expression_phenotype_term;
\copy (select * from eap) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/eap/eap.txt' with delimiter as '|' null as '';
drop view eap;

create view phenoWarehouse as
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
\copy (select * from phenoWarehouse) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/phenoWarehouse/phenoWarehouse.txt' with delimiter as '|' null as '';
drop view phenoWarehouse;

create view allele as
select feature_zdb_id, feature_name, feature_Type, feature_abbrev, fmrel_mrkr_zdb_id, mrkr_type
  from feature, feature_marker_relationship, marker
  where feature_zdb_id =fmrel_ftr_zdb_id
 and fmrel_mrkr_zdb_id = mrkr_zdb_id
 and fmrel_type = 'is allele of';
\copy (select * from allele) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/allele/allele.txt' with delimiter as '|' null as '';
drop view allele;

create view dnaMutationDetail as
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
    (select term_ont_id as id2
       from term where fdmd_gene_localization_term_zdb_id =term_Zdb_id)
  from feature_dna_mutation_detail, feature
  where fdmd_feature_zdb_id = feature_zdb_id;
\copy (select * from dnaMutationDetail) to './dnaMutationDetail.txt' with delimiter as '|' null as '';
drop view dnaMutationDetail;

create view transcriptMutationDetail as
select ftmd_zdb_id,
    (select term_ont_id from term where term_zdb_id = ftmd_transcript_consequence_term_zdb_id),
    ftmd_feature_zdb_id,
    ftmd_exon_number,
    ftmd_intron_number
  from feature_transcript_mutation_detail
  , feature
 where ftmd_feature_zdb_id = feature_zdb_id;
\copy (select * from transcriptMutationDetail) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/transcriptMutationDetail/transcriptMutationDetail.txt' with delimiter as '|' null as '';
drop view transcriptMutationDetail;

create view proteinMutationDetail as
select fpmd_zdb_id,
    fpmd_feature_zdb_id,
    fpmd_sequence_of_reference_accession_number,
    fpmd_fdbcont_zdb_id,
    fpmd_protein_position_start,
    fpmd_protein_position_end,
    (select term_ont_id from term where fpmd_wt_protein_term_zdb_id = term_zdb_id),
    (select term_ont_id as id2 from term where fpmd_mutant_or_stop_protein_term_zdb_id = term_Zdb_id),
    fpmd_number_amino_acids_removed,
    fpmd_number_amino_acids_added,
    (select term_ont_id as id3 from term where fpmd_protein_consequence_term_zdb_id=term_Zdb_id)
 from feature_protein_mutation_detail, feature
      where fpmd_feature_zdb_id = feature_zdb_id;
\copy (select * from proteinMutationDetail) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/proteinMutationDetail/proteinMutationDetail.txt' with delimiter as '|' null as '';
drop view proteinMutationDetail;

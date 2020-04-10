
drop table if exists tmp_fish_thatfitthebill;
drop table if exists tmp_fishnoeap;
drop table if exists tmp_onlyfish;
drop table if exists tmp_fish490;
drop table if exists tmp_onlydeficiencies;

select fmrel_ftr_zdb_id as featdef
into tmp_onlydeficiencies
from feature, feature_marker_relationship, marker
where feature_type='DEFICIENCY'
and fmrel_ftr_zdb_id=feature_zdb_id
and fmrel_mrkr_zdb_id=mrkr_zdb_id
group by fmrel_ftr_zdb_id having count(fmrel_ftr_zdb_id)=1;

select distinct fish_zdb_id , fish_name, concat(fc_gene_zdb_id,E'\t',mrkr_abbrev,E'\t',fc_affector_zdb_id,E'\t',feature_abbrev) as affector
into tmp_onlyfish
from fish, fish_components, feature,marker
where fish_functional_affected_gene_count=2
and fish_zdb_id=fc_fish_zdb_id
and fc_affector_zdb_id like 'ZDB-ALT%'
and fc_affector_zdb_id = feature_zdb_id
and feature_type in ('POINT_MUTATION','COMPLEX','DELETION','INSERTION','INDEL')
and fc_gene_zdb_id=mrkr_zdb_id
union
select distinct fish_zdb_id, fish_name, concat(featdef,E'\t',feature_abbrev,E'\t',mrkr_zdb_id,E'\t',mrkr_abbrev,E'\t',feature_type) as affector
from fish, genotype_Feature, feature_marker_relationship, feature,marker,tmp_onlydeficiencies
where fish_functional_affected_gene_count=2
and fish_genotype_zdb_id = genofeat_geno_zdb_id
and genofeat_feature_Zdb_id = featdef
and feature_zdb_id = featdef
and featdef=fmrel_ftr_zdb_id
and fmrel_mrkr_zdb_id = mrkr_zdb_id
union
select distinct fish_zdb_id , fish_name, concat(fc_gene_zdb_id,E'\t',mrkr_abbrev,E'\t',fc_affector_zdb_id,E'\t',feature_abbrev) as affector
from fish, fish_components, feature, feature_marker_relationship, marker
where fish_functional_affected_gene_count=2
and fish_zdb_id=fc_fish_zdb_id
and fc_affector_zdb_id like 'ZDB-ALT%'
and fc_affector_zdb_id = feature_zdb_id
and feature_type in ('TRANSGENIC_INSERTION')
and fmrel_type='contains innocuous sequence feature'
and fmrel_ftr_zdb_id=fc_affector_zdb_id
and fc_gene_zdb_id=mrkr_zdb_id
and fmrel_mrkr_zdb_id=fc_gene_zdb_id
union
select distinct fish_zdb_id , fish_name, concat(fc_gene_zdb_id,E'\t',b.mrkr_abbrev,E'\t',fc_affector_zdb_id,E'\t',a.mrkr_abbrev) as affector
from fish, fish_components, marker a, marker b
where fish_functional_affected_gene_count=2
and fish_zdb_id=fc_fish_zdb_id
and (fc_affector_zdb_id like 'ZDB-MRPH%' or fc_affector_zdb_id like 'ZDB-TALEN%' or fc_affector_zdb_id like 'ZDB-CRISPR%')
and a.mrkr_zdb_id=fc_affector_zdb_id
and b.mrkr_zdb_id=fc_gene_zdb_id;


select fish_zdb_id, fish_name, string_agg(affector,',') as affected_components into tmp_fish490 from tmp_onlyfish group by fish_zdb_id, fish_name;




select distinct fish_zdb_id , fish_name,affected_components, genox_exp_zdb_id,exp_name,genox_zdb_id,
nvl((Select term_ont_id from term where term_zdb_id = psg_e1b_zdb_id),'') as e1subtermid ,nvl(psg_e1b_name,'') as e1subtermname,psg_e1_relation_name,nvl((Select term_ont_id from term where term_zdb_id = psg_e1a_zdb_id),'') as e1supertermid ,nvl(psg_e1a_name,'') as e1supertermname,
nvl((Select term_ont_id from term where term_zdb_id =psg_e2b_zdb_id),'') as e2subtermid,nvl(psg_e2b_name,'') as e2subtermname ,psg_e2_relation_name,nvl((Select term_ont_id from term where term_zdb_id =psg_e2a_zdb_id),'') as e2supertermid,nvl(psg_e2a_name,'') as e2supertermname,
psg_tag,nvl((Select term_ont_id from term where term_zdb_id = psg_quality_zdb_id),'') as qualityid,psg_quality_name,fig_zdb_id, fig_label,fig_source_zdb_id,coalesce(accession_no,0) as pubmedid
into tmp_fish_thatfitthebill from tmp_fish490,fish_Experiment, experiment,phenotype_source_generated,phenotype_observation_generated,figure,publication
     where (psg_tag like '%ameliorated%' or psg_tag like '%exacerbated%')
	and psg_pg_id = pg_id
	and genox_Zdb_id = pg_genox_zdb_id
 and exp_zdb_id=genox_exp_zdb_id
and genox_is_std_or_generic_control = 't'
and genox_fish_zdb_id = fish_zdb_id
and fig_zdb_id = pg_fig_zdb_id
and fig_source_zdb_id=zdb_id
order by fish_zdb_id;


select  * into tmp_fishnoeap from tmp_fish_thatfitthebill
where genox_zdb_id not in
(select distinct genox_zdb_id
from fish_experiment, expression_experiment2, expression_figure_stage,expression_result2, expression_phenotype_term
where genox_zdb_id=xpatex_genox_zdb_id
and xpatex_zdb_id=efs_xpatex_zdb_id and efs_pk_id=xpatres_efs_id
and xpatres_pk_id=ept_xpatres_id);

update tmp_fishnoeap set psg_e1_relation_name='' where e1subtermname='';
update tmp_fishnoeap set psg_e2_relation_name='' where e2subtermname='';
update tmp_fishnoeap set affected_components=replace(affected_components,',',E'\t') where affected_components like '%,%';

select affected_components, genox_exp_zdb_id,exp_name,e1subtermid,e1subtermname,psg_e1_relation_name,
e1supertermid, e1supertermname,e2subtermid,e2subtermname,psg_e2_relation_name,e2supertermid,e2supertermname,psg_tag,qualityid,psg_quality_name,fig_zdb_id, fig_label,fig_source_zdb_id, pubmedid,fish_zdb_id , fish_name
 from tmp_fishnoeap;

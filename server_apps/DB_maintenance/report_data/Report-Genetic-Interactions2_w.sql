--select distinct mrkr_zdb_id, concat(pub,'&',inf,'&',ont,'&',term_ont_id) as bundle, pub,inf,ont,term_ont_id into finalct3 from finalgo;
--select bundle,pub,inf,ont,term_ont_id into finaltemp from finalct3 group by bundle,pub,inf,ont,term_ont_id having count(bundle)>1;


drop table if exists tmp_go;
drop table if exists tmp_go1;
drop table if exists tmp_go4;
drop table if exists tmp_go5;
drop table if exists tmp_go;
drop table if exists finalgo;
drop table if exists finalct3;
drop table if exists finaltemp;
drop table if exists jenk491;
drop table if exists jenk491b;
drop table if exists final491;
drop table if exists tmp_final;


select
mrkrgoev_zdb_id,mrkr_zdb_id, mrkr_abbrev,  term_ont_id, mrkrgoev_source_zdb_id as pub,infgrmem_inferred_from as grouped_item,
upper(substring(term_ontology from 1 for 1)) as ont
into tmp_go
from marker_go_term_evidence
join marker on mrkrgoev_mrkr_zdb_id = mrkr_zdb_id
join term on mrkrgoev_term_zdb_id = term_zdb_id
join publication on mrkrgoev_source_zdb_id  = zdb_id
full outer join inference_group_member on mrkrgoev_zdb_id = infgrmem_mrkrgoev_zdb_id
where  mrkrgoev_evidence_code='IGI' and mrkrgoev_annotation_organization_created_by='ZFIN' and mrkrgoev_gflag_name is null and infgrmem_inferred_from not like '%GENE%';


select mrkrgoev_zdb_id,mrkr_zdb_id,pub,string_agg(grouped_item,E'|') as inf,ont,term_ont_id into tmp_go1 from tmp_go
group by mrkrgoev_zdb_id,pub,ont,term_ont_id,mrkr_zdb_id having count(grouped_item) = 2 ;

select distinct mrkr_zdb_id,pub,inf,ont,term_ont_id into finalgo from tmp_go1 group by pub,inf,ont,term_ont_id,mrkr_zdb_id;

select distinct mrkr_zdb_id, concat(pub,'&',inf,'&',ont,'&',term_ont_id) as bundle, pub,inf,ont,term_ont_id into finalct3 from finalgo;




select pub,inf as messup,ont,term_ont_id as goid into jenk491 from finalct3 group by bundle,pub,inf,ont,term_ont_id having count(bundle)>1;



update jenk491 set messup=replace(messup,'|',',');
SELECT pub,unnest(string_to_array(messup, ',')) as inference, goid,ont,messup into jenk491b from jenk491;

select pub, concat(mrkr_zdb_id,E'\t',mrkr_abbrev,E'\t',feature_zdb_id,E'\t',feature_abbrev) as affector,goid,ont,messup
into final491
from jenk491b, genotype_Feature, feature_marker_relationship, feature,marker
where inference like '%ZDB-GENO%'
and inference = 'ZFIN:'||genofeat_geno_zdb_id
and genofeat_feature_Zdb_id = feature_zdb_id
and feature_zdb_id=fmrel_ftr_zdb_id
and fmrel_mrkr_zdb_id = mrkr_zdb_id
and fmrel_type='is allele of'
union
select pub, concat(b.mrkr_zdb_id,E'\t',b.mrkr_abbrev,E'\t',a.mrkr_zdb_id,E'\t',a.mrkr_abbrev) as affector,goid,ont,messup
from jenk491b, marker a, marker b,marker_relationship
where (inference like '%ZDB-MRPH%' or inference like '%ZDB-CRISPR%' or inference like '%ZDB-TALEN%')
and inference='ZFIN:'||a.mrkr_zdb_id and inference='ZFIN:'||mrel_mrkr_1_zdb_id and mrel_type='knockdown reagent targets gene'
and b.mrkr_zdb_id=mrel_mrkr_2_zdb_id
order by pub;

select pub,  string_agg(affector,',') as affected_components,goid, ont,messup into tmp_final from final491 group by pub,goid,ont,messup;
update tmp_final set affected_components=replace(affected_components,',',E'\t') where affected_components like '%,%';
select pub,accession_no,affected_components,
case when strpos(messup,'GENO')>0 then substring(messup,0,strpos(messup,',')) else '' end ,
ont,goid,term_name
from tmp_final
join publication on pub  = zdb_id
join term on goid=term_ont_id
order by affected_components;






















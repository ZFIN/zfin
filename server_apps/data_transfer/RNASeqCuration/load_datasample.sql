begin work;

create temp table tmp_datasample(datasample_id text,
		sample_title text,
		abundance text,
		sampleType text,
		sampleStage text,
		samplStageName text,
		sampleAge_list text,
		cellularComponentQualifierTerm text,
		anatomicalStructureTerm text,
		anatomicalStructureQualifier text,
		anatomicalSubStructureTerm text,
		anatomicalSubStructureQualifierTerm text,
		bioSampleID text,
		idType text,
		sampleText text,
		taxonid text,
		sex text,
		assayType text,
		sequencingFormat text,
		assembly text,
		notes text,
		datasetId text);

\copy tmp_datasample from '/opt/zfin/source_roots/swirl/ZFIN_WWW/server_apps/data_transfer/RNASeqCuration/data_sample.txt' with delimiter E'\t' null as '';
delete from tmp_datasample where sampleType is null;

insert into htp_dataset_sample(hds_sample_id, 
				hds_sample_title,
				hds_sample_type,
				hds_fish_Zdb_id,
				hds_sex,
				hds_assay_type,
				hds_sequencing_format,
				hds_hd_zdb_id,
				hds_abundance,
				hds_assembly,
				hds_notes,
				hds_stage_term_zdb_id)
select distinct datasample_id,
	sample_title,
	sampleType,
	bioSampleID,
	sex,
	assayType,
	sequencingFormat,
	(select hd_zdb_id from htp_dataset
		where hd_original_dataset_id = datasetId),
	abundance,
	assembly,
	notes,
	  (select term_zdb_id from term where term_ont_id = sampleStage)
  from tmp_datasample
;

update tmp_datasample
  set datasample_id = sample_title
   where datasample_id is null or datasample_id = '';

update htp_dataset_sample
  set hds_sample_id = hds_sample_title where hds_sample_id is null or hds_sample_id = '';

insert  into htp_dataset_sample_detail(hdsd_hds_id,
					hdsd_anatomy_super_term_zdb_id,
					hdsd_anatomy_sub_term_zdb_id,
					hdsd_anatomy_super_term_qualifier_zdb_id,
					hdsd_anatomy_sub_term_qualifier_zdb_id,
					hdsd_cellular_component_term_qualifier_Zdb_id)
select distinct hds_pk_id,
	(select term_zdb_id from term where term_ont_id = anatomicalStructureTerm),
	(select term_zdb_id from term where term_ont_id = anatomicalSubStructureTerm),
	(select term_zdb_id from term where term_ont_id = anatomicalStructureQualifier),
	(select term_Zdb_id from term where term_ont_id = anatomicalSubStructureQualifierTerm),
	(select term_zdb_id from term where term_ont_id = cellularComponentQualifierTerm)
   from tmp_datasample, htp_dataset_sample, htp_dataset
   where hds_hd_zdb_id = hd_zdb_id
   and hd_original_dataset_id = datasetId
   and hds_sample_id = datasample_id;


--rollback work;
commit work;

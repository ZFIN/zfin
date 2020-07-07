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
				hds_notes)
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
	notes
  from tmp_datasample
  where datasetId like 'GEO%';

insert into htp_dataset_sample(hds_sample_id, 
                                hds_sample_title,
                                hds_sample_type,
                                hds_fish_Zdb_id,
                                hds_sex,
                                hds_assay_type,
                                hds_sequencing_format,
                                hds_hd_zdb_id,
				hds_abundance)
select distinct datasample_id,
        sample_title,
        sampleType,
        bioSampleID,
        sex,
        assayType,
        sequencingFormat,
        (select	hd_zdb_id from htp_dataset
                where hd_original_dataset_id = datasetId),
	abundance
  from tmp_datasample
  where	datasetId like 'ArrayExpress%';

insert  into htp_dataset_sample_stage(hdss_hds_id, hdss_stage_term_zdb_id,
					hdss_anatomy_super_term_zdb_id,
					hdss_anatomy_sub_term_zdb_id,
					hdss_anatomy_super_term_qualifier_zdb_id,
					hdss_anatomy_sub_term_qualifier_zdb_id,
					hdss_cellular_component_term_qualifier_Zdb_id)
select distinct hds_pk_id, 
	(select term_zdb_id from term where term_ont_id = sampleStage),
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

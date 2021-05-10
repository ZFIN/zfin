
--liquibase formatted sql
--changeset pm:ZFIN-6948


insert into sequence_feature_chromosome_location_generated (sfclg_chromosome, sfclg_data_zdb_id,
    sfclg_acc_num ,
    sfclg_start ,
    sfclg_end,
    sfclg_assembly,
    sfclg_location_source,
    sfclg_location_subsource,
    sfclg_evidence_code)
 select distinct sfcl_chromosome, fmrel_mrkr_zdb_id,
    sfcl_chromosome_reference_accession_number,
    sfcl_start_position ,
    sfcl_end_position,
    sfcl_assembly,
    'other map location',
        ' geneLocationPullThruFromAllele',

    sfcl_evidence_code from sequence_feature_chromosome_location, feature_marker_relationship
where sfcl_feature_zdb_id=fmrel_ftr_zdb_id
and fmrel_type='is allele of'
and fmrel_mrkr_zdb_id not in (select sfclg_data_zdb_id from sequence_feature_chromosome_location_generated);

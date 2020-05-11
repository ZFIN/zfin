--liquibase formatted sql
--changeset sierra:add_gsc_for_inversion.sql


insert into genotype_component_significance (gcs_mrkr_type, gcs_ftr_type, gcs_fmrel_type, gcs_significance)
 values ('GENE','INVERSION','is allele of', 1);

insert into genotype_component_significance (gcs_mrkr_type, gcs_ftr_type, gcs_fmrel_type, gcs_significance)
 values	('GENE','INVERSION','created by', 2);

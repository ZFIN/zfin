--liquibase formatted sql
--changeset sierra:genotype_components.sql

insert into genotype_component_significance(gcs_mrkr_type, gcs_ftr_type, gcs_fmrel_type, gcs_significance)
 select marker_type, gcs_ftr_type, gcs_fmrel_type, gcs_significance
    from genotype_component_significance, marker_types
    where marker_type like '%G'
and gcs_mrkr_type = 'GENE';

update genotype
  set geno_display_name = get_genotype_display(geno_Zdb_id)
 where geno_display_name = '';
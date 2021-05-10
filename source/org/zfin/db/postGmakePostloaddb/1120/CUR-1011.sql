--liquibase formatted sql
--changeset pm:CUR-1011.sql

insert into genotype_component_significance(gcs_mrkr_type, gcs_ftr_type, gcs_fmrel_type, gcs_significance)
 select marker_type, gcs_ftr_type, gcs_fmrel_type, gcs_significance
    from genotype_component_significance, marker_types,marker_type_group_member
    where marker_type=mtgrpmem_mrkr_type and mtgrpmem_mrkr_type_group like 'NONT%'
and gcs_mrkr_type = 'GENE';



update genotype
  set geno_display_name = get_genotype_display(geno_Zdb_id)
 where geno_display_name = '';

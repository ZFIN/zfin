--liquibase formatted sql
--changeset pm:ZFIN-6266


update feature_genomic_mutation_detail set fgmd_sequence_of_reference='' where fgmd_sequence_of_reference is null;




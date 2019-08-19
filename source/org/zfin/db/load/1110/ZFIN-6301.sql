--liquibase formatted sql
--changeset pm:ZFIN-6301.sql


insert into feature_type (ftrtype_name,
 ftrtype_significance,
 ftrtype_type_display ) values ('MNV',10,'MNV');

insert into feature_type_group_member (ftrgrpmem_ftr_type,ftrgrpmem_ftr_type_group) values ('MNV','MUTANT');

update feature set feature_type='MNV'
where feature_zdb_id in (select fgmd_feature_zdb_id from feature_genomic_mutation_detail, feature where fgmd_feature_zdb_id=feature_zdb_id and feature_type='INDEL' and length(fgmd_sequence_of_reference)=length(fgmd_sequence_of_variation));
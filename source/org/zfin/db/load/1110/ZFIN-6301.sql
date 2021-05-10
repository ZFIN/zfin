--liquibase formatted sql
--changeset pm:ZFIN-6301.sql


insert into feature_type (ftrtype_name,
 ftrtype_significance,
 ftrtype_type_display ) values ('MNV',10,'MNV');

insert into feature_type_group_member (ftrgrpmem_ftr_type,ftrgrpmem_ftr_type_group) values ('MNV','MUTANT');

update feature set feature_type='MNV'
where feature_zdb_id in (select fgmd_feature_zdb_id from feature_genomic_mutation_detail, feature where fgmd_feature_zdb_id=feature_zdb_id and feature_type='INDEL' and length(fgmd_sequence_of_reference)=length(fgmd_sequence_of_variation));

update feature set feature_type='MNV'
where feature_zdb_id in (select fdmd_feature_zdb_id from feature_dna_mutation_detail, feature where fdmd_feature_zdb_id=feature_zdb_id and feature_type='INDEL' and fdmd_number_additional_dna_base_pairs=fdmd_number_removed_dna_base_pairs and fdmd_number_additional_dna_base_pairs>1);


insert into feature_type_mutagen_group_member (ftmgm_feature_type,ftmgm_mutagen) values ('MNV', 'ENU');
insert into feature_type_mutagen_group_member (ftmgm_feature_type,ftmgm_mutagen) values ('MNV','TMP');
insert into feature_type_mutagen_group_member (ftmgm_feature_type,ftmgm_mutagen)  values ('MNV','g-rays');
insert into feature_type_mutagen_group_member (ftmgm_feature_type,ftmgm_mutagen)  values ('MNV', 'spontaneous');
insert into feature_type_mutagen_group_member (ftmgm_feature_type,ftmgm_mutagen) values ('MNV', 'zinc finger nuclease');
insert into feature_type_mutagen_group_member (ftmgm_feature_type,ftmgm_mutagen)  values ('MNV','TALEN');
insert into feature_type_mutagen_group_member (ftmgm_feature_type,ftmgm_mutagen)  values ('MNV', 'CRISPR');
insert into feature_type_mutagen_group_member (ftmgm_feature_type,ftmgm_mutagen)  values ('MNV','not specified');
insert into feature_type_mutagen_group_member (ftmgm_feature_type,ftmgm_mutagen)  values ('MNV','EMS');
--liquibase formatted sql 
--changeset sierra:moreIndexRenaming.sql

alter table lab
drop constraint lab_contact_person_foreign_key);

create index lab_contact_person_fk_index
 on lab (contact_person)
using btree in idxdbs2;

alter table lab add constraint (foreign key (contact_person) 
    references person  constraint lab_contact_person_foreign_key);


alter table int_person_lab
 drop constraint int_person_lab_position_foreign_key;

create index int_person_lab_position_fk_index
on int_person_lab (position_id)
using btree in idxdbs1;

alter table int_person_lab add constraint (foreign 
    key (position_id) references lab_position  constraint 
    int_person_lab_position_foreign_key);


alter table int_vegatype_tscripttype
 drop constraint ivtst_tscriptt_type_id_foreign_key;

create index ivtst_tscriptt_type_id_fk_index
 on int_vegatype_tscripttype (ivtst_tscriptt_type_id)
using btree in idxdbs1;

alter table int_vegatype_tscripttype add constraint 
    (foreign key (ivtst_tscriptt_type_id) references 
transcript_type  constraint ivtst_tscriptt_type_id_foreign_key);

alter table feature_type_mutagen_group_member
 drop constraint ftmgm_feature_type_foreign_key_odc;

create index ftmgm_feature_type_fk_odc_index
 on feature_type_mutagen_group_member (tmgm_feature_type)
using btree in idxdbs1;

alter table feature_type_mutagen_group_member add 
    constraint (foreign key (ftmgm_feature_type) references 
    feature_type  on delete cascade constraint ftmgm_feature_type_foreign_key_odc);
    
alter table feature_type_mutagen_group_member
 drop constraint ftmgm_mutagen_foreign_key_odc;


create index ftmgm_mutagen_fk_odc_index
 on feature_type_mutagen_group_member (tmgm_mutagen)
using btree in idxdbs1;

alter table feature_type_mutagen_group_member add 
    constraint (foreign key (ftmgm_mutagen) references 
    mutagen  on delete cascade constraint ftmgm_mutagen_foreign_key_odc);
    

alter table feature_transcript_mutation_detail
  drop constraint ftmd_feature_zdb_id_fk_odc;

create index ftmd_feature_zdb_id_fk_odc_index
 on feature_transcript_mutation_detail (ftmd_feature_zdb_id)
using btree in idxdbs3;


alter table feature_transcript_mutation_detail add 
    constraint (foreign key (ftmd_feature_zdb_id) references 
    feature  on delete cascade constraint 
    ftmd_feature_zdb_id_fk_odc);

drop index dalias_alias_lower_index;

create index dalias_alias_lower_index on 
    data_alias (dalias_alias_lower) using 
    btree in idxdbs3 ;

drop index experiment_condition_restricted_condition_data_type_foreign_key_index;

create index ecr__condition_data_type_fk_index 
    on experiment_condition_restricted (ecr_cdt_zdb_id) 
    using btree in idxdbs2;


drop index feature_mutation_detail_protein_consequenc_term_zdb_id_foreign_key_index;

create index fpmd_protein_consequence_term_zdb_id_fk_index 
    on feature_protein_mutation_detail (fpmd_protein_consequence_term_zdb_id) 
    using btree  in idxdbs3;


drop index lpmem_linkage_member_index

create index lpmem_linkage_member_index on 
    linkage_pair_member (lpmem_linkage_zdb_id) 
    using btree in idxdbs2;

create index lpmem_linkage_member_index on 
    linkage_pair_member (lpmem_member_zdb_id) 
    using btree in idxdbs1;

alter table linkage_pair_member
 drop constraint lpmem_linkage_member_foreign_key;

alter table linkage_pair_member add constraint (foreign 
    key (lpmem_linkage_zdb_id,lpmem_member_zdb_id) references 
    linkage_member  constraint lpmem_linkage_member_foreign_key);

drop index paneled_markers_target_abbrev_etc_index_a2 ;

create index paneled_markers_target_abbrev_etc_index_a2 
    on paneled_markers (target_abbrev) 
    using btree in idxdbs2;

create index paneled_markers_or_lg_index
    on paneled_markers (or_lg) 
    using btree in idxdbs1;

create index paneled_markers_mtype_index
    on paneled_markers (mtype) 
    using btree in idxdbs3;

create index paneled_markers_zdb_id_index 
    on paneled_markers (zdb_id) 
    using btree in idxdbs1;

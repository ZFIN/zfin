--liquibase formatted sql
--changeset sierra:renameIndexes

alter table blast_database
 drop constraint bdot_origination_id_foreign_key;

create index bdot_origination_id_foreign_key_index 
  on blast_database (blastdb_origination_id)
  using btree in idxdbs2;

alter table blast_database add constraint (foreign 
    key (blastdb_origination_id) references blast_database_origination_type 
     constraint bdot_origination_id_foreign_key);

alter table blast_database
 drop constraint blast_database_type_foreign_key;

create index blast_database_type_foreign_key_index
  on blast_database (blastdb_type)
  using btree in idxdbs2;

alter table blast_database add constraint (foreign 
    key (blastdb_type) references blast_database_type 
     constraint blast_database_type_foreign_key);


alter table blast_hit
 drop constraint bhit_bqry_zdb_id_foreign_keyn_odc;

create index bhit_bqry_zdb_id_fk_odc_index
  on blast_hit(bhit_bqry_zdb_id)
 using btree in idxdbs3;

alter table blast_hit add constraint (foreign key 
    (bhit_bqry_zdb_id) references blast_query  on delete 
    cascade constraint bhit_bqry_zdb_id_foreign_keyn_odc);

alter table blast_query
  drop constraint bqry_runcan_zdb_id_foreign_key_odc;

create index bqry_runcan_zdb_id_foreign_key_odc_index
  on blast_query (bqry_runcan_zdb_id)
 using btree in idxdbs3;

alter table blast_query add constraint (foreign key 
    (bqry_runcan_zdb_id) references run_candidate  
    on delete cascade constraint bqry_runcan_zdb_id_foreign_key_odc);


alter table blastdb_regen_content 
 drop constraint blastdb_regen_content_alternate_key ;

alter table blastdb_regen_content
  drop constraint blastdb_regen_content_blastdb_zdb_id_foreign_key;

create index blastdb_regen_content_blastdb_zdb_id_fk_index
 on blastdb_regen_content ( brc_blastdb_zdb_id)
 using btree in idxdbs3;

alter table blastdb_regen_content add constraint unique(brc_acc_num,
    brc_blastdb_zdb_id) constraint blastdb_regen_content_alternate_key ;

alter table blastdb_regen_content add constraint (foreign 
    key (brc_blastdb_zdb_id) references blast_database 
     constraint blastdb_regen_content_blastdb_zdb_id_foreign_key);

alter table company
  drop constraint company_contact_person_foreign_key;

create index company_contact_person_fk_index
 on company (contact_person)
 using btree in idxdbs1;


alter table company 
  drop constraint company_owner_foreign_key;

create index company_owner_fk_index
  on company (owner)
using btree in idxdbs3;


alter table company add constraint (foreign key (owner) 
    references person  constraint company_owner_foreign_key);

alter table company add constraint (foreign key (contact_person) 
    references person  constraint company_contact_person_foreign_key);

create index cc_construct_zdb_id_fk_index
 on construct_component(cc_construct_zdb_id)
 using btree in idxdbs3;

delete from construct_component
 where not exists (select 'x' from construct where cc_construcT_zdb_id = construct_zdb_id);

alter table construct_component
  add constraint (foreign key (cc_construct_zdb_id)
references construct on delete cascade constraint cc_construct_zdb_id_odc);

alter table construct
 drop constraint construct_owner_zdb_id;

create index construct_owner_zdb_id_fk_index
 on construct (construct_owner_zdb_id)
 using btree in idxdbs2;

alter table construct add constraint (foreign key 
    (construct_owner_zdb_id) references person  constraint 
    construct_owner_zdb_id);

alter table data_alias
  drop constraint dalias_scope_id_fk;

create index dalias_scope_id_fk_index
 on data_alias (dalias_scope_id) 
using btree in idxdbs1;

alter table data_alias add constraint (foreign key 
    (dalias_scope_id) references alias_scope  constraint 
    dalias_scope_id_fk);

alter table entrez_to_protein
  drop constraint ep_entrez_acc_num_foreign_key_odc;

create index ep_entrez_acc_num_foreign_key_odc_index
 on entrez_to_protein (ep_entrez_acc_num)
 using btree in idxdbs1;


alter table entrez_to_protein add constraint (foreign 
    key (ep_entrez_acc_num) references entrez_gene 
     on delete cascade constraint ep_entrez_acc_num_foreign_key_odc);

alter table entrez_to_xref 
 drop constraint ex_entrez_acc_num_foreign_key_odc;

create index ex_entrez_acc_num_foreign_key_odc_index
 on entrez_to_xref  (ex_entrez_acc_num)
 using btree in idxdbs1;

alter table entrez_to_xref add constraint (foreign 
    key (ex_entrez_acc_num) references entrez_gene 
     on delete cascade constraint ex_entrez_acc_num_foreign_key_odc);

alter table int_person_company
 drop constraint int_person_company_position_foreign_key;

create index int_person_company_position_fk_index
 on int_person_company (position_id)
using btree in idxdbs3;

alter table int_person_company add constraint (foreign 
    key (position_id) references company_position  
    constraint int_person_company_position_foreign_key);

alter table lab 
  drop constraint lab_owner_foreign_key;

create index lab_owner_fk_index 
 on lab (owner)
 using btree in idxdbs2;
    
alter table lab add constraint (foreign key (owner) 
    references person  constraint lab_owner_foreign_key);

alter table linkage_membership
 drop constraint lnkgm_member_2_zdb_id_foreign_key;

create index lnkgm_member_2_zdb_id_fk_index
 on linkage_membership (lnkgm_member_2_zdb_id)
 using btree in idxdbs1;

alter table linkage_membership add constraint (foreign 
    key (lnkgm_member_2_zdb_id) references zdb_active_data 
     constraint lnkgm_member_2_zdb_id_foreign_key);


alter table linkage_membership_search
 drop constraint lms_member_2_zdb_id_fk_zdb_active_data;

alter table linkage_membership_search
 drop constraint lms_member_1_zdb_id_fk_zdb_active_data;

create index lms_member_1_zdb_id_fk_odc
 on linkage_membership_search (lms_member_1_zdb_id)
 using btree in idxdbs1;

create index lms_member_2_zdb_id_fk_odc
 on linkage_membership_search (lms_member_2_zdb_id)
 using btree in idxdbs2;

alter table linkage_membership_search add constraint 
    (foreign key (lms_member_2_zdb_id) references zdb_active_data 
     on delete cascade constraint lms_member_2_zdb_id_fk_zdb_active_data);
    
alter table linkage_membership_search add constraint 
    (foreign key (lms_member_1_zdb_id) references zdb_active_data 
     on delete cascade constraint lms_member_1_zdb_id_fk_zdb_active_data);


alter table mapped_marker
 drop constraint owner_person_zdb_id_foreign_key_odc;

create index owner_person_zdb_id_fk_odc_index
 on mapped_marker(owner)
using btree in idxdbs1;

alter table mapped_marker add constraint (foreign 
    key (owner) references person  
    constraint owner_person_zdb_id_foreign_key_odc);

alter table marker_go_term_evidence
 drop constraint mrkrgoev_annotation_organization_foreign_key;

alter table marker_go_term_evidence
 drop constraint mrkrgoev_term_zdb_id_foreign_key;

create index mrkrgoev_annotation_org_fk_index
 on marker_go_term_evidence (mrkrgoev_annotation_organization)
using btree in idxdbs1;

create index mrkrgoev_term_zdb_id_fk_index
 on marker_go_term_evidence (mrkrgoev_term_zdb_id)
using btree in idxdbs2;

alter table marker_go_term_evidence add constraint 
    (foreign key (mrkrgoev_annotation_organization) references 
    marker_go_term_evidence_annotation_organization 
     constraint mrkrgoev_annotation_organization_foreign_key);

alter table marker_go_term_evidence add constraint 
    (foreign key (mrkrgoev_term_zdb_id) references term 
     on delete cascade constraint mrkrgoev_term_zdb_id_foreign_key);

alter table marker_type_group_member
 drop constraint mtgrpmem_mrkr_type_fk;

create index mtgrpmem_mrkr_type_fk_index
 on marker_type_group_member (mtgrpmem_mrkr_type)
using btree in idxdbs3;

alter table marker_type_group_member add constraint 
    (foreign key (mtgrpmem_mrkr_type) references marker_types 
     constraint mtgrpmem_mrkr_type_fk);

alter table phenotype_statement
 drop constraint phenos_quality_foreign_key;

create index phenos_quality_fk_index
 on phenotype_statement(phenos_quality_zdb_id)
 using btree in idxdbs3;

alter table phenotype_statement add constraint (foreign 
    key (phenos_quality_zdb_id) references term  constraint 
    phenos_quality_foreign_key);


alter table pub_tracking_history
 drop constraint pth_claimed_by_foreign_key;

create index pth_claimed_by_fk_index
 on pub_tracking_history (pth_claimed_by)
using btree in idxdbs1;


alter table pub_tracking_history add constraint (foreign 
    key (pth_claimed_by) references person  constraint 
    pth_claimed_by_foreign_key);

alter table so_zfin_mapping
 drop constraint so_zfin_mapping_term_foreign_key;

create index so_zfin_mapping_term_fk_index
 on so_zfin_mapping(szm_term_ont_id)
using btree in idxdbs2;

alter table so_zfin_mapping add constraint (foreign 
    key (szm_term_ont_id) references term (term_ont_id) 
    on delete cascade constraint so_zfin_mapping_term_foreign_key);

alter table transcript
 drop constraint tscript_status_foreign_key;

create index tscript_status_fk_index
on transcript (tscript_status_id)
 using btree in idxdbs2;

alter table transcript add constraint (foreign key 
    (tscript_status_id) references transcript_status 
     constraint tscript_status_foreign_key);


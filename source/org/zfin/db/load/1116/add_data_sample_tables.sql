--liquibase formatted sql
--changeset sierra:add_data_sample_tables.sql

create table htp_dataset_sample (hds_pk_id serial8 not null primary key,
				 hds_sample_id text,
				 hds_sample_title text,
			         hds_sample_type text not null,
				 hds_fish_zdb_id text,
				 hds_sex text,
				 hds_assay_type text not null,
				 hds_sequencing_format text,
				 hds_hd_zdb_id text,
				 hds_abundance text,
				 hds_assembly text,
         hds_notes text,
				 hds_stage_term_zdb_id text)
;

create unique index hds_sample_id_sample_title_index on htp_dataset_sample(hds_sample_id, hds_sample_title, hds_stage_term_zdb_id);

create index hds_fish_Zdb_id_index on htp_dataset_sample(hds_fish_zdb_id);

create index hds_hd_zdb_id_index on htp_dataset_sample(hds_hd_zdb_id);

create index hds_stage_term_zdb_id  on htp_dataset_sample(hds_stage_term_zdb_id);

alter table htp_dataset_sample
  add constraint hds_stage_term_zdb_id_fk_odc
  foreign key (hds_stage_term_zdb_id)
  references term(term_zdb_id)
 ;

alter table htp_dataset_sample
  add constraint hds_fish_zdb_id_fk_odc
  foreign key (hds_fish_zdb_id)
  references fish(fish_zdb_id)
  on delete cascade; 

alter table htp_dataset_sample
  add constraint hds_hd_zdb_id_fk_odc
  foreign key (hds_hd_zdb_id)
  references htp_dataset (hd_zdb_id)
  on delete cascade;

alter table htp_dataset_sample
  add constraint hds_alternate_key unique (hds_sample_title, hds_sample_id, hds_stage_term_zdb_id);

create table htp_dataset_sample_detail(hdsd_pk_id serial8 not null primary key,
					hdsd_hds_id int8 not null,
					hdsd_anatomy_super_term_zdb_id text not null,
					hdsd_anatomy_sub_term_zdb_id text,
					hdsd_anatomy_sub_term_qualifier_zdb_id text,
					hdsd_anatomy_super_term_qualifier_zdb_id text,
					hdsd_cellular_component_term_zdb_id text,
					hdsd_cellular_component_term_qualifier_zdb_id text);

create unique index hdsd_hds_id_ak_index on htp_dataset_sample_detail (hdsd_hds_id, hdsd_anatomy_super_term_zdb_id,
									hdsd_anatomy_sub_term_Zdb_id, hdsd_anatomy_super_term_qualifier_zdb_id,
									hdsd_anatomy_sub_term_qualifier_zdb_id, hdsd_cellular_component_term_Zdb_id,
									hdsd_cellular_component_term_qualifier_zdb_id);

create index hdsd_anatomy_super_term_zdb_id_fk_index on htp_dataset_sample_detail (hdsd_anatomy_super_term_zdb_id) ;
create index hdsd_anatomy_sub_term_zdb_id_fk_index on htp_dataset_sample_detail (hdsd_anatomy_sub_term_zdb_id) ;
create index hdsd_anatomy_super_term_qualifier_zdb_id_fk_index on htp_dataset_sample_detail (hdsd_anatomy_super_term_qualifier_zdb_id) ;
create index hdsd_anatomy_sub_term_qualifier_zdb_id_fk_index on htp_dataset_sample_detail (hdsd_anatomy_sub_term_qualifier_zdb_id) ;
create index hdsd_anatomy_cellular_component_term_zdb_id_fk_index on htp_dataset_sample_detail (hdsd_cellular_component_term_zdb_id) ;
create index hdsd_anatomy_cellular_component_term_qualifier_zdb_id_fk_index on htp_dataset_sample_detail (hdsd_cellular_component_term_qualifier_zdb_id) ;

alter table htp_dataset_sample_detail
 add constraint	hdsd_anatomy_super_term_zdb_id_fk    
 foreign key (hdsd_anatomy_super_term_zdb_id)
 references term (term_zdb_id); 

alter table htp_dataset_sample_detail
 add constraint hdsd_anatomy_sub_term_zdb_id_fk
 foreign key (hdsd_anatomy_sub_term_zdb_id)
 references term (term_zdb_id);

alter table htp_dataset_sample_detail
 add constraint hdsd_anatomy_super_term_qualifier_zdb_id_fk
 foreign key (hdsd_anatomy_super_term_qualifier_zdb_id)
 references term (term_zdb_id);

alter table htp_dataset_sample_detail
 add constraint hdsd_anatomy_sub_term_qualifier_zdb_id_fk
 foreign key (hdsd_anatomy_sub_term_qualifier_zdb_id)
 references term (term_zdb_id);

alter table htp_dataset_sample_detail
 add constraint hdsd_cellular_component_term_zdb_id_fk
 foreign key (hdsd_cellular_component_term_zdb_id)
 references term (term_zdb_id);

alter table htp_dataset_sample_detail
 add constraint hdsd_cellular_component_qualifier_term_zdb_id_fk
 foreign key (hdsd_cellular_component_term_qualifier_zdb_id)
 references term (term_zdb_id);

alter table htp_dataset_sample_detail
  add constraint hdsd_alternate_key unique (hdsd_hds_id, hdsd_anatomy_super_term_zdb_id,
                                                                        hdsd_anatomy_sub_term_Zdb_id, hdsd_anatomy_super_term_qualifier_zdb_id,
                                                                        hdsd_anatomy_sub_term_qualifier_zdb_id, hdsd_cellular_component_term_Zdb_id,
                                                                        hdsd_cellular_component_term_qualifier_zdb_id);  		 
		




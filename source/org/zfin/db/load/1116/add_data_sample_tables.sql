--liquibase formatted sql
--changeset sierra:add_data_sample_tables.sql

create table htp_dataset_sample (hds_pk_id serial8 not null primary key,
				 hds_sample_id text,
				 hds_sample_title text,
			         hds_sample_type text,
				 hds_fish_zdb_id text,
				 hds_sex text,
				 hds_assay_type text,
				 hds_sequencing_format text,
				 hds_hd_zdb_id text)
;

create unique index hds_sample_id_sample_title_index on htp_dataset_sample(hds_sample_id, hds_sample_title);

create index hds_fish_Zdb_id_index on htp_dataset_sample(hds_fish_zdb_id);

create index hds_hd_zdb_id_index on htp_dataset_sample(hds_hd_zdb_id);

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
  add constraint hds_alternate_key unique (hds_sample_title, hds_sample_id);  

create table htp_dataset_sample_stage(hdss_hds_pk_id serial8 not null primary key,
					hdss_hds_id int8 not null,
					hdss_stage_term_zdb_id text not null,
					hdss_anatomy_term_zdb_id text not null);

create unique index hdss_hds_id_ak_index on htp_dataset_sample_stage (hdss_hds_id, hdss_stage_term_zdb_id, hdss_anatomy_term_zdb_id);

create index hdss_stage_term_zdb_id_fk_index on htp_dataset_sample_stage (hdss_stage_term_zdb_id) ;

create index hdss_anatomy_term_zdb_id_fk_index on htp_dataset_sample_stage (hdss_anatomy_term_zdb_id) ;

alter table htp_dataset_sample_stage
 add constraint hdss_stage_term_zdb_id_fk
 foreign key (hdss_stage_term_zdb_id)
 references term (term_zdb_id);

alter table htp_dataset_sample_stage
 add constraint	hdss_anatomy_term_zdb_id_fk    
 foreign key (hdss_anatomy_term_zdb_id)
 references term (term_zdb_id); 

alter table htp_dataset_sample_stage
  add constraint hdss_alternate_key unique (hdss_hds_id, hdss_stage_term_Zdb_id, hdss_anatomy_term_zdb_id);  		 
		




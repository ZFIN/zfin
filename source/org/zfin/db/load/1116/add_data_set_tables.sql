--liquibase formatted sql
--changeset sierra:add_data_set_table

create table htp_category_tag (hct_zdb_id text not null primary key,
			       hct_category_tag text not null)
;

create unique index hct_category_tag_unique_index on htp_Category_tag(hct_category_tag);

alter table htp_category_tag
  add constraint hct_category_tag_fk
  unique (hct_category_tag);

create table htp_dataset(hd_zdb_id text not null primary key,
			 hd_geo_id text,
			 hd_arrayexpress_id text,
		         hd_sra_id text,
			 hd_daniocode_id text,
			 hd_title text,
                         hd_summary text,
	                 hd_date_curated timestamp not null default now(),
			 hd_subseries_id text);

create unique index hd_ak_index on htp_dataset (hd_geo_id, hd_arrayexpress_id, hd_sra_id, hd_daniocode_id);

alter table htp_dataset
 add constraint hd_id_unique unique (hd_geo_id, hd_arrayexpress_id, hd_sra_id, hd_daniocode_id);


create table htp_dataset_publication(hdp_pk_id serial8 not null primary key,
				     hdp_dataset_zdb_id text not null,
				     hdp_pub_zdb_id text not null
				);

create unique index hdp_ak_index on htp_dataset_publication (hdp_dataset_zdb_id, hdp_pub_zdb_id);

alter table htp_dataset_publication
 add constraint hdp_dataset_pub_unique unique (hdp_dataset_zdb_id, hdp_pub_zdb_id); 

create index hdp_pub_zdb_id_fk_index on htp_dataset_publication (hdp_pub_zdb_id);

create index hdp_dataset_Zdb_id_fk_index on htp_dataset_publication (hdp_dataset_zdb_id);

alter table htp_dataset_publication
  add constraint hdp_pub_zdb_id_fk
  foreign key (hdp_pub_zdb_id)
  references publication(zdb_id) on delete cascade;

alter table htp_dataset_publication
  add constraint hdp_dataset_zdb_id_fk
  foreign key (hdp_dataset_zdb_id)
  references htp_dataset(hd_zdb_id) on delete cascade;


create table htp_dataset_category_tag(hdct_pk_id serial8 not null primary key,
					hdct_dataset_zdb_id text not null,
					hdct_category_tag text not null
);

create unique index hdct_ak_index on htp_dataset_category_tag (hdct_dataset_zdb_id, hdct_category_tag);

alter table htp_dataset_category_tag add constraint htp_dataset_category_unique unique (hdct_dataset_zdb_id, hdct_category_tag);

create index hdct_dataset_Zdb_id_fk_index on htp_dataset_category_tag (hdct_dataset_zdb_id);

create index hdct_dataset_category_tag_fk_index on htp_dataset_category_tag (hdct_category_tag);

alter table htp_dataset_category_tag 
  add constraint hdct_dataset_zdb_id_fk
 foreign key (hdct_dataset_zdb_id)
 references htp_dataset (hd_zdb_id);

alter table htp_dataset_category_tag
  add constraint hdct_category_tag_fk
  foreign key (hdct_category_tag)
 references htp_category_tag (hct_zdb_id);


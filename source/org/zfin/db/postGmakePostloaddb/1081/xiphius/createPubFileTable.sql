--liquibase formatted sql
--changeset sierra:createPubFileTable

create table publication_file (pf_pub_zdb_id varchar(50) not null constraint pf_pub_zdb_id_not_null,
       	     		       pf_file_name varchar(255) not null constraint pf_file_name_not_null,
			       pf_file_type_id int8 not null constraint pf_file_type_not_null,
			       pf_date_entered datetime year to second default current year to second not null constraint pf_date_entered_not_null,
			       pf_original_file_name varchar(255))
in tbldbs2
extent size 8192 next size 8192;

create unique index publication_file_primary_key  on publication_file (pf_pub_zdb_id, pf_file_name, pf_file_type_id)
 using btree in idxdbs2;

alter table publication_file
 add constraint unique (pf_pub_zdb_id, pf_file_name, pf_file_type_id)
 constraint publication_file_primary_key ;

create index pf_file_type_index
  on publication_file (pf_file_type_id)
 using btree in idxdbs3;

create table publication_file_type (pft_pk_id serial8 not null constraint pft_pk_id_not_null,
       	     			   pft_type varchar(100) not null constraint pft_type_not_null, 
				   pft_type_order int8 not null constraint pft_type_order_not_null)
in tbldbs1
extent size 8 next size 8;

create unique index pft_primary_key_index 
  on publication_file_type (pft_pk_id)
 using btree in idxdbs3;

create unique index pft_alternate_key_index
 on publication_file_type (pft_type)
 using btree in idxdbs1;  

alter table publication_file_type
  add constraint primary key (pft_pk_id)
 constraint publication_file_type_primary_key;

alter table publication_file_type
 add constraint unique (pft_type)
 constraint pft_file_type_alternate_key;

alter table publication_file
 add constraint (foreign key (pf_file_type_id)
 references publication_file_type constraint
 pf_file_type_id_foreign_key);


insert into publication_file_type (pft_type, pft_type_order) 
 values ('Original Article','1');
insert into publication_file_type (pft_type, pft_type_order)
 values ('Annotated Article','2');
insert into publication_file_type (pft_type, pft_type_order) 
 values ('Supplemental Material','3');
insert into publication_file_type (pft_type, pft_type_order) 
 values ('Correspondence Details','4');
insert into publication_file_type (pft_type, pft_type_order) 
 values ('Other','5');

insert into publication_file (pf_pub_zdb_id, 
       	    		     		     pf_file_name,
					     pf_original_file_name,
					     pf_file_type_id)
  select zdb_id, substr(get_date_from_id(zdb_id,'YYYYMMDD'),1,4)||"/"||pub_file,pub_file, 1
    from publication
 where pub_file is not null;

alter table publication
  drop pub_file;


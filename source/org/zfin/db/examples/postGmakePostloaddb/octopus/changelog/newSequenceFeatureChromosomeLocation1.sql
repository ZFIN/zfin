--liquibase formatted sql
create table sequence_feature_chromosome_location (sfcl_zdb_id varchar(50) not null constraint sfcl_zdb_id_not_null,
       sfcl_feature_zdb_id varchar(50) not null constraint sfcl_feature_zdb_id_not_null,
       sfcl_start_position integer,
       sfcl_end_position integer,
       sfcl_assembly varchar(10),
       sfcl_chromosome varchar(2) not null constraint sfcl_chromosome_not_null,
       check ((sfcl_start_position is not null and sfcl_end_position is not null and 
       	      sfcl_assembly is not null) or (sfcl_start_position is null and sfcl_end_position is null and sfcl_assembly is null )) constraint sequence_feature_chromosome_location_check_constraint)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 4096 next size 4096
lock mode row;

create unique index sfcl_zdb_id_primary_key_index
  on sequence_feature_chromosome_location (sfcl_zdb_id)
 using btree in idxdbs2;

create unique index sfcl_alternate_key_index
 on sequence_feature_chromosome_location (sfcl_feature_zdb_id, sfcl_start_position, sfcl_end_position, sfcl_chromosome, sfcl_assembly)
using btree in idxdbs3;

create index sfcl_feature_fk_index
  on sequence_feature_chromosome_location (sfcl_feature_zdb_id)
 using btree in idxdbs1;

create index sfcl_chromosome_fk_index
 on sequence_feature_chromosome_location (sfcl_chromosome)
 using btree in idxdbs1;

alter table sequence_feature_chromosome_location
  add constraint primary key (sfcl_zdb_id)
 constraint sfcl_primary_key;

alter table sequence_feature_chromosome_location
  add constraint unique (sfcl_feature_zdb_id, sfcl_start_position, sfcl_end_position, sfcl_chromosome, sfcl_assembly) constraint sfcl_alternate_key;
       
alter table sequence_feature_chromosome_location
 add constraint (foreign key (sfcl_feature_zdb_id)
 references feature on delete cascade constraint sfcl_feature_zdb_id_fk_odc);

alter table sequence_feature_chromosome_location
 add constraint (foreign key (sfcl_zdb_id)
 references zdb_active_data on delete cascade constraint sfcl_zdb_id_fk_odc);

alter table sequence_feature_chromosome_location
 add constraint (foreign key (sfcl_chromosome)
  references chromosome constraint sfcl_chromosome_fk_odc);
--commit work;

--begin work;
insert into zdb_object_type (zobjtype_name, 
       	    		     zobjtype_day,
			     zobjtype_app_page,
			     zobjtype_home_table,
			     zobjtype_home_zdb_id_column,
			     zobjtype_is_data,
			     zobjtype_is_source)
 values ('SFCL',current,'sequence_feature_chromosome_location','sequence_feature_chromosome_location','sfcl_zdb_id','t','f');

create sequence sfcl_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;
alter sequence sfcl_seq restart with 1;


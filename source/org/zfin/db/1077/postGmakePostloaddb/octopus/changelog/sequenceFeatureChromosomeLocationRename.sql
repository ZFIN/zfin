
create table sequence_feature_chromosome_location_generated
  (
    sfclg_chromosome varchar(20) not null constraint sfclg_chromosome_not_null,
    sfclg_data_zdb_id varchar(50),
    sfclg_pk_id serial8 not null constraint sfclg_pk_id_not_null,
    sfclg_acc_num varchar(30),
    sfclg_start integer,
    sfclg_end integer,
    sfclg_location_source varchar(40),
    sfclg_location_subsource varchar(100),
    sfclg_fdb_db_id int8
  ) 
  fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3 
  extent size 16384 next size 16384 lock mode row;

create index sfclg_chromosome_index on sequence_feature_chromosome_location_generated 
    (sfclg_chromosome) using btree  in idxdbs2;
create index sfclg_mrkr_index on sequence_feature_chromosome_location_generated  
    (sfclg_data_zdb_id) using btree  in idxdbs3;
create unique index sfclg_pk_index on sequence_feature_chromosome_location_generated   
    (sfclg_data_zdb_id,sfclg_chromosome,sfclg_location_source,sfclg_location_subsource,
    sfclg_start,sfclg_end,sfclg_acc_num) using btree  in idxdbs3;

insert into sequence_feature_chromosome_location_generated
 select * from sequence_feature_chromosome_location;

alter table sequence_feature_Chromosome_location_generated
 add (sfclg_pub_zdb_id varchar(50));


drop table sequence_feature_chromosome_location;

create table sequence_feature_chromosome_location_generated_temp 
  (
    sfclg_chromosome varchar(20) not null constraint sfclgt_chromosome_not_null,
    sfclg_data_zdb_id varchar(50),
    sfclg_acc_num varchar(30),
    sfclg_start integer,
    sfclg_end integer,
    sfclg_location_source varchar(40),
    sfclg_location_subsource varchar(100),
    sfclg_fdb_db_id int8
  ) 
  fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3 
  extent size 16384 next size 16384 lock mode row;


create index sfclgt_chromosome_index on sequence_feature_chromosome_location_generated_temp 
    (sfclg_chromosome) using btree  in idxdbs2;
create index sfclgt_data_index on sequence_feature_chromosome_location_generated_temp 
    (sfclg_data_zdb_id) using btree  in idxdbs3;
create unique index sfclgt_pk_index on sequence_feature_chromosome_location_generated_temp 
    (sfclg_data_zdb_id,sfclg_chromosome,sfclg_location_source,sfclg_location_subsource,
    sfclg_start,sfclg_end) using btree  in idxdbs3;

insert into sequence_feature_chromosome_location_generated_temp
 select * from sequence_feature_chromosome_location_temp;


create table sequence_feature_chromosome_location_generated_bkup 
  (
    sfclg_chromosome varchar(20) not null constraint sfclgb_chromosome_not_null,
    sfclg_data_zdb_id varchar(50),
    sfclg_acc_num varchar(30),
    sfclg_start integer,
    sfclg_end integer,
    sfclg_location_source varchar(40),
    sfclg_location_subsource varchar(100),
    sfclg_fdb_db_id int8
  ) 
  fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3 
  extent size 16384 next size 16384 lock mode row;


create index sfclgb_chromosome_index on sequence_feature_chromosome_location_generated_bkup 
    (sfclg_chromosome) using btree  in idxdbs2;
create index sfclgb_data_index on sequence_feature_chromosome_location_generated_bkup 
    (sfclg_data_zdb_id) using btree  in idxdbs3;
create unique index sfclgb_pk_index on sequence_feature_chromosome_location_generated_bkup 
    (sfclg_data_zdb_id,sfclg_chromosome,sfclg_location_source,sfclg_location_subsource,
    sfclg_start,sfclg_end) using btree  in idxdbs3;


alter table sequence_feature_chromosome_location_temp
 add (sfclg_pub_zdb_id varchar(50));

alter table sequence_feature_chromosome_location_bkup
 add (sfclg_pub_zdb_id varchar(50));

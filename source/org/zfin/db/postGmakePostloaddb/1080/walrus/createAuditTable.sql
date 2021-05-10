--liquibase formatted sql
--changeset sierra:createAuditTable

create table marker_history_audit (mha_pk_id serial8 not null constraint mha_pk_id_not_null,
       	     			 mha_mrkr_zdb_id varchar(50) not null constraint mha_mrkr_zdb_id_not_null,
				 mha_mrkr_abbrev_before varchar(100) not null constraint mha_mrkr_abbrev_before_not_null,
				 mha_mrkr_abbrev_after varchar(100) not null constraint mha_mrkr_abbrev_after_not_null,
				 mha_insert_date datetime year to second default current year to second not null constraint mha_insert_date_not_null,
				 mha_mrkr_name_before varchar(255),
				 mha_mrkr_name_after varchar(255)
				 )
in tbldbs2
extent size 4096 next size 4096 lock mode row;


create unique index mha_primary_key_index 
    on marker_history_audit (mha_pk_id) using btree  in 
    idxdbs2;

create index mha_mrkr_zdb_id_index on marker_history_audit 
    (mha_mrkr_zdb_id) using btree  in idxdbs2;

alter table marker_history_audit add constraint primary 
    key (mha_pk_id) constraint mha_primary_key 
     ;

alter table marker_history_audit add constraint (foreign 
    key (mha_mrkr_zdb_id) references marker  on delete 
    cascade constraint mha_mrkr_zdb_id_foreign_key);
  
insert into marker_history_audit (mha_mrkr_zdb_id, mha_mrkr_abbrev_before, mha_mrkr_abbrev_after, mha_mrkr_name_before, mha_mrkr_name_after, mha_insert_date)
       select mhist_mrkr_zdb_id, mhist_mrkr_abbrev_on_mhist_date, mhist_mrkr_abbrev_on_mhist_date, mhist_mrkr_prev_name, mhist_mrkr_prev_name, mhist_date
         from marker_history;


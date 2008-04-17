begin work ;

delete from marker_type_group_member
 where mtgrpmem_mrkr_type = 'SNP'
 and mtgrpmem_mrkr_type_group = 'SEARCH_MKSEG';

create table snp_download(
        snpd_pk_id serial not null constraint 
		   	  snpd_pk_id_not_null,
        snpd_rs_acc_num varchar(40) not null constraint 
			snpd_rs_acc_num_not_null,
	snpd_mrkr_zdb_id varchar(50) not null constraint 
			 snpd_mrkr_zdb_id_not_null,
	snpd_comment lvarchar
        )
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 32768 next size 32768
lock mode page;

create unique index snp_download_primary_key_index
 on snp_download (snpd_pk_id)
  using btree in idxdbs3 ;

create index snpd_mrkr_zdb_id_index
  on snp_download (snpd_mrkr_zdb_id)
  using btree in idxdbs2;

create unique index snpd_rs_mrkr_alternate_key_index
 on snp_download (snpd_rs_acc_num, snpd_mrkr_zdb_id)
 using btree in idxdbs1;

alter table snp_download  
  add constraint primary key (snpd_pk_id)
  constraint snp_download_primary_key ;

alter table snp_download
  add constraint unique (snpd_rs_acc_num, snpd_mrkr_zdb_id)
  constraint snpd_alternate_key ;

alter table snp_download 
  add constraint (foreign key (snpd_mrkr_zdb_id)
  references marker on delete cascade constraint
  snpd_mrkr_zdb_id_foreign_key_odc);


create table snp_download_attribution(
        snpdattr_snpd_pk_id int not null constraint 
		   	  snpdattr_snpd_pk_id_not_null,
        snpdattr_pub_zdb_id varchar(50) not null constraint
			  snpdattr_pub_zdb_id_not_null
        )
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 32768 next size 32768
lock mode page;

create unique index snpdattr_primary_key_index
  on snp_download_attribution (snpdattr_snpd_pk_id, snpdattr_pub_zdb_id)
  using btree in idxdbs1 ;

alter table snp_download_attribution
  add constraint primary key (snpdattr_snpd_pk_id, snpdattr_pub_zdb_id)
  constraint snpd_download_attribution_primary_key ;


--rollback work ;

commit work ;
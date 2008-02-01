begin work ;

--looks like ensemble number and dbsnp number are both the same, the 
--rs number; so I just include the rs number here.

create table snp_download(
        snpd_pk_id serial not null constraint snpd_pk_id_not_null,
        snpd_name varchar(255) not null constraint snpd_name_not_null,
        snpd_abbrev varchar(40) not null constraint snpd_abbrev_not_null,
        snpd_mrkr_zdb_id varchar(50) not null constraint
                snpd_mrkr_zdb_id_not_null, --clone/gene/est id
        snpd_sequence lvarchar,
        snpd_offset int,
        snpd_type varchar(10) not null constraint snpd_type_not_null,
        snpd_rs_acc_num varchar(40),
        snpd_ss_acc_num varchar(40) not null constraint
                snpd_ss_acc_num_not_null,
        snpd_pub_zdb_id varchar(50) not null constraint
                snpd_pub_zdb_id_not_null,
		 --Jeff Smith attribution needs to be a
		--default here at some point.
	snpd_chromosome_number varchar(4),
	snpd_variation varchar(20),
	snpd_left_end varchar(3),
	snpd_seq_type varchar(20)
)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 32768 next size 32768
lock mode page;

create unique index snp_download_primary_key_index
 on snp_download (snpd_pk_id)
  using btree in idxdbs3 ;

create index snpd_mrkr_zdb_id_index
  on snp_download(snpd_mrkr_zdb_id)
  using btree in idxdbs2;

create unique index snp_download_alternate_key_index
  on snp_download (snpd_ss_acc_num) 
  using btree in idxdbs2 ;

create unique index snpd_name_alternate_key_index
 on snp_download (snpd_name,snpd_ss_acc_num, snpd_abbrev)
 using btree in idxdbs1;

--these two constraints allow us to mimic marker for the
--time when snps go into marker, or equivalent table.

alter table snp_download
  add constraint unique (snpd_ss_acc_num)
  constraint snpd_alternate_ss_acc_num_key ;

alter table snp_download
  add constraint unique (snpd_name, snpd_abbrev, snpd_ss_acc_num)
  constraint snpd_alternate_key ;

alter table snp_download  
  add constraint primary key (snpd_pk_id)
  constraint snp_download_primary_key ;

alter table snp_download 
  add constraint (foreign key (snpd_mrkr_Zdb_id)
  references marker on delete cascade constraint
  snpd_mrkr_zdb_id_foreign_key_odc);

create index snpd_variation_index 
 on snp_download (snpd_variation)
 using btree in idxdbs3 ;
 
alter table snp_download
  add constraint (foreign key (snpd_variation)
  references sequence_ambiguity_code constraint 
  snpd_variation_foreign_key) ;
  


rollback work ;

--commit work ;
begin work ;

set constraints all deferred ;

update foreign_db_contains
  set fdbcont_fdb_db_name = 'dbSNP'
  where fdbcont_fdb_db_name = 'SNP' ;

update foreign_db
  set fdb_db_name = 'dbSNP'
  where fdb_db_name = 'SNP' ;

insert into foreign_db (fdb_db_name, fdb_db_query, fdb_url_suffix,
				fdb_db_significance)
  values ('SNPBLAST', 
	  'http://www.ncbi.nlm.nih.gov/SNP/snp_blastByOrg.cgi',
	   null,
	  '8');
	  
insert into foreign_db_contains (fdbcont_zdb_id, 
				 fdbcont_fdbdt_data_type,
				 fdbcont_fdb_db_name,
			 	 fdbcont_organism_common_name,
				 fdbcont_fdbdt_super_type)
  values (get_id('FDBCONT'),
	   'Genomic',
	   'SNPBLAST',
	   'Zebrafish',
	   'sequence');

insert into marker_type_group_member (mtgrpmem_mrkr_type,
	mtgrpmem_mrkr_type_group)
  values ('SNP','POLYMORPH');

insert into marker_type_group_member (mtgrpmem_mrkr_type,
	mtgrpmem_mrkr_type_group)
  values ('SNP','SEARCH_MKSEG');

insert into marker_type_group_member (mtgrpmem_mrkr_type,
	mtgrpmem_mrkr_type_group)
  values ('SSLP','POLYMORPH');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
  values ('POLYMORPH','Group containing SNPs and SSLPs and INDELs');

update marker_types
  set mrkrtype_significance = '16'
  where marker_type = 'SNP' ;

insert into marker_relationship_type (mreltype_name,
	mreltype_mrkr_type_group_1, 
	mreltype_mrkr_type_group_2,
	mreltype_1_to_2_comments,
	mreltype_2_to_1_comments)
  values ('clone contains polymorphism', 
		'CLONE',
		'POLYMORPH',
		'Contains',
		'Contained in');

insert into marker_relationship_type (mreltype_name,
	mreltype_mrkr_type_group_1, 
	mreltype_mrkr_type_group_2,
	mreltype_1_to_2_comments,
	mreltype_2_to_1_comments)
  values ('gene contains polymorphism', 
		'GENE',
		'POLYMORPH',
		'Contains',
		'Contained in');


insert into zdb_object_type (zobjtype_name,
	zobjtype_day,
	zobjtype_home_table,
	zobjtype_home_zdb_id_column,
	zobjtype_is_data,
	zobjtype_is_source)
  values ('INDEL', '11/07/2005','marker','mrkr_zdb_id','t','f');


insert into zdb_object_type (zobjtype_name,
	zobjtype_day,
	zobjtype_home_table,
	zobjtype_home_zdb_id_column,
	zobjtype_is_data,
	zobjtype_is_source)
  values ('SNP', '11/07/2005','marker','mrkr_zdb_id','t','f');

insert into zdb_active_data
  select mrel_Zdb_id from marker_relationship
  where mrel_zdb_id not in (Select zactvd_zdb_id from zdb_active_data);


alter table marker_sequence 
  add (mrkrseq_seq_type varchar(20) default 'Genomic') ;

alter table marker_sequence
  add constraint (foreign key (mrkrseq_seq_type)
  references sequence_type
  constraint mrkrseq_seq_type_foreign_key) ;

alter table marker_sequence
  add (mrkrseq_offset_start int) ;

alter table marker_sequence
  add (mrkrseq_offset_stop int) ;
				
alter table marker_sequence
  add (mrkrseq_variation varchar(20)) ;

create table sequence_ambiguity_code (seqac_symbol char(1),
				seqac_meaning varchar(60))
in tbldbs1 
extent size 8 next size 9 lock mode row; 

insert into sequence_ambiguity_code (seqac_symbol, seqac_meaning)
  values ('M','A/C') ;

insert into sequence_ambiguity_code (seqac_symbol, seqac_meaning)
  values ('R','A/G') ;

insert into sequence_ambiguity_code (seqac_symbol, seqac_meaning)
  values ('W','A/T') ;

insert into sequence_ambiguity_code (seqac_symbol, seqac_meaning)
  values ('S','C/G') ;

insert into sequence_ambiguity_code (seqac_symbol, seqac_meaning)
  values ('Y','C/T') ;

insert into sequence_ambiguity_code (seqac_symbol, seqac_meaning)
  values ('K','G/T') ;

insert into sequence_ambiguity_code (seqac_symbol, seqac_meaning)
  values ('V','A/C/G') ;

insert into sequence_ambiguity_code (seqac_symbol, seqac_meaning)
  values ('H','A/C/T') ;

insert into sequence_ambiguity_code (seqac_symbol, seqac_meaning)
  values ('D','A/G/T') ;

insert into sequence_ambiguity_code (seqac_symbol, seqac_meaning)
  values ('B','C/G/T') ;

insert into sequence_ambiguity_code (seqac_symbol, seqac_meaning)
  values ('N','A/C/G/T') ;


set constraints all immediate;

create unique index mrkrseq_mrkr_seq_type_unique
  on marker_sequence (mrkrseq_mrkr_Zdb_id, mrkrseq_seq_type)
  using btree in idxdbs3 ;

alter table marker_sequence
  add constraint unique (mrkrseq_mrkr_zdb_id, mrkrseq_seq_type)
  constraint marker_sequence_alternate_key ;


create unique index sequence_ambiguity_code_primary_key_index
  on sequence_ambiguity_code (seqac_symbol)
  using btree in idxdbs3 ;

alter table sequence_ambiguity_code 
  add constraint primary key (seqac_symbol)
  constraint sequence_ambiguity_code_primary_key ;

--rollback work ;
commit work ;

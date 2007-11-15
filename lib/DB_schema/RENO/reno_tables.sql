begin work ;

--------------------------------------------
--ACCESSION BANK CHANGES--

alter table accession_bank
  drop constraint accession_bank_primary_key ;

drop index accession_bank_primary_key_index ;

update accession_bank
  set accbk_db_name = 'GenBank'
  where accbk_db_name = 'Genbank' ;

alter table accession_bank
  add (accbk_pk_id int) ;

alter table accession_bank
  add (accbk_fdbcont_zdb_id varchar(50));

alter table accession_bank
  add (accbk_defline lvarchar);

update accession_bank
  set accbk_fdbcont_zdb_id = (select fdbcont_zdb_id
      			        from foreign_db_contains
				where fdbcont_fdbdt_data_type = accbk_data_type
  				and fdbcont_fdb_db_name = accbk_db_name
				and fdbcont_organism_common_name = 'Zebrafish') ;

alter table accession_bank
  drop accbk_db_name ;

alter table accession_bank
  drop accbk_data_type ;

create temp table tmp_accbk (zdb_id serial, acc_num varchar(30))
with no log ;

--!echo "inserting into tmp_accbk" ;

insert into tmp_accbk (acc_num)
 select accbk_acc_num
  from accession_bank ;

create unique index acc_index
  on tmp_accbk (acc_num)
  using btree in idxdbs2;

update statistics high for table tmp_accbk;
update statistics high for table accession_bank ;

update accession_bank
  set accbk_pk_id = (select zdb_id from tmp_accbk where acc_num = accbk_acc_num);

alter table accession_bank
 modify (accbk_pk_id serial not null constraint accbk_pk_id_not_null);


create unique index accbk_primary_key_index
  on accession_bank (accbk_pk_id)
  using btree in idxdbs3 ;


create unique index accbk_alternate_key_index
  on accession_bank (accbk_acc_num, accbk_fdbcont_zdb_id)
  using btree in idxdbs4 ;

create index accbk_fdbcont_foreign_key_index
  on accession_bank (accbk_fdbcont_zdb_id)
  using btree in idxdbs2 ;

alter table accession_bank
  add constraint primary key (accbk_pk_id)
  constraint accession_bank_primary_key ;

alter table accession_bank
  add constraint unique (accbk_acc_num, accbk_fdbcont_zdb_id)
  constraint accession_bank_alternate_key ;

alter table accession_bank
  add constraint (foreign key (accbk_fdbcont_zdb_id)
  references foreign_db_contains on delete cascade constraint
  accbk_fdbcont_foreign_key_odc) ;


alter table accession_bank add (accbk_abbreviation varchar(30));

alter table accession_bank add (accbk_name varchar(255));

-----------------------------------------

create table blast_database (
    blastdb_zdb_id varchar(50) not null constraint blastdb_zdb_id_not_null,
    blastdb_name varchar(100) not null constraint blastdb_name_not_null,
    blastdb_abbrev varchar(30) not null constraint blastdb_abbrev_not_null,
    blastdb_xdget_path varchar(255),
    blastdb_description lvarchar(2500),
    blastdb_public boolean default 'f' not null constraint blastdb_public_not_null
)
in tbldbs3
extent size 8 next size 8
lock mode row;

create unique index blast_database_primary_key_index
 on blast_database(blastdb_zdb_id)
 using btree in idxdbs1 ;

create unique index blast_database_alternate_key_index
  on blast_database(blastdb_name)
  using btree in idxdbs1 ;

alter table blast_database
  add constraint primary key (blastdb_zdb_id)
  constraint blast_database_primary_key ;

alter table blast_database
  add constraint unique (blastdb_name)
  constraint blast_database_alternate_key;

insert into zdb_object_type (zobjtype_name, zobjtype_day,
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table,
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source,
	zobjtype_attribution_display_tier)
  values ('BLASTDB', CURRENT,'1','','blast_database',
	  'blastdb_zdb_id', 't','f', '2') ;


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_xdget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'Ensembl Zebrafish Transcripts',
    'ensembl_zf',
    '/research/zblastdb/db/Current/',
    'Ensembl Zebrafish Transcripts',
    't'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_xdget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'EST Human',
    'gbk_est_hs',
    '/research/zblastdb/db/Current/',
    'Subset of GenBank that contains Human sequences from EST division.',
    'f'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_xdget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'EST Mouse',
    'gbk_est_ms',
    '/research/zblastdb/db/Current/',
    'Subset of GenBank that contains Mouse sequences from EST division.',
    'f'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_xdget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'EST Zebrafish',
    'gbk_est_zf',
    '/research/zblastdb/db/Current/',
    'Subset of GenBank that contains zebrafish sequences from EST division. Zebrafish sequences are parsed from the EST divisions by examining the organism lines for entries "Danio rerio"',
    't'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_xdget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'GenBank Human',
    'gbk_gb_hs',
    '/research/zblastdb/db/Current/',
    'Subset of GenBank that contains human sequences from these divisions: vetebrate and high-throughput cDNAs (HTC). ESTs, genome survey sequences (GSS), and high-throughput genomic sequences (HTGs) are not included.',
    'f'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_xdget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'GenBank Mouse',
    'gbk_gb_ms',
    '/research/zblastdb/db/Current/',
    'Subset of GenBank that contains mouse sequences from these divisions: vetebrate and high-throughput cDNAs (HTC). ESTs, genome survey sequences (GSS), and high-throughput genomic sequences (HTGs) are not included.',
    'f'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_xdget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'GenBank Zebrafish',
    'gbk_gb_zf',
    '/research/zblastdb/db/Current/',
    'Subset of GenBank that contains zebrafish sequences from these divisions: vetebrate and high-throughput cDNAs (HTC). ESTs, genome survey sequences (GSS), and high-throughput genomic sequences (HTGs) are not included.',
    't'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_xdget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'GSS Zebrafish',
    'gbk_gss_zf',
    '/research/zblastdb/db/Current/',
    'Subset of GenBank that contains zebrafish Genome Survey Sequences (GSS). Zebrafish sequences are parsed from the GSS division by examining the organism line for entries "Danio rerio"',
    't'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_xdget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'Human DNA',
    'gbk_hs_dna',
    '/research/zblastdb/db/Current/',
    'Subset of GenBank that contains human sequences from all divisions (including EST) that are of type DNA.',
    'f'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_xdget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'Human mRNA',
    'gbk_hs_mrna',
    '/research/zblastdb/db/Current/',
    'Subset of GenBank that contains human sequences from all divisions (including EST) that are of type mRNA. Human mRNA sequences are parsed by examining the locus line for entries containing "mRNA".',
    'f'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_xdget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'HTG Zebrafish',
    'gbk_htg_zf',
    '/research/zblastdb/db/Current/',
    'Subset of GenBank that contains zebrafish High-throughput Genomic sequences (HTG). Zebrafish sequences are parsed from the HTG division by examining the organism line for entries "Danio rerio".',
    't'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_xdget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'Mouse DNA',
    'gbk_ms_dna',
    '/research/zblastdb/db/Current/',
    'Subset of GenBank that contains mouse sequences from all divisions (including EST) that are of type DNA. Zebrafish DNA sequences are parsed by examining the locus line for entries containing "DNA"',
    'f'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_xdget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'Mouse mRNA',
    'gbk_ms_mrna',
    '/research/zblastdb/db/Current/',
    'Subset of GenBank that contains mouse sequences from all divisions (including EST) that are of type mRNA. Zebrafish mRNA sequences are parsed by examining the locus line for entries containing "mRNA".',
    'f'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_xdget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'All Zebrafish',
    'gbk_zf_all',
    '/research/zblastdb/db/Current/',
    'All zebrafish sequences in GenBank?',
    't'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_xdget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'Zebrafish DNA',
    'gbk_zf_dna',
    '/research/zblastdb/db/Current/',
    'Subset of GenBank that contains zebrafish sequences from all divisions (including EST) that are of type DNA. Zebrafish DNA sequences are parsed by examining the organism lines for entries "Danio rerio" and locus line for entries containing "DNA".',
    't');


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_xdget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'Zebrafish mRNA',
    'gbk_zf_mrna',
    '/research/zblastdb/db/Current/',
    'Subset of GenBank that contains zebrafish sequences from all divisions (including EST) that are of type mRNA. Zebrafish mRNA sequences are parsed by examining the organism lines for entries "Danio rerio" and locus line for entries containing "mRNA".',
    't'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_xdget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'RefSeq Zebrafish Protein',
    'refseq_zf_aa',
    '/research/zblastdb/db/Current/',
    'NCBI RefSeq zebrafish protein sequences. (Zebrafish sequences of type: NP_, XP_).',
    't'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_xdget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'RefSeq Zebrafish mRNA',
    'refseq_zf_rna',
    '/research/zblastdb/db/Current/',
    'NCBI RefSeq Zebrafish transcripts (Zebrafish RefSeqs of type: NM_, NR, XM_).',
    't'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_xdget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'UniProt Human',
    'sptr_hs',
    '/research/zblastdb/db/Current/',
    'Human protein sequences from the "non-redundant" set of sequences from UniProt.',
    'f'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_xdget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'UniProt Mouse',
    'sptr_ms',
    '/research/zblastdb/db/Current/',
    'Mouse protein sequences from the "non-redundant" set of sequences from UniProt.',
    'f'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_xdget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'UniProt Zebrafish',
    'sptr_zf',
    '/research/zblastdb/db/Current/',
    'Zebrafish protein sequences from the "non-redundant" set of sequences from UniProt. Zebrafish sequences are defined as those entries "Danio rerio" in the species lines.',
    't'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_xdget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'TIGR Zebrafish Clusters',
    'tigr_zf',
    '/research/zblastdb/db/Current/',
    "Tentative consensus transcript sequences from TIGR's Zebrafish Gene Index (ZGI).",
    't'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_xdget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'ZFIN Vega Transcripts',
    'vega_zfin',
    '/research/zblastdb/db/Current/',
    'Vega zebrafish transcripts that are associated with ZFIN zebrafish genes.',
    't'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_xdget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'Zebrafish Trace Archive',
    'wgs_zf',
    '/research/zblastdb/db/Current/',
    'Zebrafish sequences from NCBI Trace Archive database.',
    't'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_xdget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'ZFIN cDNA Sequences',
    'zfin_cdna',
    '/research/zblastdb/db/Current/',
    'Combination of GenBank zebrafish cDNA sequences from all divisions (including EST) that are associated with ZFIN zebrafish genes or markers or clones, and Vega zebrafish transcripts that are associated with ZFIN zebrafish genes.',
    't'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_xdget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'ZFIN MicroRNA Sequences',
    'zfin_microRNA',
    '/research/zblastdb/db/Current/',
    'MicroRNA sequences in ZFIN.',
    't'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_xdget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'ZFIN Morpholino Sequences',
    'zfin_mrph',
    '/research/zblastdb/db/Current/',
    'Morpholino sequences in ZFIN.',
    't'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_xdget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'name',
    'zfin_seq',
    '/research/zblastdb/db/Current/',
    'All non protein zebrafish sequences in ZFIN',
    't'
);


-----------------------------------------



create table blastdb_run (
    blastdbr_blastdb_zdb_id varchar(50) not null constraint blastdbr_blastdb_zdb_id_not_null,
    blastdbr_run_zdb_id varchar(50) not null constraint blastdbr_run_zdb_id_not_null
)
  in tbldbs2
  extent size 32
  next size 32
  lock mode row ;

create index blastdbr_blastdb_zdb_id_foreign_key_index
  on blastdb_run(blastdbr_blastdb_zdb_id)
  using btree in idxdbs2 ;

create index blastdbr_run_Zdb_id_Foreign_key_index
  on blastdb_run(blastdbr_run_Zdb_id)
  using btree in idxdbs2 ;

alter table blastdb_run
  add constraint foreign key(blastdbr_blastdb_zdb_id) references blast_database
  constraint blastdbr_blastdb_zdb_id_foreign_key ;


-----------------------------------

create table run_type (
    runtype_name varchar(30) not null constraint runtype_name_not_null
)
 in tbldbs3 extent size 8 next size 8 ;

create unique index runtype_name_primary_key_index
  on run_type(runtype_name)
  using btree in idxdbs4 ;

alter table run_type
  add constraint primary key (runtype_name) constraint
  run_type_primary_key ;

insert into run_type(runtype_name) values ('Redundancy');
insert into run_type(runtype_name) values ('Nomenclature');

-----------------------------------

create table run_program (
    runprog_program varchar(10) not null constraint runprog_program_not_null,
    runprog_target_type varchar(10),
    runprog_query_type varchar(10)
)
 in tbldbs2 extent size 8 next size 8 ;

create unique index runprog_program_primary_key_index
  on run_program(runprog_program)
  using btree in idxdbs3 ;

alter table run_program
  add constraint primary key (runprog_program) constraint
  run_program_primary_key ;

insert into run_program values('BLASTN',  'n', 'n');
insert into run_program values('TBLASTX', 'n', 'n');
insert into run_program values('BLASTX',  'n', 'p');
insert into run_program values('TBLASTN', 'p', 'n');
insert into run_program values('BLASTP',  'p', 'p');

-----------------------------------

create table run (
    run_zdb_id varchar(50) not null constraint run_zdb_id_not_null,
    run_nomen_pub_zdb_id varchar(50) default "ZDB-PUB-030508-1" not null constraint run_nomen_pub_zdb_id_not_null,
    run_relation_pub_zdb_id varchar(50),
    run_name varchar(30) not null constraint run_name_not_null,
    run_program varchar(10) not null constraint run_program_not_null,
    run_blastdb varchar(30) not null constraint run_blastdb_not_null,
    run_date datetime year to day  not null constraint run_date_not_null,
    run_version varchar(10),
    run_type varchar(30) not null constraint run_type_not_null
    )
in tbldbs1 extent size 32 next size 32 ;

create unique index run_primary_key_index
  on run (run_zdb_id)
  using btree in idxdbs3 ;

create unique index run_name_alternate_key_index
  on run (run_name)
  using btree in idxdbs2 ;

create index run_nomen_pub_foreign_key_index
  on run (run_nomen_pub_zdb_id)
  using btree in idxdbs1 ;

create index run_relation_pub_foreign_key_index
  on run (run_relation_pub_zdb_id)
  using btree in idxdbs4 ;

create index run_type_foreign_key_index
  on run (run_type)
  using btree in idxdbs1 ;

create index run_program_foreign_key_index
  on run (run_program)
  using btree in idxdbs4 ;

alter table run
  add constraint primary key (run_zdb_id)
  constraint run_primary_key ;

alter table run
  add constraint unique (run_name)
  constraint run_alternate_key ;

alter table run
  add constraint (foreign key (run_program)
  references run_program constraint
  run_program_foreign_key) ;

alter table run
  add constraint (foreign key (run_type)
  references run_type constraint
  run_type_foreign_key);

alter table run
  add constraint (foreign key(run_zdb_id)
  references zdb_active_data on delete cascade
  constraint run_zdb_Active_Data_foreign_key_odc);

alter table run
  add constraint (foreign key (run_nomen_pub_zdb_id)
  references publication on delete cascade constraint
  run_nomen_pub_foreign_key_odc);

alter table run
  add constraint (foreign key (run_relation_pub_zdb_id)
  references publication on delete cascade constraint
  run_relation_pub_foreign_key_odc);


insert into zdb_object_type (zobjtype_name, zobjtype_day,
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table,
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source,
	zobjtype_attribution_display_tier)
  values ('RUN', CURRENT,'1','','run',
	  'run_zdb_id', 't','f', '2') ;

alter table blastdb_run
  add constraint foreign key(blastdbr_run_zdb_id) references run
  constraint blastdbr_run_zdb_id_foreign_key ;


-------------------------------------

create table candidate (
    cnd_zdb_id varchar(50) not null constraint cnd_zdb_id_not_null,
    cnd_suggested_name varchar(60),
    cnd_is_problem boolean default 'f' not null constraint cnd_is_problem_not_null,
    cnd_mrkr_type varchar(10) not null constraint cnd_mrkr_type_not_null,
    cnd_run_count int default 0 not null constraint cnd_run_count_not_null,
    cnd_last_done_date datetime year to day,
    cnd_note lvarchar(1500)

)

fragment by round robin in tbldbs1, tbldbs2, tbldbs3
 extent size 2048 next size 2048 lock mode row;


create unique index candidate_primary_key_index
  on candidate (cnd_zdb_id)
  using btree in idxdbs3 ;

create index cnd_mrkr_type_foreign_key_index
  on candidate (cnd_mrkr_type)
  using btree in idxdbs2 ;


alter table candidate
  add constraint primary key (cnd_zdb_id)
  constraint candidate_primary_key ;

alter table candidate
  add constraint (foreign key (cnd_zdb_id)
  references zdb_active_data on delete cascade constraint
  candidate_active_data_foreign_key_odc);

alter table candidate
  add constraint (foreign key (cnd_mrkr_type)
  references marker_types constraint
  candidate_type_foreign_key);

create function increment_candidate_occurrences (vCndZdbId varchar(50))
        returning int;

       define counter like run_candidate.runcan_occurrence_order;

       let counter = (select cnd_run_count
       	   	     from candidate 
		     where cnd_zdb_id = vCndZdbId); 

       let counter = counter + 1;

       update candidate
         set cnd_run_count = cnd_run_count + 1
         where cnd_zdb_id = vCndZdbId ;

return counter;
end function;


insert into zdb_object_type (zobjtype_name, zobjtype_day,
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table,
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source,
	zobjtype_attribution_display_tier)
  values ('CND', CURRENT,'1','','candidate',
	  'cnd_zdb_id', 't','f', '2') ;


--------------------------------------

create table run_candidate (
    runcan_zdb_id varchar(50) not null constraint runcan_zdb_id_not_null,
    runcan_run_zdb_id varchar(50) not null constraint runcan_run_zdb_id_not_null,
    runcan_cnd_zdb_id varchar(50) not null constraint runcan_cnd_zdb_id_not_null,
    runcan_done boolean default 'f' not null constraint runcan_done_not_null,
    runcan_locked_by varchar(50),
    runcan_occurrence_order int not null constraint runcan_occurrence_order 
)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 1024 next size 1024 ;

create unique index run_candidate_primary_key_index
  on run_candidate (runcan_zdb_id)
  using btree in idxdbs2 ;

create index runcan_run_zdb_id_foreign_key_index
  on run_candidate (runcan_run_zdb_id)
  using btree in idxdbs3 ;

create index runcan_cnd_zdb_id_foreign_key_index
  on run_candidate (runcan_cnd_zdb_id)
  using btree in idxdbs3 ;

alter table run_candidate
  add constraint primary key (runcan_zdb_id)
  constraint run_candidate_primary_key ;

--not putting runcan_zdb_id in active data at this point.

alter table run_candidate
  add constraint (foreign key (runcan_run_zdb_id)
  references run on delete cascade constraint
  runcan_run_zdb_id_foreign_key_odc) ;

alter table run_candidate
  add constraint (foreign key (runcan_cnd_zdb_id)
  references candidate on delete cascade constraint
  runcan_cnd_zdb_id_foreign_key_odc) ;

create trigger run_candidate_insert_trigger insert on
    run_candidate referencing new as new_runcan
    for each row (
        execute function increment_candidate_occurrences (new_runcan.runcan_cnd_zdb_id)
	into run_candidate.runcan_occurrence_order	
    );


insert into zdb_object_type (
    zobjtype_name,
    zobjtype_day,
    zobjtype_seq,
    zobjtype_app_page,
    zobjtype_home_table,
    zobjtype_home_zdb_id_column,
    zobjtype_is_data,
    zobjtype_is_source,
    zobjtype_attribution_display_tier
  )values (
    'RUNCAN',
    CURRENT,
    '1',
    '',
    'run_candidate',
    'runcan_zdb_id',
    't',
    'f',
    '2'
);

---------------------------------------------------------

create table blast_query (
    bqry_zdb_id varchar(50) not null constraint bqry_zdb_id_not_null,
    bqry_runcan_zdb_id varchar(50) not null constraint bqry_runcan_zdb_id_not_null,
    bqry_accbk_pk_id int not null constraint bqry_accbk_pk_id_not_null
)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
 extent size 2048 next size 2048 ;

create unique index blast_query_primary_key_index
  on blast_query (bqry_zdb_id)
  using btree in idxdbs3 ;

create unique index blast_query_alternate_key_index
  on blast_query (bqry_runcan_zdb_id, bqry_accbk_pk_id)
  using btree in idxdbs4 ;

create index bqry_accbk_pk_id_foreign_key_index
  on blast_query (bqry_accbk_pk_id)
  using btree in idxdbs1 ;

alter table blast_query
  add constraint primary key (bqry_zdb_id)
  constraint blast_query_primary_key ;

alter table blast_query
  add constraint unique (bqry_runcan_zdb_id, bqry_accbk_pk_id)
  constraint blast_query_alternate_key ;

alter table blast_query
  add constraint (foreign key (bqry_runcan_zdb_id)
   references run_candidate on delete cascade constraint
   bqry_runcan_zdb_id_foreign_key_odc);


alter table blast_query
  add constraint (foreign key (bqry_accbk_pk_id)
   references accession_bank on delete cascade constraint
   bqry_accbk_pk_id_foreign_key_odc);

insert into zdb_object_type (zobjtype_name, zobjtype_day,
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table,
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source,
	zobjtype_attribution_display_tier)
  values ('BQRY', CURRENT,'1','','blast_query',
	  'bqry_zdb_id', 't','f', '2') ;


--------------------------------------------

create table blast_report (
    brpt_zdb_id varchar(50) not null constraint brpt_zdb_id_not_null,
    brpt_exitcode int not null constraint brpt_exitcode_not_null,
    brpt_bqry_zdb_id varchar(50) not null constraint brpt_bqry_not_null,
    brpt_detail_header lvarchar(2500)
)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 2048 next size 2048 ;

create unique index blast_report_primary_key_index
  on blast_report (brpt_zdb_id)
  using btree in idxdbs1 ;

alter table blast_report
  add constraint primary key (brpt_zdb_id)
  constraint blast_report_primary_key ;

create index brprt_bqry_fk_index
  on blast_report (brpt_bqry_zdb_id)
  using btree in idxdbs2;

alter table blast_report
  add constraint (foreign key (brpt_bqry_zdb_id)
  references blast_query on delete cascade constraint
  blast_report_bqry_foreign_key_odc);

insert into zdb_object_type (zobjtype_name, zobjtype_day,
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table,
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source,
	zobjtype_attribution_display_tier)
  values ('BRPT', CURRENT,'1','','blast_report',
	  'brpt_zdb_id', 't','f', '2') ;

--------------------------------------------

create table blast_hit (
    bhit_zdb_id varchar(50) not null constraint bhit_zdb_id_not_null,
    bhit_bqry_zdb_id varchar(50) not null constraint bhit_bqry_zdb_id_not_null,
    bhit_target_accbk_pk_id int not null constraint bhit_target_accbk_pk_id_not_null,
    bhit_hit_number int not null constraint bhit_hit_number_not_null,
    bhit_score int not null constraint bhit_score_not_null,
    bhit_expect_value double precision not null constraint bhit_expect_value_not_null,
    bhit_probability float,
    bhit_positives_numerator int not null constraint bhit_positives_numerator_not_null,
    bhit_positives_denominator int not null constraint bhit_positives_denominator_not_null,
    bhit_identities_numerator int,
    bhit_identities_denominator int,
    bhit_strand varchar(30),
    bhit_alignment lvarchar(30000)
)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 2048 next size 2048 ;

create unique index blast_hit_primary_key_index
 on blast_hit (bhit_zdb_id)
 using btree in idxdbs2;

--create unique index blast_hit_alternate_key_index
--  on blast_hit (bhit_bqry_zdb_id, bhit_target_accbk_pk_id)
--  using btree in idxdbs4;

--create index blast_hit_target_accbk_pk_id_foreign_key_index
--  on blast_hit(bhit_target_accbk_pk_id)
--  using btree in idxdbs3 ;


alter table blast_hit
  add constraint primary key (bhit_zdb_id)
  constraint blast_hit_primary_key ;

--alter table blast_hit
--  add constraint unique (bhit_bqry_zdb_id, bhit_target_accbk_pk_id)
--  constraint blast_hit_alternate_key ;

alter table blast_hit
  add constraint (foreign key (bhit_bqry_zdb_id)
  references blast_query on delete cascade constraint
  bhit_bqry_zdb_id_foreign_key_odc);

--alter table blast_hit
--  add constraint (foreign key (bhit_target_accbk_pk_id)
--  references accession_bank on delete cascade constraint
--  bhit_target_accbk_pk_id_foreign_key_odc);

insert into zdb_object_type (zobjtype_name, zobjtype_day,
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table,
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source,
	zobjtype_attribution_display_tier)
  values ('BHIT', CURRENT,'1','','blast_hit',
	  'bhit_zdb_id', 't','f', '2') ;


------------------------------------------

create table marker_family_name (mfam_name varchar(255) not null constraint mfam_name_not_null)
in tbldbs2 extent size 32 next size 32 ;

create unique index marker_family_name_primary_key_index
  on marker_family_name (mfam_name)
  using btree in idxdbs1 ;

alter table marker_family_name
  add constraint primary key (mfam_name)
  constraint marker_family_name_primary_key ;

------------------------------------------

create table marker_family_member (
    mfammem_mfam_name varchar(255) not null constraint mfammem_mfam_name_not_null,
    mfammem_mrkr_zdb_id varchar(50) not null constraint mfammem_mrkr_zdb_id_not_null
)
in tbldbs2
extent size 64 next size 64 ;

create unique index marker_family_member_primary_key_index
  on marker_family_member (mfammem_mrkr_zdb_id, mfammem_mfam_name)
  using btree in idxdbs1 ;

create index marker_family_member_mrkr_zdb_id_foreign_key_index
 on marker_family_member(mfammem_mfam_name)
 using btree in idxdbs2 ;

alter table marker_family_member
  add constraint primary key (mfammem_mrkr_zdb_id, mfammem_mfam_name)
  constraint marker_family_member_primary_key ;

alter table marker_family_member
  add constraint (foreign key (mfammem_mrkr_zdb_id)
   references marker on delete cascade constraint
   marker_family_member_mrkr_zdb_id_foreign_key_odc);

alter table marker_family_member
  add constraint (foreign key (mfammem_mfam_name)
  references marker_family_name constraint
  marker_family_member_mfam_name_foreign_key );

set constraints all deferred ;

update foreign_db
  set (fdb_url_suffix,fdb_db_query) = ('','/cgi-bin/pre_vega.cgi?ottdarg=')
  where fdb_db_name = 'PREVEGA';

update foreign_db_contains
  set (fdbcont_fdbdt_super_type,fdbcont_fdbdt_data_type) =
      ('sequence','Vega Transcript')
  where fdbcont_fdb_db_name = 'PREVEGA';

insert into foreign_db_contains (fdbcont_zdb_id,
					fdbcont_fdbdt_data_type,
					fdbcont_fdb_db_name,
					fdbcont_organism_common_name,
					fdbcont_fdbdt_super_type)
select get_id('FDBCONT'),
		'Vega Transcript',
		'INTVEGA',
		'Zebrafish',
		'sequence'
  from single ;

insert into foreign_db (fdb_db_name,
       	    	        fdb_db_query,
			fdb_url_suffix,
			fdb_db_significance)
 values ('INTVEGA',
	  '/cgi-bin/internal_vega.cgi?ottdarg=',
	  '',
	  '2');

insert into foreign_db_contains (fdbcont_zdb_id,
					fdbcont_fdbdt_data_type,
					fdbcont_fdb_db_name,
					fdbcont_organism_common_name,
					fdbcont_fdbdt_super_type)
select get_id('FDBCONT'),
		'Polypeptide',
		'UniProt',
		'Mouse',
		'sequence'
  from single ;


insert into foreign_db_contains (fdbcont_zdb_id,
					fdbcont_fdbdt_data_type,
					fdbcont_fdb_db_name,
					fdbcont_organism_common_name,
					fdbcont_fdbdt_super_type)
select get_id('FDBCONT'),
		'Polypeptide',
		'UniProt',
		'Human',
		'sequence'
  from single ;


insert into foreign_db (fdb_db_name,
       	    	        fdb_db_query,
			fdb_url_suffix,
			fdb_db_significance)
 values ('NovelGene',
 	  'http://www.google.com/search?q=',
	  '',
	  '2');

insert into foreign_db_contains (fdbcont_zdb_id,
					fdbcont_fdbdt_data_type,
					fdbcont_fdb_db_name,
					fdbcont_organism_common_name,
					fdbcont_fdbdt_super_type)
select get_id('FDBCONT'),
       		'other',
		'NovelGene',
		'Zebrafish',
		'sequence'
  from single ;

insert into zdb_active_data
 select fdbcont_zdb_id
   from foreign_db_contains
   where not exists (Select 'x' from
   	     	    	    zdb_active_Data
			    where zactvd_zdb_id = fdbcont_Zdb_id);

set constraints all immediate ;

load from /research/zusers/staylor/hoover/RenoData/gene_fams.txt
insert into marker_family_name ;


update statistics high for table run;
update statistics high for table candidate;
update statistics high for table run_candidate;
update statistics high for table blast_query;
update statistics high for table blast_hit ;
update statistics high for table accession_bank;
update statistics high for table marker;

create table entrez_gene(
    eg_acc_num varchar(50)
                  not null constraint en_entrez_acc_num_not_null,
    eg_symbol varchar(60),
    eg_name varchar(255)
)
fragment by round robin in tbldbs1,tbldbs2,tbldbs3
extent size 4096 next size 4096
;

create unique index entrez_gene_primary_key_index
 on entrez_gene(eg_acc_num)
 using btree in idxdbs2 ;

create unique index entrez_gene_alternate_key_index
 on entrez_gene(eg_acc_num, eg_symbol, eg_name)
 using btree in idxdbs2 ;

alter table entrez_gene add constraint primary key
  (eg_acc_num) constraint
  entrez_gene_primary_key ;

--------------------------------------------------------------------------

create table entrez_to_protein(
    ep_pk_id serial not null constraint ep_pk_id_not_null,
    ep_organism_common_name varchar(30)
                    not null constraint ep_organism_common_name_not_null,
    ep_entrez_acc_num varchar(50) not null constraint ep_entrez_acc_num_not_null,
    ep_protein_acc_num varchar(50) not null constraint ep_protein_Acc_num
)
fragment by round robin in tbldbs1,tbldbs2,tbldbs3
extent size 4096 next size 4096
lock mode page;

create unique index ep_primary_key_index
 on entrez_to_protein (ep_pk_id)
 using btree in idxdbs3;

create unique index ep_alternate_key_index
  on entrez_to_protein (ep_entrez_acc_num,
      ep_protein_acc_num,
      ep_organism_common_name)

  using btree in idxdbs3;

create index ep_protein_acc_num_index
    on entrez_to_protein (ep_protein_acc_num)
    using btree in idxdbs3;

alter table entrez_to_protein
  add constraint primary key (ep_pk_id)
  constraint entrez_to_protein_primary_key;

alter table entrez_to_protein
  add constraint (foreign key (ep_entrez_acc_num)
  references entrez_gene on delete cascade constraint
  ep_entrez_acc_num_foreign_key_odc);

update statistics high for table entrez_to_protein;
-------------------------------------------------------------

create table entrez_to_xref(

    ex_entrez_acc_num varchar(50)
                  not null constraint ex_entrez_acc_num_not_null,
    ex_xref varchar(30) not null constraint ex_xref_not_null
)
fragment by round robin in tbldbs1,tbldbs2,tbldbs3
extent size 4096 next size 4096 ;

create unique index entrez_xref_primary_key_index
      on entrez_to_xref(ex_entrez_acc_num, ex_xref)
 using btree in idxdbs1 ;

create index ex_xref_index
      on entrez_to_xref(ex_xref)
 using btree in idxdbs1 ;

alter table entrez_to_xref add constraint (
    foreign key (ex_entrez_acc_num) references entrez_gene
    on delete cascade constraint
    ex_entrez_acc_num_foreign_key_odc);

alter table entrez_to_xref add constraint primary
    key (ex_entrez_acc_num,ex_xref) constraint
    entrez_to_xref_primary_key;

alter table accession_bank
  modify (accbk_fdbcont_zdb_id varchar(50) not null constraint accbk_fdbcont_zdb_id_not_null) ;

--rollback work;
commit work;


-- load run report hit
-- move run
-- add query accession
-- move report
-- (new) candidate_accession
--  candidate
--   ->run_candidate
--    -> blast_query
--     -> [blast_report & blast_hit]
----------------------------------------------------------
-- try moving the transaction start  to after all the create tables

drop table tmp_run;
drop table tmp_report;
drop table tmp_hit;
drop table tmp_candidate;
drop table tmp_run_cnd;
drop table tmp_blast_query;

create table tmp_run (
    trun_name    	text,
    trun_host    	text,
    trun_datetime	text,
    trun_program 	varchar(10),
    trun_database   varchar(100), ---> blast_database
    trun_query_type varchar(20),
    trun_target_type varchar(20),
    trun_details 	text);

\echo 'Import run table'

\copy tmp_run from 'run.unl';

alter table tmp_run add trun_zad text;
update tmp_run set trun_zad = get_id('RUN');

\echo 'set a default foreign db for this runs QUERIES';
alter table tmp_run add trun_query_fdbcont text;

update tmp_run set trun_query_fdbcont =
    case
        when substring(trun_name,1,3) = 'ZGC' then     'ZDB-FDBCONT-040412-37' -- GenBank RNA
        when substring(trun_name,1,4) = 'Vega' then(
        select fdbcont_zdb_id from foreign_db_contains, foreign_db, foreign_db_data_type
         where fdbdt_data_type = 'RNA'
           and fdb_db_name = 'PREVEGA'
           and fdbcont_organism_common_name = 'Zebrafish'
           and fdbdt_super_type = 'sequence'
	   and fdbcont_fdb_db_id = fdb_db_pk_id
	   and fdbcont_fdbdt_id = fdbdt_pk_id
        )
        when substring(trun_name,1,7) = 'UniProt' then 'ZDB-FDBCONT-040412-47' -- (get FDBCONT from DB_LINK)
        when substring(trun_name,1,6) = 'Sanger' then 'ZDB-FDBCONT-061018-1' -- (get FDBCONT from DB_LINK)
	when substring(trun_name,1,7) = 'Protein' then 'ZDB-FDBCONT-061018-1'
        else  'ZDB-FDBCONT-040412-37'     
    end
;
-- fix trailing space from case statment
update tmp_run set trun_query_fdbcont = trim(trun_query_fdbcont);

\echo 'set a default foreign db for this runs HITS';
alter table tmp_run add trun_target_fdbcont text;

update tmp_run set trun_target_fdbcont =
    case
        when substring(trun_name,1,3) = 'ZGC' then     'ZDB-FDBCONT-040412-37' -- GenBank RNA
        when substring(trun_name,1,4) = 'Vega' then    'ZDB-FDBCONT-040412-37' -- GenBank RNA
        when substring(trun_name,1,7) = 'UniProt' then 'ZDB-FDBCONT-040412-47' -- UniProt Zebrafish
        when substring(trun_name,1,6) = 'Sanger' then 'ZDB-FDBCONT-060108-1' -- Sanger-Ensembl
	when substring(trun_name,1,7) = 'Protein' then 'ZDB-FDBCONT-110301-1' -- Sanger-Ensembl	
        else  'ZDB-FDBCONT-040412-37'
    end
;
---------------------------------------------------------------------

create table tmp_report (
    trpt_acc       text,  -- BC146618
    trpt_acc_db    varchar(20),  -- 'gb|ref|tpe|'
    trpt_acc_type  varchar(20),  -- 'transcript|RNA|DNA|RNA ...' (PSEUDO?)
    trpt_locus_acc text,  -- 'zgc:158136' or 'OTTDARG'
    trpt_acc_len   integer,      -- 3498
    trpt_alt_id    text,  -- 'si:dkey'
    trpt_exitcode  integer,	     -- 0
    trpt_defline   text,     -- {gi|148921504|gb|BC146618.1|zgc:158136 Danio rerio RNA clone MGC:158136 IMAGE:6970042, complete cds (3498 letters; record 1)}
    trpt_detail    text
);


\echo 'Import report table';
\copy tmp_report from 'report.unl';

update tmp_report set trpt_locus_acc = NULL where trpt_locus_acc = '';
update tmp_report set trpt_locus_acc = trpt_acc where trpt_locus_acc is NULL;
update tmp_report set trpt_alt_id = NULL where trpt_alt_id = '';
update tmp_report set trpt_alt_id = trpt_locus_acc where trpt_alt_id is NULL;

create index tmp_report_trpt_acc_idx on tmp_report (trpt_acc ) ;
create index tmp_report_trpt_alt_id_idx on tmp_report (trpt_alt_id ) ;
create index tmp_report_trpt_acc_len_idx on tmp_report (trpt_acc_len ) ; -- cheap and might help speed up accession bank update.




------------------------------------------------------------------
create table tmp_hit (
    thit_rpt             text, --;;; : BC146618.1
    thit_order           integer,     --;;; : 0
    thit_acc             text, --;;; : 'BC116508'
    thit_acc_db          varchar(10), --;;; : 'gb'
    thit_acc_type        varchar(20), --;;; : 'nucleotide|protein|transcript --- RNA|DNA|[t|r|m|u]RNA|RNA ...EST GSS WGS ...
    thit_defline         text,    --;;; : {BC116508.1|BC116508 Danio rerio zgc     ;;;; :136342, mRNA (RNA clone MGC     ;;;; :136342 IMAGE     ;;;; :8128252), complete cds Length = 2014}
    thit_species         varchar(15), --;;; : 'Danio rerio'
    thit_acc_len         integer,     --;;; : 0
    thit_score           integer,     --;;; : '5874'
    thit_bits            decimal,     --;;; : '887.4'
    thit_expect          float,       --;;; : '0.'
    thit_probtype        varchar(20), --;;; : ''  [P | Sum P(N)]
    thit_prob            float,       --;;; : '0.'
    thit_identites_num   integer,     --;;; : '1188'
    thit_identites_denom integer,     --;;; : '1197'
    thit_positives_num   integer,     --;;; : '1188'
    thit_positives_denom integer,     --;;; : '1197'
    thit_strand          varchar(5), --;;; : ''
    thit_alignment       text     --;;;; : {(99%), Strand = Plus / Plus ...
  --thit_target_id       (accession_bank_id)
  --thit_zad
);

\echo 'Import hit table';

\copy tmp_hit from 'hit.unl';

\echo 'Import hit table';

select distinct thit_acc_db
 from tmp_hit;

create index tmp_hit_thit_rpt_idx on  tmp_hit(thit_rpt);
create index tmp_hit_thit_acc_idx on  tmp_hit(thit_acc);

---------------------------------

\echo 'filter hits to self (should not be any)';

\echo 'this is a stop-gap on loading old runs srt >tpe| deflines to transcripts';
update tmp_hit set thit_acc_type = (
	select case
		when substring(trun_name,1,4) in ('Vega','ZGC_')
         	     		    and thit_acc_db = 'tpe' then 'transcript';
		else trun_target_type
	end
	from tmp_run
) where (thit_acc_type is NULL or thit_acc_type = '')
;

update tmp_hit set thit_acc_type = trim(thit_acc_type);

-- these tables will not be populated till later on

create table tmp_candidate (
	tcnd_zad           text,
 	tcnd_mrkr_type        VARCHAR(10), --GENEP
	tcnd_suggested_name   VARCHAR(60)
);

create table tmp_run_cnd (
    truncan_zad       text,
    truncan_run_zdb_id   text,
    truncan_cnd_zdb_id   text
);


create table tmp_blast_query (
    tbqry_zad text ,
    tbqry_runcan_zdb_id text,
    tbqry_accbk_pk_id int8
);

-- everything above does not impact existing tables
-- so although there maybe broken tables to remove
-- if a roll back happend  it might be better than the locks

begin work;

\echo 'move run to schema table';
insert into zdb_active_data select trun_zad from tmp_run;

insert into run (
    run_zdb_id,
    run_name,
    run_program,
    run_blastdb,
    run_date,
    run_type,
    run_relation_pub_zdb_id

) select
    trun_zad,
    trun_name,
    trun_program,
    trun_database,
    trun_datetime::date,
    case
        when runprog_target_type = 'n' then 'Redundancy'
        when runprog_target_type = 'p' then 'Nomenclature'
    end,
    case
        when trun_name like 'Vega_%'    then 'ZDB-PUB-030703-1'
        when trun_name like 'ZGC_%'     then 'ZDB-PUB-040217-1'
        when trun_name like 'UniProt_%' then 'ZDB-PUB-030905-1'
	when trun_name like 'Protein_%' then 'ZDB-PUB-030905-1'
        else NULL
    end
 from tmp_run, run_program
 where trun_program = runprog_program;

\echo 'fix trailing space from case statment';
update run set run_type = trim(run_type);

update run set run_nomen_pub_zdb_id= trim(run_nomen_pub_zdb_id);

update run set run_relation_pub_zdb_id = trim(run_relation_pub_zdb_id);

------------------------------------

\echo 'add the accession bank id for the query  to the record (if it exists)';

alter table tmp_report add trpt_query_id int8;

update tmp_report
 set trpt_query_id = (
    select accbk_pk_id
     from accession_bank,tmp_run
     where trpt_acc = accbk_acc_num
 )where exists (
    select 1 from accession_bank, tmp_run
     where trpt_acc = accbk_acc_num
);

create index tmp_report_trpt_query_idx on tmp_report(trpt_query_id);

\echo 'update accession_bank with NULL length';

update accession_bank set accbk_length = (
    select trpt_acc_len
     from tmp_report
     where trpt_query_id = accbk_pk_id
) where accbk_length is NULL
    and exists (
    select 1 from tmp_report
     where trpt_query_id = accbk_pk_id
       and trpt_acc_len > 0
);

update accession_bank set accbk_length = (
    select max(trpt_acc_len)
     from tmp_report
     where trpt_query_id = accbk_pk_id
) where accbk_length is NOT NULL
    and exists (
    select 1 from tmp_report
     where trpt_query_id = accbk_pk_id
      and trpt_acc_len > 0
       and trpt_acc_len != accbk_length
);

\echo 'clean up'

create temp table tmp_ottdart_length(
       tol_acc text,
       tol_len integer
);

\echo 'clobber existing VEGA accession_bank deflines if they are changed';

update accession_bank set accbk_defline = (
    select (max (length(trpt_defline)))
     from tmp_report
     where trpt_query_id = accbk_pk_id
)
 where substring(accbk_acc_num,1,6) = 'OTTDAR'
   and exists(
    select 1 from tmp_report
     where trpt_query_id = accbk_pk_id
       and trpt_defline is not NULL
       and trpt_defline != accbk_defline
);

create temp table tmp_distinct_acc (
       tdc_acc text,
       tdc_fdbcont text,
       tdc_defline text
);

--- make a temp table with acc, fdb and defline 
insert into tmp_distinct_acc
select distinct
    trpt_acc,      -- BC146618
    case  -- all QUERIES are assumed to be zebrafish
        when trpt_acc_type = 'protein'
     and trpt_acc_db = 'ref'  then 'ZDB-FDBCONT-040412-39'
        when trpt_acc_type = 'protein'
     and trpt_acc_db = 'gb'   then 'ZDB-FDBCONT-040412-42'
        when trpt_acc_type = 'protein'
         and trpt_acc_db = 'wz'   then 'ZDB-FDBCONT-090929-8'
        when trpt_acc_type = 'protein'
         and trpt_acc_db = 'sp'   then 'ZDB-FDBCONT-040412-47'
        when trpt_acc_type = 'nucleotide'
         and trpt_acc_db = 'ref'  then 'ZDB-FDBCONT-040412-38'
        when trpt_acc_type = 'nucleotide'
         and trpt_acc_db = 'gb'   then 'ZDB-FDBCONT-040412-37'
        when trpt_acc_type = 'transcript'
         and trpt_acc_db = 'tpe'  then 'ZDB-FDBCONT-050210-1'
        else trun_query_fdbcont
    end,
    max(trpt_defline)
 from tmp_report, tmp_run
 where trpt_query_id is NULL
 group by 1,2
;

\echo 'add new *QUERIES* to accession_bank';

insert into  accession_bank (
    accbk_acc_num ,
    accbk_length ,
    --accbk_pk_id ,
    accbk_fdbcont_zdb_id ,
    accbk_defline
)
select distinct
    tdc_acc,      -- BC146618    
    trpt_acc_len,  -- 3498
    tdc_fdbcont,
    tdc_defline
 from tmp_run, tmp_distinct_acc, tmp_report
 where trpt_query_id is NULL
   and trpt_defline = tdc_defline
;

update accession_bank set accbk_fdbcont_zdb_id = trim (accbk_fdbcont_zdb_id)
 where exists (select 1 from tmp_report where accbk_acc_num = trpt_acc)
   and octet_length(accbk_fdbcont_zdb_id) != length(accbk_fdbcont_zdb_id)
;

\echo 'add the new accession bank ids back in to the queries';

update tmp_report
 set trpt_query_id = (
    select accbk_pk_id
     from accession_bank,tmp_run
     where trpt_acc = accbk_acc_num
 )where exists (
    select 1
     from accession_bank, tmp_run
     where trpt_acc = accbk_acc_num
     and trpt_query_id is NULL
);

-----------------------------------------------------------
\echo 'add the candidate id to the record (if it exists)';

alter table tmp_report add trpt_cnd_zdbid text;

update tmp_report
 set trpt_cnd_zdbid = (
    select cnd_zdb_id
     from candidate
     where trpt_alt_id = cnd_suggested_name
 )where exists (
    select 1 from candidate
     where trpt_alt_id = cnd_suggested_name
 );

\echo 'how many Candidates still need to be added?';
select count(*) howmany from tmp_report where trpt_cnd_zdbid is NULL;

\echo 'create new candidates if not seen before';


insert into tmp_candidate (tcnd_suggested_name,tcnd_mrkr_type)
select distinct trpt_alt_id,
 case
 	when trpt_defline like '%pesudogene%' then 'GENEP'
 	else 'GENE'
 end
 from  tmp_report
 where trpt_cnd_zdbid is NULL
   and trpt_alt_id is not NULL
;

update tmp_candidate set tcnd_mrkr_type = trim(tcnd_mrkr_type);
update tmp_candidate set tcnd_zad = get_id('CND');


insert into  zdb_active_data select tcnd_zad from tmp_candidate;

insert into candidate (
    cnd_zdb_id,
    cnd_mrkr_type,
    cnd_run_count,
    cnd_suggested_name
)
select distinct
    tcnd_zad,
    tcnd_mrkr_type,   ---GENE|GENEP
    -1,
    tcnd_suggested_name
 from tmp_candidate
;

\echo 'fix trailing space from case statment';
update candidate set cnd_mrkr_type = trim(cnd_mrkr_type)
 ;

\echo 'aassociate the new candidates with their reports';
update tmp_report set trpt_cnd_zdbid = (
    select tcnd_zad
    from tmp_candidate
     where tcnd_suggested_name = trpt_alt_id
)
 where trpt_cnd_zdbid is NULL
;

----------------------------------
\echo 'next make run_candidates'


insert into  tmp_run_cnd (truncan_run_zdb_id, truncan_cnd_zdb_id)
 select  distinct trun_zad, trpt_cnd_zdbid
  from tmp_run, tmp_report
;

update tmp_run_cnd set truncan_zad = get_id('RUNCAN');

insert into run_candidate (
    runcan_zdb_id,
    runcan_run_zdb_id,
    runcan_cnd_zdb_id
)
select * from  tmp_run_cnd;

--------------------------------
\echo 'next make blast_query';

insert into tmp_blast_query (
    tbqry_runcan_zdb_id,
    tbqry_accbk_pk_id
)
select distinct truncan_zad, trpt_query_id
 from tmp_report, tmp_run_cnd
 where truncan_cnd_zdb_id =  trpt_cnd_zdbid 
   and trpt_query_id is not NULL
;

update tmp_blast_query set tbqry_zad = get_id('BQRY');

insert into blast_query (
    bqry_zdb_id,
    bqry_runcan_zdb_id,
    bqry_accbk_pk_id
)
select * from tmp_blast_query where tbqry_runcan_zdb_id is not NULL;

create index tmp_blast_query_tbqry_accbk_pk_idx
    on tmp_blast_query(tbqry_accbk_pk_id);

-----------------------------------------
\echo 'next make blast_report';

alter table tmp_report add trpt_brpt_zad text;
update tmp_report set trpt_brpt_zad = get_id('BRPT');

insert into blast_report (
    brpt_zdb_id,
    brpt_exitcode,
    brpt_bqry_zdb_id,
    brpt_detail_header
)
 select distinct
    trpt_brpt_zad,
    trpt_exitcode,
    tbqry_zad,
    trpt_detail
  from  tmp_report, tmp_blast_query
  where  trpt_query_id  = tbqry_accbk_pk_id
;

---------------------------------------------------------------------
\echo 'add the accession bank id for the target to tmp_hit (if it exists)';

alter table tmp_hit add thit_target_id int;

update tmp_hit
 set thit_target_id = (
    select accbk_pk_id from accession_bank
     where thit_acc = accbk_acc_num
 )where exists (
    select 1 from accession_bank
    where thit_acc = accbk_acc_num
);

create index tmp_hit_thit_target_idx
    on tmp_hit(thit_target_id);


\echo 'update accession_banks with NULL length if any is available';
update accession_bank set accbk_length = (
    select max(thit_acc_len)
     from tmp_run,tmp_hit
     where thit_target_id = accbk_pk_id
       and trun_target_fdbcont = accbk_fdbcont_zdb_id
) where accbk_length is NULL
    and exists (
    select 1 from tmp_run,tmp_hit
     where thit_target_id = accbk_pk_id
       and trun_target_fdbcont = accbk_fdbcont_zdb_id
       and thit_acc_len > 0
);

\echo 'update not null accession_banks length if new one is available';

update accession_bank set accbk_length = (
    select max(thit_acc_len)
     from tmp_run,tmp_hit
     where thit_target_id = accbk_pk_id
       and trun_target_fdbcont = accbk_fdbcont_zdb_id
) where accbk_length is not NULL
    and exists (
    select 1 from tmp_run,tmp_hit
     where thit_target_id = accbk_pk_id
       and trun_target_fdbcont = accbk_fdbcont_zdb_id
       and thit_acc_len > 0
       and thit_acc_len != accbk_length
);


\echo 'update existing NULL accession_bank defline if one is available';

update accession_bank set accbk_defline = (
    select distinct thit_defline
     from tmp_hit,tmp_run
     where thit_target_id = accbk_pk_id
       and trun_target_fdbcont = accbk_fdbcont_zdb_id
) where accbk_defline is NULL
  and exists (
    select 1 from tmp_run,tmp_hit
     where thit_target_id = accbk_pk_id
       and trun_target_fdbcont = accbk_fdbcont_zdb_id
       and thit_defline is not NULL
);

select count(thit_acc) all_told,count(distinct thit_acc) howmany from tmp_hit where thit_target_id is NULL;

\echo 'add new *TARGETS* to accession_bank';

insert into  accession_bank (
    accbk_acc_num ,
    accbk_length ,
    accbk_fdbcont_zdb_id ,
    accbk_defline
)
select distinct
    thit_acc,      -- BC146618
    max(thit_acc_len),  -- 3498
    max(case  -- HITs
        when thit_acc_type = 'protein'
         and thit_acc_db = ''
         and thit_species= 'Homo sapiens' then (
                select fdbcont_zdb_id from foreign_db_contains, foreign_db, foreign_db_data_type
                where fdbdt_super_type = 'sequence'
                  and fdbdt_data_type = 'Polypeptide'
                  and fdb_db_name = 'UniProtKB'
		  and fdbcont_fdb_db_id = fdb_db_pk_id
		  and fdbcont_fdbdt_id = fdbdt_pk_id
                  and fdbcont_organism_common_name = 'Human'
         )
        when thit_acc_type = 'protein'
         and thit_acc_db = 'sp'
         and thit_species= 'Mus musculus'  then(
                select fdbcont_zdb_id from foreign_db_contains, foreign_db, foreign_db_data_type
                where fdbdt_super_type = 'sequence'
                  and fdbdt_data_type = 'Polypeptide'
                  and fdb_db_name = 'UniProtKB'
		  and fdbcont_fdb_db_id = fdb_db_pk_id
		  and fdbcont_fdbdt_id = fdbdt_pk_id
                  and fdbcont_organism_common_name = 'Mouse'
         )
        when thit_acc_type = 'protein'
         and thit_acc_db = 'sp'
         and thit_species= 'Homo sapiens'  then(
                select fdbcont_zdb_id from foreign_db_contains, foreign_db, foreign_db_data_type
                where fdbdt_super_type = 'sequence'
                  and fdbdt_data_type = 'Polypeptide'
                  and fdb_db_name = 'UniProtKB'
		  and fdbcont_fdb_db_id = fdb_db_pk_id
		  and fdbcont_fdbdt_id = fdbdt_pk_id
                  and fdbcont_organism_common_name = 'Human'
         )
       when thit_acc_type = 'protein'
         and thit_acc_db = 'ref'           
	     then 'ZDB-FDBCONT-040412-39'
        when thit_acc_type = 'protein'
         and thit_acc_db = 'gb'            
	     then 'ZDB-FDBCONT-040412-42'
        when thit_acc_type = 'protein'
         and thit_acc_db = 'wz'            
	     then 'ZDB-FDBCONT-090929-8'
        when thit_acc_type = 'protein'
         and thit_acc_db = 'sp'
	     then 'ZDB-FDBCONT-040412-47'
        when thit_acc_type = 'protein'
         and thit_acc_db = 'sp'
         and thit_species= 'Danio rerio'   then 'ZDB-FDBCONT-040412-47'
        when thit_acc_type = 'nucleotide'
         and thit_acc_db = 'ref'           then 'ZDB-FDBCONT-040412-38'
        when thit_acc_type = 'nucleotide'
         and thit_acc_db = 'wz'            then 'ZDB-FDBCONT-040412-31'
        when thit_acc_type = 'nucleotide'
         and thit_acc_db = 'gb'            then 'ZDB-FDBCONT-040412-37'
        when thit_acc_type = 'transcript'
         and thit_acc_db = 'tpe'           then 'ZDB-FDBCONT-060417-1'
        else trun_target_fdbcont
    end ),
    max(thit_defline)
 from tmp_hit, tmp_run
 where thit_target_id is NULL
 group by thit_acc
;

update accession_bank set accbk_fdbcont_zdb_id = trim (accbk_fdbcont_zdb_id)
 where exists (
 	select 1 from tmp_hit
 	 where accbk_acc_num = thit_acc
 ) and octet_length(accbk_fdbcont_zdb_id) != length (accbk_fdbcont_zdb_id)
;

\echo 'add this accession length if it is > 0 and accbk_length = 0';

update accession_bank set accbk_length = (
    select max(thit_acc_len) from tmp_hit
     where thit_acc = accbk_acc_num
 )
 where (accbk_length < 1 or accbk_length is NULL)
   and exists (
   	select 1 from tmp_hit
   	 where accbk_acc_num =  thit_acc
   	   and thit_acc_len > 0
);

\echo 'add the new accession bank ids back into tmp_hit';

update tmp_hit
 set thit_target_id = (
    select accbk_pk_id from accession_bank
     where thit_acc = accbk_acc_num
 )
 where thit_target_id is NULL
   and exists (
    select 1 from accession_bank
    where thit_acc = accbk_acc_num
);

\echo 'make a blast hit';
alter table tmp_hit add thit_zad text;
update tmp_hit set thit_zad = get_id('BHIT');

\echo 'loading alignments takes a while';
insert into blast_hit (
    bhit_zdb_id,
    bhit_bqry_zdb_id,
    bhit_target_accbk_pk_id,
    bhit_hit_number,
    bhit_score,
    bhit_expect_value,
    bhit_probability,
    bhit_positives_numerator,
    bhit_positives_denominator,
    bhit_identities_numerator,
    bhit_identities_denominator,
    bhit_strand,
    bhit_alignment
)
select distinct
    thit_zad,
    tbqry_zad,
    thit_target_id,
    thit_order,
    thit_score,
    thit_expect,
    thit_prob,
    thit_identites_num,
    thit_identites_denom,
    thit_positives_num,
    thit_positives_denom,
    thit_strand,
    thit_alignment

 from tmp_hit, tmp_blast_query, tmp_report
 where trpt_query_id = tbqry_accbk_pk_id
   and trpt_acc      = thit_rpt
   and thit_rpt     != thit_acc  -- no self hits (double checking)
;

\echo 'alignments loaded';

-----------------------------------------------------------
-----------------------------------------------------------
-----------------------------------------------------------

\echo 'FINISHED';
\echo '############################################################################'


-------- droops outside rollback/commit ---------------------------------
drop table tmp_run;
drop table tmp_report;
drop table tmp_hit;
drop table tmp_candidate;
drop table tmp_run_cnd;
drop table tmp_blast_query;

--rollback/commit applied externally with drop tables


-- load run report hit
-- move run
-- add query accession
-- move report
-- (new) candidate_accession
--  candidate
--   ->run_candidate
--    -> blast_query
--     -> [blast_report & blast_hit]

begin work;
! echo "BEGIN `date`"
create table tmp_run (
    trun_name    	varchar(40),
    trun_host    	varchar(40),
    trun_datetime	varchar(40),
    trun_program 	varchar(10),  ---> run_program  ?? --> run_type ??
    trun_database   varchar(100), ---> blast_database
    trun_query_type varchar(20),
    trun_target_type varchar(20),
    trun_details 	lvarchar(2500)
  --trun_zad
  --trun_query_fdbcont
  --trun_target_fdbcont
);

! echo "Import run table"
load from 'run.unl' insert into tmp_run;
alter table tmp_run add trun_zad varchar(50);
update tmp_run set trun_zad = get_id('RUN');

! echo "set a default foreign db for this runs QUERIES"
alter table tmp_run add trun_query_fdbcont varchar(50);
update tmp_run set trun_query_fdbcont =
    case
        when trun_name[1,3] = 'ZGC' then     'ZDB-FDBCONT-040412-37' -- GenBank RNA
--      when trun_name[1,4] = 'Vega' then    'ZDB-FDBCONT-050210-1'  -- PreVega ... (make that internal)
        when trun_name[1,4] = 'Vega' then(
        select fdbcont_zdb_id from foreign_db_contains, foreign_db, foreign_db_data_type
         where fdbdt_data_type = 'RNA'
           and fdb_db_name = 'PREVEGA'
           and fdbcont_organism_common_name = 'Zebrafish'
           and fdbdt_super_type = 'sequence'
	   and fdbcont_fdb_db_id = fdb_db_pk_id
	   and fdbcont_fdbdt_id = fdbdt_pk_id
        )
        when trun_name[1,7] = 'UniProt' then 'ZDB-FDBCONT-040412-47' -- (get FDBCONT from DB_LINK)
        else  'ZDB-FDBCONT-040412-37'        --local??? google???    -- GenBank RNA
    end
;
-- fix trailing space from case statment
update tmp_run set trun_query_fdbcont = trim( trun_query_fdbcont);


! echo "set a default foreign db for this runs HITS"
alter table tmp_run add trun_target_fdbcont varchar(50);
update tmp_run set trun_target_fdbcont =
    case
        when trun_name[1,3] = 'ZGC' then     'ZDB-FDBCONT-040412-37' -- GenBank RNA
        when trun_name[1,4] = 'Vega' then    'ZDB-FDBCONT-040412-37' -- GenBank RNA
        when trun_name[1,7] = 'UniProt' then 'ZDB-FDBCONT-040412-47' -- UniProt Zebrafish
        else  'ZDB-FDBCONT-040412-37'
    end
;
-- fix trailing space from case statment (not currently needed)
--update tmp_run set trun_target_fdbcont = trim(trun_target_fdbcont);

---------------------------------------------------------------------

create table tmp_report (
    trpt_acc       varchar(40),  -- BC146618
    trpt_acc_db    varchar(20),  -- "gb|ref|tpe|"
    trpt_acc_type  varchar(20),  -- "transcript|RNA|DNA|RNA ..." (PSEUDO?)
    trpt_locus_acc varchar(60),  -- "zgc:158136" or "OTTDARG"
    trpt_acc_len   integer,      -- 3498
    trpt_alt_id    varchar(60),  -- "si:dkey"
    trpt_exitcode  integer,	     -- 0
    trpt_defline   lvarchar,     -- {gi|148921504|gb|BC146618.1|zgc:158136 Danio rerio RNA clone MGC:158136 IMAGE:6970042, complete cds (3498 letters; record 1)}
    trpt_detail    lvarchar
  --trpt_query_id  (accession_bank_id)
  --trpt_cnd_zdbid
  --trpt_brpt_zad

) fragment by round robin in tbldbs1,tbldbs2,tbldbs3 ;


! echo "Import report table"
load from 'report.unl' insert into tmp_report;
! echo "`date`"
update tmp_report set trpt_locus_acc = NULL where trpt_locus_acc = '';
update tmp_report set trpt_locus_acc = trpt_acc where trpt_locus_acc is NULL;
update tmp_report set trpt_alt_id = NULL where trpt_alt_id = '';
update tmp_report set trpt_alt_id = trpt_locus_acc where trpt_alt_id is NULL;

create index tmp_report_trpt_acc_idx on tmp_report (trpt_acc ) in idxdbs1 ;
create index tmp_report_trpt_alt_id_idx on tmp_report (trpt_alt_id )in idxdbs1 ;
create index tmp_report_trpt_acc_len_idx on tmp_report (trpt_acc_len )in idxdbs1 ; -- cheap and might help speed up accession bank update.

update statistics high for table tmp_report;
------------------------------------------------------------------
create table tmp_hit (
    thit_rpt             varchar(30), --;;; : BC146618.1
    thit_order           integer,     --;;; : 0
    thit_acc             varchar(30), --;;; : "BC116508"
    thit_acc_db          varchar(10), --;;; : "gb"
    thit_acc_type        varchar(20), --;;; : "nucleotide|protein|transcript --- RNA|DNA|[t|r|m|u]RNA|RNA ...EST GSS WGS ...
    thit_defline         lvarchar,    --;;; : {BC116508.1|BC116508 Danio rerio zgc     ;;;; :136342, mRNA (RNA clone MGC     ;;;; :136342 IMAGE     ;;;; :8128252), complete cds Length = 2014}
    thit_species         varchar(15), --;;; : "Danio rerio"
    thit_acc_len         integer,     --;;; : 0
    thit_score           integer,     --;;; : "5874"
    thit_bits            decimal,     --;;; : "887.4"
    thit_expect          float,       --;;; : "0."
    thit_probtype        varchar(20), --;;; : ""  [P | Sum P(N)]
    thit_prob            float,       --;;; : "0."
    thit_identites_num   integer,     --;;; : "1188"
    thit_identites_denom integer,     --;;; : "1197"
    thit_positives_num   integer,     --;;; : "1188"
    thit_positives_denom integer,     --;;; : "1197"
    thit_strand          varchar(5), --;;; : ""
    thit_alignment       lvarchar(30400)     --;;;; : {(99%), Strand = Plus / Plus ...
  --thit_target_id       (accession_bank_id)
  --thit_zad
) fragment by round robin in tbldbs1,tbldbs2,tbldbs3;

! echo "Import hit table `date`"
load from 'hit.unl' insert into tmp_hit;
! echo "Import hit table `date`"

create index tmp_hit_thit_rpt_idx on  tmp_hit(thit_rpt)in idxdbs3;
create index tmp_hit_thit_acc_idx on  tmp_hit(thit_acc)in idxdbs3;

update statistics high for table tmp_hit;

! echo "filter hits to self (should not be any)"
delete from tmp_hit where thit_rpt = thit_acc;

! echo "this is a stop-gap on loading old runs srt >tpe| deflines to transcripts"
update tmp_hit set thit_acc_type = (
	select case
		when trun_name[1,4] in ('Vega','ZGC_')
         and thit_acc_db = 'tpe' then 'transcript'
		else trun_target_type
	end
	from tmp_run
) where (thit_acc_type is NULL or thit_acc_type = "")
;

update tmp_hit set thit_acc_type = trim(thit_acc_type);

update statistics high for table tmp_hit;
----------------------------------------------------------------------------------
! echo "move run to schema table"
insert into zdb_active_data select trun_zad from tmp_run;

insert into run (
    run_zdb_id,
    run_name,
    run_program,
    run_blastdb,
    run_date,
    run_type,
    --run_version,
    --run_nomen_pub_zdb_id,
    run_relation_pub_zdb_id

) select
    trun_zad,
    trun_name,
    trun_program,
    trun_database,
    extend(trun_datetime),
    case
        when runprog_target_type = 'n' then 'Redundancy'
        when runprog_target_type = 'p' then 'Nomenclature'
    end,
    case
        when trun_name like 'Vega_%'    then 'ZDB-PUB-030703-1'
        when trun_name like 'ZGC_%'     then 'ZDB-PUB-040217-1'
        when trun_name like 'UniProt_%' then 'ZDB-PUB-030905-1'
        else NULL
    end
 from tmp_run, run_program
 where trun_program = runprog_program
;

! echo "fix trailing space from case statment"
update run set run_type = trim(run_type)
 --where octet_length(run_type) != length(run_type)
 ;

-- fix trailing space from case statment (just in case)
update run set run_nomen_pub_zdb_id= trim(run_nomen_pub_zdb_id)
 --where run_nomen_pub_zdb_id != trim(run_nomen_pub_zdb_id)
 ;

update run set run_relation_pub_zdb_id = trim(run_relation_pub_zdb_id)
 --where run_relation_pub_zdb_id != trim(run_relation_pub_zdb_id)
 ;

----------------------------------------------------------------------------------

! echo "add the accession bank id for the query  to the record (if it exists)"
! echo "`date`"
alter table tmp_report add trpt_query_id varchar(50);

update tmp_report
 set trpt_query_id = (
    select accbk_pk_id
     from accession_bank,tmp_run
     where trpt_acc = accbk_acc_num
 )where exists (
    select 1 from accession_bank, tmp_run
     where trpt_acc = accbk_acc_num
);

create index tmp_report_trpt_query_idx on tmp_report(trpt_query_id) in idxdbs2;

update statistics high for table tmp_report;


! echo "update accession_bank with NULL length"
! echo "`date`"
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

! echo "update accession_bank with NEW length  *** ACTION SKIPPED FOR EXCESSIVE TIME ***"
! echo "`date`"
--update accession_bank set accbk_length = (
--    select trpt_acc_len
--     from tmp_report
--     where trpt_query_id = accbk_pk_id
--) where accbk_length is NOT NULL
--    and exists (
--    select 1 from tmp_report
--     where trpt_query_id = accbk_pk_id
--       and trpt_acc_len > 0
--       and trpt_acc_len != accbk_length
--);


! echo "clobber existing VEGA accession_bank deflines if they are changed"
! echo "`date`"
update accession_bank set accbk_defline = (
    select trpt_defline
     from tmp_report
     where trpt_query_id = accbk_pk_id
)
 where accbk_acc_num[1,6] = 'OTTDAR'
   and exists(
    select 1 from tmp_report
     where trpt_query_id = accbk_pk_id
       and trpt_defline is not NULL
       and trpt_defline != accbk_defline
);

! echo "how many Queries still need to be added to accession bank?"
select count(*) howmany from tmp_report where trpt_query_id is NULL;

! echo "add new *QUERIES* to accession_bank"
! echo "`date`"
insert into  accession_bank (
    accbk_acc_num ,
    accbk_length ,
    --accbk_pk_id ,
    accbk_fdbcont_zdb_id ,
    accbk_defline
)
select distinct
    trpt_acc,      -- BC146618
    trpt_acc_len,  -- 3498
    case  -- all QUERIES are assumed to be zebrafish
        when trpt_acc_type = 'protein'
         and trpt_acc_db = 'ref'  then 'ZDB-FDBCONT-040412-39'
        when trpt_acc_type = 'protein'
         and trpt_acc_db = 'gb'   then 'ZDB-FDBCONT-040412-42'
        when trpt_acc_type = 'protein'
         and trpt_acc_db = 'sp'   then 'ZDB-FDBCONT-040412-47'
        when trpt_acc_type = 'nucleotide'
         and trpt_acc_db = 'ref'  then 'ZDB-FDBCONT-040412-38'
        when trpt_acc_type = 'nucleotide'
         and trpt_acc_db = 'gb'   then 'ZDB-FDBCONT-040412-37'
        when trpt_acc_type = 'transcript'
         and trpt_acc_db = 'tpe'  then 'ZDB-FDBCONT-050210-1' --TODO: should be internal prevega
        else trun_query_fdbcont
    end,
    trpt_defline
 from tmp_report, tmp_run
 where trpt_query_id is NULL
;
-- clean up after the SQL's superior conditional operator skillz
update accession_bank set accbk_fdbcont_zdb_id = trim (accbk_fdbcont_zdb_id)
 where exists (select 1 from tmp_report where accbk_acc_num = trpt_acc)
   and octet_length(accbk_fdbcont_zdb_id) != length(accbk_fdbcont_zdb_id)
;

update statistics high for table accession_bank;

! echo "add the new accession bank ids back in to the queries"
! echo "`date`"
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

update statistics high for table tmp_report;

-----------------------------------------------------------
! echo "add the candidate id to the record (if it exists)"
! echo "`date`"
alter table tmp_report add trpt_cnd_zdbid varchar(50);

update tmp_report
 set trpt_cnd_zdbid = (
    select cnd_zdb_id
     from candidate
     where trpt_alt_id = cnd_suggested_name
 )where exists (
    select 1 from candidate
     where trpt_alt_id = cnd_suggested_name
 );

! echo "how many Candidates still need to be added?"
select count(*) howmany from tmp_report where trpt_cnd_zdbid is NULL;

! echo "create new candidates if not seen before"
! echo "`date`"
create table tmp_candidate (
	tcnd_zad           VARCHAR(50),
--	tcnd_is_problem       BOOLEAN,
--	tcnd_mrkr_zdb_id      VARCHAR(50),
 	tcnd_mrkr_type        VARCHAR(10), --GENEP
--	tcnd_run_count        INTEGER,
--	tcnd_last_done_date   DATETIME YEAR TO FRACTION(5),
	tcnd_suggested_name   VARCHAR(60)--,
--	tcnd_note             LVARCHAR DEFAULT
) fragment by round robin in tbldbs1,tbldbs2,tbldbs3;

insert into tmp_candidate (tcnd_suggested_name,tcnd_mrkr_type)
select distinct trpt_alt_id,
 case
 	when trpt_defline like "%pesudogene%" then 'GENEP'
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
    --cnd_is_problem,
    --cnd_mrkr_zdb_id,
    cnd_mrkr_type,       -- gene /pseudogene
    cnd_run_count,       -- 1
    --cnd_last_done_date,
    cnd_suggested_name
    --cnd_mrkr_note
)
select distinct
    tcnd_zad,
    tcnd_mrkr_type,   ---GENE|GENEP
    '-1',
    tcnd_suggested_name
 from tmp_candidate
;

! echo "fix trailing space from case statment"
update candidate set cnd_mrkr_type = trim(cnd_mrkr_type)
 --where cnd_mrkr_type != trim(cnd_mrkr_type)
 ;

! echo "aassociate the new candidates with their reports"
update tmp_report set trpt_cnd_zdbid = (
    select tcnd_zad
    from tmp_candidate
     where tcnd_suggested_name = trpt_alt_id
)
 where trpt_cnd_zdbid is NULL
;

----------------------------------
! echo "next make run_candidates"
! echo "`date`"


create table tmp_run_cnd (
    truncan_zad       varchar(50) ,
    truncan_run_zdb_id   varchar(50) ,
    truncan_cnd_zdb_id   varchar(50)
--    truncan_done         boolean default 'f' ,
--    truncan_locked_by    varchar(50)
)fragment by round robin in tbldbs1,tbldbs2,tbldbs3;

insert into  tmp_run_cnd (truncan_run_zdb_id, truncan_cnd_zdb_id)
 select  distinct trun_zad, trpt_cnd_zdbid
  from tmp_run, tmp_report
;

update tmp_run_cnd set truncan_zad = get_id('RUNCAN');

-- not putting runcan_zdb_id in active data at this point. - S.M.
--insert into zdb_active_data select runcan_zad from  tmp_run_cnd;

insert into run_candidate (
    runcan_zdb_id,
    runcan_run_zdb_id,
    runcan_cnd_zdb_id
--    runcan_done         boolean default 'f' ,
--    runcan_locked_by    varchar(50)
)
select * from  tmp_run_cnd
;

--------------------------------
! echo "next make blast_query"
! echo "`date`"

create table tmp_blast_query (
    tbqry_zad varchar(50) ,
    tbqry_runcan_zdb_id varchar(50),
    tbqry_accbk_pk_id int
) fragment by round robin in tbldbs1,tbldbs2,tbldbs3;

insert into tmp_blast_query (
    tbqry_runcan_zdb_id,
    tbqry_accbk_pk_id
)
select distinct truncan_zad, trpt_query_id
 from tmp_report, tmp_run_cnd
 where truncan_cnd_zdb_id =  trpt_cnd_zdbid -- ? sufficent ?
   and trpt_query_id is not NULL
;

update tmp_blast_query set tbqry_zad = get_id('BQRY');

--insert into zdb_active_data select tbqry_zad from tmp_blast_query;


insert into blast_query (
    bqry_zdb_id,
    bqry_runcan_zdb_id,
    bqry_accbk_pk_id
)
select * from tmp_blast_query where tbqry_runcan_zdb_id is not NULL;

create index tmp_blast_query_tbqry_accbk_pk_idx
    on tmp_blast_query(tbqry_accbk_pk_id) in idxdbs1;

update statistics high for table tmp_blast_query;

-----------------------------------------
! echo "next make blast_report"
! echo "`date`"
alter table tmp_report add trpt_brpt_zad varchar(50);
update tmp_report set trpt_brpt_zad = get_id('BRPT');

--insert into zdb_active_data select trpt_brpt_zad from tmp_report;

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

update statistics high for table blast_report;
---------------------------------------------------------------------
{ just continue to use the accession as FK }
! echo "add the accession bank id for the target to tmp_hit (if it exists)"
! echo "`date`"
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
    on tmp_hit(thit_target_id) in idxdbs1;

update statistics high for table tmp_hit;


! echo "update accession_banks with NULL length if any is available"
! echo "`date`"
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

! echo "update not null accession_banks length if new one is available"
! echo "`date`"
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


! echo "update existing NULL accession_bank defline if one is available"
! echo "`date`"
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

! echo "how many distinct Hit still need to be added to accession bank? (mind the dups)"
! echo "`date`"
select count(thit_acc) all_told,count(distinct thit_acc) howmany from tmp_hit where thit_target_id is NULL;


-- DEBUG
{
select thit_acc,accbk_fdbcont_zdb_id,thit_target_id
 from tmp_hit, accession_bank
 where thit_acc =  accbk_acc_num
   and (thit_target_id IS NULL OR accbk_pk_id != thit_target_id)
 ;
}

{
select thit_acc,thit_acc_len, thit_defline
 from tmp_hit
 where thit_acc in (
 	select thit_acc
 	 from tmp_hit
 	  where thit_target_id IS NULL
 	 group by 1 having count(*) > 1
) order by 1,2;
}



-- TODO: swap the selects for the hardcoded FDBCONT after it is stabalized

! echo "add new *TARGETS* to accession_bank"
! echo "`date`"
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
         and thit_acc_db = 'ref'           then 'ZDB-FDBCONT-040412-39'
        when thit_acc_type = 'protein'
         and thit_acc_db = 'gb'            then 'ZDB-FDBCONT-040412-42'
        when thit_acc_type = 'protein'
         and thit_acc_db = 'sp'
--       and thit_species= 'Homo sapiens' then 'ZDB-FDBCONT-071023-3'
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
--       and thit_species= 'Mus musculus'  then 'ZDB-FDBCONT-071023-2'
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
         and thit_species= 'Danio rerio'   then 'ZDB-FDBCONT-040412-47'
        when thit_acc_type = 'nucleotide'
         and thit_acc_db = 'ref'           then 'ZDB-FDBCONT-040412-38'
        when thit_acc_type = 'nucleotide'
         and thit_acc_db = 'wz'            then 'ZDB-FDBCONT-040412-31'
        when thit_acc_type = 'nucleotide'
         and thit_acc_db = 'gb'            then 'ZDB-FDBCONT-040412-37'
        when thit_acc_type = 'transcript'
         and thit_acc_db = 'tpe'           then 'ZDB-FDBCONT-060417-1' -- vega at sanger
        else trun_target_fdbcont
    end ),
    max(thit_defline)
 from tmp_hit, tmp_run
 where thit_target_id is NULL
 group by thit_acc
;

update statistics high for table accession_bank;

-- clean up after the SQL's superior conditional operator skillz
update accession_bank set accbk_fdbcont_zdb_id = trim (accbk_fdbcont_zdb_id)
 where exists (
 	select 1 from tmp_hit
 	 where accbk_acc_num = thit_acc
 ) and octet_length(accbk_fdbcont_zdb_id) != length (accbk_fdbcont_zdb_id)
;

! echo "add this accession length if it is > 0 and accbk_length = 0"
! echo "`date`"
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


! echo "add the new accession bank ids back into tmp_hit"
! echo "`date`"
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

update statistics high for table tmp_hit;
-------------------------------
! echo "Finally make blast_hit"
! echo "`date`"

alter table tmp_hit add thit_zad varchar(50);
update tmp_hit set thit_zad = get_id('BHIT');
--insert into zdb_active_data select thit_zad from tmp_hit;

! echo "loading alignments takes a while `date`"
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
-- this DISTINCT really slows things down, especially large nomenclature loads
-- where so far, it has not been needed. nor has it been needed for Vega or ZGC
-- loads to date.  *BUT* it has been needed for at least one 'paper' load of RNA
-- for now I am going to enable it only if need be for a particular load
select --DISTINCT -- ***
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

! echo "alignments loaded `date`"

-----------------------------------------------------------
-----------------------------------------------------------
-----------------------------------------------------------


! echo "just checking"
select count(*) run from run;
select count(*) candidate from candidate;
select count(*) runcan from run_candidate;
select count(*) blstqry from blast_query;
select count(*) blstrpt from blast_report;
select count(*) hit from blast_hit;


-- TODO:
--  make a seperate script (in it's own transaction) to:
--  create novle genes where there is not enough information to say otherwise

-- find candidates with insufficent data
-- no hits
{
select trpt_locus_acc no_hit
 from tmp_report,tmp_run
 where trun_program = 'BLASTN'
   and trun_name[1,4] in ('Vega','ZGC_')
   and not exists (
 	select 1 from tmp_hit
 	 where thit_rpt = trpt_acc
);
}
--  or, only hits to *** NONE *** across *all* their queries.
{
select trpt_locus_acc none_hit
 from tmp_report,tmp_run,tmp_hit a
 where trun_program = 'BLASTN'
   and trun_name[1,4] in ('Vega','ZGC_')
   and a.thit_rpt = trpt_acc
   and a.thit_acc is NULL
   and not exists (
   	select 1 from tmp_hit b
   	 where a.thit_rpt = b.thit_rpt
   	 and b.thit_acc is NOT NULL
);
}

{
select accbk_acc_num hit ,accbk_length len,accbk_fdbcont_zdb_id fdbcont
 from accession_bank, tmp_hit
 where accbk_acc_num = thit_acc
   and accbk_fdbcont_zdb_id is NULL
;
}


drop table tmp_run;
drop table tmp_report;
drop table tmp_hit;
drop table tmp_candidate;
drop table tmp_run_cnd;
drop table tmp_blast_query;
--drop table ;

! echo "FINISHED `date`"


--rollback/commit applied externally

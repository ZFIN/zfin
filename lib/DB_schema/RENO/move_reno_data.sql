begin work ;

alter table blast_hit
  add (runcanh_zdb_id varchar(50));

alter table blast_hit
  add (bh_db_t varchar(30));

alter table blast_hit
 add (bh_type_t varchar(30));

alter table blast_hit
  add (bh_db_q varchar(30));

alter table blast_hit
 add (bh_type_q varchar(30));

delete from accession
  where acc_accession = 'none' ;

alter table report
  add (rpt_zdb_id varchar(50));


alter table candidateR
  add (cndR_zdb_id varchar(50)) ;

update candidateR
  set cndR_zdb_id = get_id('CND') ;

--runR

alter table candidate
  add (cnd_accbk_pk_id int);

alter table runR
  add (runR_zdb_id varchar(50)) ;

update runR
  set runR_zdb_id = get_id('RUN');

--hit

alter table hit
 add (hit_zdb_id varchar(50));

update hit
  set hit_zdb_id = get_id('BHIT');

--report

alter table report
  add (report_zdb_id varchar(50)) ;

--auth

alter table auth
  add (auth_zdb_id varchar(50));

update auth
  set auth_zdb_id = (select zdb_id
      		       from zdb_submitters
		       where login = username);

alter table accession
  add (acc_zdb_id varchar(50));

update accession
  set acc_zdb_id = (select accbk_pk_id
      		       from accession_bank, foreign_db_contains
		       where accbk_fdbcont_zdb_id = fdbcont_zdb_id
		       and acc_accession = accbk_acc_num
		       and fdbcont_fdb_db_name = acc_lnk_db);


set constraints all deferred ;

update runr
  set runr_program = 'BLASTN'
  where runr_program like 'BLA%STN' or runr_program like 'BLAS%TN';

update runr
  set runr_program = 'BLASTP'
  where runr_program like 'BLAS%TP' ;

insert into run (run_zdb_id, 
		 run_name,
		 run_program,
		 run_blastdb,
		 run_date,
		 run_version)
 select runr_zdb_id,
 	runr_name,
	runr_program,
	runr_blast_db,
	runr_datetime,
	runr_version
   from runr ;

update run
  set run_date = (select current year to day from single)
   where lower(run_name) like 'zgc%';

--select distinct cndr_zdb_id from candidateR;


insert into candidate (cnd_zdb_id,
		       cnd_accbk_pk_id,
		       cnd_mrkr_zdb_id,
		       cnd_mrkr_type,
		       cnd_last_done_date)
select distinct cndR_zdb_id,
       accbk_pk_id,
       dblink_linked_recid,
       'GENE',
       current year to day
  from candidateR, 
       accession_bank,
       db_link,
       report, 
       query_acc, 
       accession
   where cnd_id = rpt_cnd_id
     and rpt_id = qryacc_rpt_id
     and accbk_acc_num = acc_accession
     and qryacc_acc_id = acc_id
     and dblink_acc_num = acc_accession 
     and exists (select 'x'
     	 		from db_link a
			where a.dblink_acc_num = acc_accession);

!echo "cnd_mrkr_zdb_id is null" ;

select * from candidate
 where cnd_mrkr_zdb_id is null ;


create temp table tmp_cnd_dups (counter int, id varchar(50))
with no log ;

insert into tmp_cnd_dups (counter, id)
select count(*) as counter, cnd_zdb_id
  from candidate
  group by cnd_zdb_id
  having count(*) > 1 ;

delete from zdb_active_data
  where exists (select 'x'
  	          from tmp_cnd_dups
		  where zactvd_zdb_id = id);

delete from candidate 
    where exists (select 'x'
  	          from tmp_cnd_dups
		  where cnd_zdb_id = id);

create temp table tmp_cnd_gene (cnd_id varchar(50), 
       	    	  	       	accbk_id varchar(50), 
				dblink_id varchar(50),
				related_gene varchar(50)
				)
with no log ;

!echo "how many inserted into tmp_cnd_gene for gene candidates?";

insert into tmp_cnd_gene
  select cndR_zdb_id,
       accbk_pk_id,
       dblink_linked_recid, 'none'
  from candidateR, 
       accession_bank,
       db_link,
       report, 
       query_acc, 
       accession
   where cnd_id = rpt_cnd_id
     and rpt_id = qryacc_rpt_id
     and accbk_acc_num = acc_accession
     and qryacc_acc_id = acc_id
     and dblink_acc_num = acc_accession 
     and exists (select 'x'
     	 		from db_link a
			where a.dblink_acc_num = acc_accession)
     and exists (Select 'x'
     	     	    	from tmp_cnd_dups
			where id = cndR_zdb_id);

!echo "Sierra HERE" ;
-----SIERRA HERE-----------------------
--insert into candidate (cnd_zdb_id, cnd_accbk_pk_id, cnd_mrkr_zdb_id, cnd_last_done_date)
--  select cndR_zdb_id,
--       accbk_pk_id,
--       a.acc_accession,
--       current year to day
--  from candidateR,
--       report, 
--       query_acc, 
--       accession a,
--       accession b,
--       accession_bank,
--       cnd_acc
--   where cnd_id = rpt_cnd_id
--     and rpt_id = qryacc_rpt_id
--     and a.acc_id = cndacc_acC_id
--     and cnd_id = cndacc_cnd_id
--     and not exists (Select 'x' 
--     	     	    	    from accession_bank
--			    where accbk_acc_num = b.acc_accession)
--     and qryacc_acc_id = b.acc_id
--    and not exists (select 'x'
 --    	 		from db_link b
--			where b.dblink_acc_num = a.acc_accession)
--     and not exists (Select 'x'
--     	     	    	from candidate
--			where cndR_zdb_id = cnd_zdb_id);


!echo "how many related genes from dup candidates?" ;

update tmp_cnd_gene
  set related_gene = (select mrel_mrkr_1_zdb_id
      		        from marker_relationship
			where mrel_mrkr_2_zdb_id = dblink_id);

select * from tmp_cnd_gene where related_gene is null;

delete from candidater
 where cndr_zdb_id in (Select cnd_id from tmp_cnd_gene);

update tmp_cnd_gene
  set cnd_id = get_id('CND')
  where related_gene is null;



update tmp_cnd_gene
  set related_gene = dblink_id 
  where related_gene is null ;

insert into candidate (cnd_zdb_id,
       	    	       cnd_mrkr_zdb_id,
		       cnd_mrkr_type,
		       cnd_last_done_date)
select distinct cnd_id, 
       		related_gene, 
		get_obj_type(related_gene), 
		current year to day
   from tmp_cnd_gene 
   where not exists (select 'x'
    	    	      from candidate
		      where cnd_id = cnd_zdb_id);

select * from tmp_cnd_gene ;

!echo "SIERRA!!! how many dups in candidate" ;
select count(*), cnd_zdb_id
  from candidate
  group by cnd_zdb_id
  having count(*) > 1;

--update candidate 
--  set cnd_suggested_name = (select mrkr_abbrev 
--      			      from marker
--			      where mrkr_zdb_id = cnd_mrkr_zdb_id);

alter table candidateR
  add (cnd_name varchar(60));

--suggested name is only for novel genes

update candidateR
  set cnd_name = (select acc_display
      	       	    from accession, cnd_acc
		    where cndacc_acc_id = acc_id
		    and cndacc_cnd_id = cnd_id
		    and acc_lnk_db = "NovelGene");

!echo "count of null cnd_name" ;
select * from accession, cnd_acc, candidater
  where cndacc_acc_id = acc_id
  and cndacc_cnd_id = cnd_id 
   and cndr_zdb_id = 'ZDB-CND-070905-2'
   and cnd_name is null ;

update candidate
  set cnd_suggested_name = (select cnd_name 
      			   	   from candidateR 
				   where cndr_zdb_id = cnd_zdb_id);

!echo "how many candidates with null mrkr, and cnd_suggested_name";
select * from candidate
where cnd_suggested_name is null
and cnd_mrkr_zdb_id is null ;

---non-dblink ones.


update accession
  set acc_lnk_db = 'Entrez Gene'
  where acc_lnk_db= 'ENTREZ' ;

update accession
  set acc_lnk_db = 'GenBank'
  where acc_lnk_db= 'Genbank' ;

update accession
  set acc_lnk_db = 'GenBank'
  where acc_lnk_db= 'Genbank' ;

update accession
  set acc_lnk_db = 'Vega_Trans'
  where acc_lnk_db= 'VTRAN' ;

update accession
  set acc_lnk_db = 'UniProt'
  where acc_lnk_db= 'SWISS-PROT' ;

!echo "NOVEL" ;

update accession
  set acc_lnk_db = 'NovelGene'
  where acc_lnk_db= 'NONE' ;

update accession
  set acc_lnk_db = 'PreVega'
  where acc_lnk_db = 'VEGA' ;

alter table accession 
  add (acc_type varchar(40));

update accession
  set acc_type = 'cDNA'
  where acc_lnk_db ='GenBank';

update accession
  set acc_type = 'Vega Transcript'
  where acc_lnk_db ='PreVega';

!echo "This is the vega_trans" ;

update accession
  set acc_type = 'Vega Transcript'
  where acc_lnk_db ='Vega_Trans';

!echo "NOVEL" ;

update accession
  set acc_type = 'other'
  where acc_lnk_db ='NovelGene';

update accession
  set acc_type = 'Polypeptide'
  where acc_lnk_db in ('GenPept','UniProt', 'UniGene');

update accession
  set acc_type = 'other'
  where acc_lnk_db in ('ZFIN','Entrez Gene');

select distinct acc_lnk_db from accession
  where acc_type is null ;

update accession
  set acc_species = 'Human'
  where acc_species = "Homo Sapiens";


update accession
  set acc_species = 'Human'
  where acc_species = "Homo sapiens";

update accession
  set acc_species = 'Mouse'
  where acc_species = "Mus musculus";

update accession
  set acc_species = 'Zebrafish'
  where acc_species = "Danio rerio";


select distinct acc_species from accession ;

insert into accession_bank (
				accbk_acc_num,
				accbk_length,
				accbk_fdbcont_zdb_id,
				accbk_defline)
  select 
         acc_accession,
	 acc_length,
	 (select fdbcont_zdb_id
	    from foreign_db_contains
	    where fdbcont_fdb_db_name = acc_lnk_db
	    and fdbcont_fdbdt_data_type = acc_type
	    and fdbcont_organism_common_name = acc_species
	    ),
	 acc_defline
    from accession
    where acc_lnk_db != 'ZFIN'
    and acc_lnk_db != 'RBH'
    and acc_lnk_db != 'none' 
     and not exists (select 'x'
     	     	    	from accession_bank, foreign_db_contains
			where accbk_acc_num = acc_accession
			and accbk_fdbcont_zdb_id = fdbcont_zdb_id)
   and acc_accession != 'none';

!echo "Sierra accession BANK" ;
select count(*) from accession
  where not exists (Select 'x'
  	    	   	   from accession_bank
			   where accbk_acc_num = acc_accession) ;

insert into candidate (cnd_zdb_id,
		       cnd_accbk_pk_id,
		       cnd_last_done_date)
  select distinct cndr_zdb_id, 
  	 accbk_pk_id,
	 current year to day
    from candidateR,  
    	 cnd_acc,
	 accession, accession_bank
    where cnd_id = cndacc_cnd_id
    and cndacc_acc_id = acc_id
    and not exists (select 'x'
    	    	      from candidate
		      where cndr_zdb_id = cnd_zdb_id)
    and acc_accession = accbk_acc_num  ;


!echo "SIERRA first cnd" ;
select * from candidate 
where cnd_zdb_id = 'ZDB-CND-070906-186';

!echo "another cnd" ;
insert into candidate (cnd_zdb_id,
       	    	       cnd_accbk_pk_id,
		      cnd_mrkr_zdb_id,
		       cnd_last_done_date)
  select distinct cndr_zdb_id, 
  	 accbk_pk_id,
  	a.acc_accession,
	 current year to day
    from candidateR,  
    	 cnd_acc,
	accession b, 
	 accession_bank,
	 report,
	 query_acc,
	accession a
    where cnd_id = cndacc_cnd_id
    and not exists (select 'x'
    	    	      from candidate
		      where cndr_zdb_id = cnd_zdb_id)
    		      
    and b.acc_accession = accbk_acc_num
    and cndacc_acc_id = a.acc_id
    and rpt_cnd_id = cnd_id
    and qryacc_rpt_id = rpt_id
    and qryacc_acc_id = b.acc_id;

!echo "how many candidates with null mrkr, and cnd_suggested_name";
select count(*) from candidate
  where cnd_mrkr_zdb_id is null 
  and cnd_suggested_name is null ;


select *
  from candidate
  where cnd_zdb_id = 'ZDB-CND-070906-186' ;

--select * from candidate
-- where cnd_mrkr_zdb_id  = 'ZDB-GENE-040718-60' ;

select * from candidateR, accession, cnd_acc
 where acc_id = cndacc_acc_id
 and cndacc_cnd_id = cnd_id
and cndr_zdb_id in ( 'ZDB-CND-070711-173',
    		    'ZDB-CND-070711-159',
		    'ZDB-CND-070711-162');

select * from candidate, accession_bank
where cnd_zdb_id in ( 'ZDB-CND-070711-173',
    		    'ZDB-CND-070711-159',
		    'ZDB-CND-070711-162')
and cnd_accbk_pk_id = accbk_pk_id;

--add names.

update run
  set run_default_nomen_pub_zdb_id = 'ZDB-PUB-040217-1'
  where run_name like 'ZGC%';

update run
  set run_default_nomen_pub_zdb_id = 'ZDB-PUB-030703-1'
  where run_name like 'Vega%' ;

update run
  set run_default_nomen_pub_zdb_id = 'ZDB-PUB-030508-1'
  where run_default_nomen_pub_zdb_id is null ;

insert into zdb_active_data
  select distinct cnd_zdb_id from candidate ;

insert into zdb_Active_data
  select distinct run_zdb_id from run ;

!echo "candidate count" ;
select count(*) from candidate ;

!echo "number candidates not in candidate" ;
select first 10 * from candidateR
where cndr_zdb_id not in (select cnd_zdb_id from candidate);


select distinct cnd_mrkr_zdb_id
  from candidate
  where not exists (select 'x'
  	    	   	   from marker
			   where mrkr_zdb_id = cnd_mrkr_zdb_id);


select first 10 * from accession
  where not exists (Select 'x'
  	    	   	   from accession_bank
			   where accbk_acc_num = acc_accession)
  and acc_accession not like 'ZDB-%';


--unload to candidate_check
-- select * from candidate ;

-------------------------------------------------------
insert into run_candidate (
			    runcan_run_zdb_id,
			    runcan_cnd_zdb_id,
			    runcan_locked_by)
 select distinct runr_zdb_id, 
 		 cndR_zdb_id, rpt_curator
   from runR, candidateR, report
   where runr_id = rpt_run_id
     and cnd_id = rpt_cnd_id ;

update run_candidate
  set runcan_zdb_id = get_id('RUNCAN');


!echo "SHOULD BE 300" ;


insert into blast_query (bqry_zdb_id, 
       	    		 bqry_runcan_zdb_id, 
			 bqry_accbk_pk_id)
  select get_id('BQRY'),
  	 runcan_zdb_id,
	 accbk_pk_id
    from run_candidate, 
    	 accession_bank, 
	 query_acc,
    	 accession, 
	 report, 
	 candidateR, 
	 foreign_db_contains,
	 runR
    where runcan_run_zdb_id = runr_zdb_id
    and runcan_cnd_zdb_id = cndr_zdb_id
    and runr_id = rpt_run_id
    and cnd_id = rpt_cnd_id
    and acc_id = qryacc_acc_id
    and qryacc_rpt_id = rpt_id 
    and accbk_acc_num = acc_accession 
    and fdbcont_fdb_db_name = acc_lnk_db
    and fdbcont_fdbdt_data_type = acc_type;

--insert into blast_report (brpt_zdb_id, brpt_exitcode, brpt_detail_header, brpt_runcan_zdb_id)
 -- select get_id('BRPT'), rpt_exitcode, rpt_details, runcan_zdb_id
 --  from report, run_candidate, candidate, candidateR
 --  where rpt_cnd_id = cnd_id
 --  and cnd_zdb_id = cndR_zdb_id
 --  and runcan_cnd_zdb_id = cnd_zdb_id ;

delete from accession_bank
  where accbk_acc_num = 'none' ;

--select count(*), brpt_zdb_id 
--  from blast_report
--  group by brpt_zdb_id
--  having count(*) > 1;

!echo "SHOULD be 2325" ;

update statistics high for table run; 

update statistics high for table candidate;

update statistics high for table run_candidate; 

update statistics high for table blast_query; 

update statistics high for table blast_hit ;

--select count(*), accbk_fdbcont_zdb_id, accbk_acc_num
--  from accession_bank
--  group by accbk_fdbcont_zdb_id, accbk_acc_num
--  having count(*) > 1;


create unique index cnd_index on candidateR (cnd_id) using btree in idxdbs1;

create unique index trg_index on target_acc (trgtacc_hit_id, trgtacc_acc_id) using btree in idxdbs2;

--set constraints all immediate ;


--set constraints all deferred ;

select distinct acc_lnk_db from accession ;

select distinct acc_type from accession ;

alter table blast_hit
  modify (bhit_target_accbk_pk_id varchar(50));

insert into blast_hit (
		       runcanh_zdb_id,
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
		       bhit_alignment,
		       bh_db_q,
		       bh_type_q,
		       bh_db_t,
		       bh_type_t
)
  select
	 runcan_zdb_id,
  	 a.acc_accession,
	 b.acc_accession,
	 hit_order,
	 hit_score,
	 hit_expect,
	 hit_prob,
	 hit_positives_num,
	 hit_positives_denom,
	 hit_identites_num,
	 hit_identites_denom,
	 hit_strand,
	 hit_alignment,
	 a.acc_lnk_db,
	 a.acc_type,
	 b.acc_lnk_db,
	 b.acc_type
  from query_acc, target_acc, report, hit, accession a, accession b, runr, candidateR, run_candidate
where rpt_id = qryacc_rpt_id
  and rpt_run_id = runr_id
  and rpt_cnd_id = cnd_id
  and runr_zdb_id = runcan_run_zdb_id
  and cndr_zdb_id = runcan_cnd_zdb_id
  and hit_rpt_id = rpt_id
  and trgtacc_hit_id = hit_id
  and a.acc_id = qryacc_acc_id
  and b.acc_id = trgtacc_acc_id 
  and b.acc_accession not like 'ZDB-%' ;

!echo "blast_hit COUNTER";
select count(*) from blast_hit;

--select * from accession where acc_accession = 'Q8C8U0' ;

--select * from accession_bank where accbk_acc_num = 'Q8C8U0';

--select * from accession_bank
--  where accbk_fdbcont_zdb_id is null ;

--select * from accession_bank
-- where accbk_acc_num = 'BC142600' ;


--!echo "runcan not in bqry?" ;
--select count(*) from blast_hit where 
--       runcanh_zdb_id not in (select bqry_runcan_zdb_id from blast_Query);


--!echo "bhit_bqry != accbk_acc_num?" ;

--select COUNT(*) from blast_hit, blast_query, accession_bank, foreign_db_contains
--  where bqry_accbk_pk_id = accbk_pk_id
--  and accbk_acc_num = bhit_bqry_zdb_id 
--  and fdbcont_zdb_id = accbk_fdbcont_zdb_id
--  and bh_db_q = fdbcont_fdb_db_name
-- and bh_type_q = fdbcont_fdbdt_data_type;


--select first 5 * from blast_hit, blast_query, accession_bank
 -- where bqry_accbk_pk_id = accbk_pk_id
 -- and accbk_acc_num = bhit_bqry_zdb_id 
-- and not exists (select 'x'
 --    	 		from foreign_db_contains
--			where fdbcont_zdb_id = accbk_fdbcont_zdb_id
--   			and bh_db_q = fdbcont_fdb_db_name
 --			and bh_type_q = fdbcont_fdbdt_data_type);


update blast_hit
  set bhit_bqry_zdb_id = (select bqry_zdb_id

      		       	    from blast_query,
 
			    	 accession_bank, 

				 foreign_db_contains

			    where bqry_runcan_zdb_id = runcanh_zdb_id

			    and accbk_acc_num = bhit_bqry_zdb_id

		    	    and bqry_accbk_pk_id = accbk_pk_id

			    and fdbcont_zdb_id = accbk_fdbcont_zdb_id

			    and bh_db_q = fdbcont_fdb_db_name

			    and bh_type_q = fdbcont_fdbdt_data_type)
where exists (select 'x'
      		       	    from blast_query, accession_bank, foreign_db_contains
			    where bqry_runcan_zdb_id = runcanh_zdb_id
			    and bqry_accbk_pk_id = accbk_pk_id
			    and accbk_acc_num = bhit_bqry_zdb_id
			    and fdbcont_zdb_id = accbk_fdbcont_zdb_id
			    and bh_db_q = fdbcont_fdb_db_name
			    and bh_type_q = fdbcont_fdbdt_data_type);



update blast_hit
  set bhit_target_accbk_pk_id = (select accbk_pk_id
      		       	       	 	 from accession_bank, foreign_Db_contains
			    		 where accbk_acc_num = bhit_target_accbk_pk_id
					 and bh_db_t = fdbcont_fdb_db_name
					 and bh_type_t = fdbcont_fdbdt_data_type
					 and accbk_fdbcont_zdb_id = fdbcont_zdb_id	 
)

 where exists (select 'x'
      		       	       	 	 from accession_bank, foreign_Db_contains
			    		 where accbk_acc_num = bhit_target_accbk_pk_id
					 and bh_db_t = fdbcont_fdb_db_name
					 and bh_type_t = fdbcont_fdbdt_data_type
					 and accbk_fdbcont_zdb_id = fdbcont_zdb_id	 
 );


update blast_hit
  set bhit_mrkr_zdb_id = (select dblink_linked_recid
      		       	       	 	 from db_link, accession_bank
			    		 where dblink_Acc_num = accbk_acc_num
					 and accbk_pk_id = bhit_target_accbk_pk_id
					 and dblink_fdbcont_zdb_id = accbk_fdbcont_zdb_id);


--select count(*), bhit_bqry_zdb_id, bhit_target_accbk_pk_id
--  from blast_hit
--  group by bhit_bqry_zdb_id, bhit_target_accbk_pk_id
--  having count(*) > 1;


update blast_hit
  set bhit_zdb_id = get_id('BHIT');


update candidate 
  set cnd_run_count = 1 
  where cnd_run_count is null ;

update run
  set run_type = "Redundancy"
  where run_name like 'ZGC%' ;

update run
  set run_type = "Redundancy"
  where run_name like 'Vega%' ;

update run
  set run_type = "Nomenclature"
  where run_name like 'UniProt%' ;

--drop table accession;
--drop table auth;
--drop table cnd_acc;
--drop table target_Acc;
--drop table query_acc;
--drop table orthology_acc;
--drop table link ;
--drop table entrez_accession;
--drop table candidateR;
--drop table runR;
--drop table report ;
--drop table hit ;

--delete from accession_bank
--  where accbk_defline = 'novel candidate';

update candidate
  set cnd_suggested_name = (select accbk_acc_num
      			      from accession_bank, foreign_db_contains
			      where accbk_pk_id = cnd_accbk_pk_id
			      and fdbcont_fdb_db_name = 'NovelGene'
                              and fdbcont_zdb_id = accbk_fdbcont_zdb_id)  
  where exists (select 'x' 
  	       	     from accession_bank, foreign_db_contains
		     where accbk_pk_id = cnd_accbk_pk_id
	             and fdbcont_fdb_db_name = 'NovelGene' 
		      and fdbcont_zdb_id = accbk_fdbcont_zdb_id
		     );

update candidate
  set cnd_accbk_pk_id = null ;

update candidate
  set cnd_mrkr_type = 'GENE';

set constraints all immediate ;

delete from accession_bank 
  where exists (Select 'x'
  	          from foreign_Db_contains
		  where fdbcont_zdb_id = accbk_fdbcont_Zdb_id
		  and fdbcont_fdb_db_name = 'NovelGene');

delete from foreign_db_contains
  where fdbcont_fdb_db_name = 'NovelGene' ;

delete from foreign_db
  where fdb_db_name = 'NovelGene' ;

alter table candidate
  drop cnd_accbk_pk_id ;

update candidate 
  set cnd_is_problem = 't'
  where cnd_mrkr_zdb_id like 'ZDB-CDNA-070615-3%' ;

update run_candidate
  set runcan_done = 't'
  where exists (select 'x' 
  	         from candidate
		 where cnd_zdb_id = runcan_cnd_zdb_id
		 and cnd_is_problem = 't');


update run
  set run_default_ortho_pub_zdb_id = 'ZDB-PUB-030905-1'
  where run_type='Nomenclature' ;

update candidate
  set cnd_suggested_name = (select mrkr_abbrev
      			     from marker
			     where mrkr_zdb_id = cnd_mrkr_zdb_id
			     and mrkr_abbrev like 'MGC:%')
  where cnd_suggested_name is null
  and cnd_mrkr_Zdb_id like 'ZDB-CDNA-%' ;

update candidate
  set cnd_suggested_name = replace (cnd_suggested_name, "MGC:","zgc:")
  where cnd_mrkr_zdb_id like 'ZDB-CDNA-%'
  and cnd_suggested_name like 'MGC:%';

update candidate 
  set cnd_mrkr_type = 'GENE' ;

create temp table tmp_deletes (id varchar(50))
with no log;

insert into tmp_deletes 
select runcan_zdb_id from run_candidate, blast_query
  where runcan_zdb_id = bqry_runcan_zdb_id
  and not exists (select 'x'
			from blast_hit
			where bhit_bqry_zdb_id = bqry_zdb_id);

delete from run_candidate
 where exists (Select 'x'
       	      	      from tmp_deletes
		      where id = 
		      runcan_zdb_id);

select count(*) from candidate, candidateR, cnd_acc, accession
  where cndr_zdb_id = cnd_zdb_id
  and cnd_suggested_name is null 
  and cnd_mrkr_zdb_id is null 
and cnd_id = cndacc_cnd_id
and cndacc_acc_id = acc_id;

alter table blast_hit
  drop runcanh_zdb_id ;

select first 1 * from candidate ;

--update candidate
--  set cnd_note = cnd_mrkr_comments ;

--alter table candidate
--  drop cnd_mrkr_comments ;

select count(*), cnd_zdb_id from candidate 
group by cnd_zdb_id 
having count(*)>1;

update candidate
  set cnd_mrkr_zdb_id = (select mrkr_zdb_id
      		      	   from marker
			   where cnd_suggested_name = mrkr_abbrev)
  where cnd_mrkr_zdb_id is null
  and cnd_suggested_name is not null
  and exists (Select 'x'
      	     	     from marker
		     where mrkr_abbrev = cnd_suggested_name);

update candidate
  set cnd_suggested_name = null
  where cnd_mrkr_zdb_id is not null
  and cnd_suggested_name is not null 
  and cnd_suggested_name not like 'zgc:%';

select count(*) from candidate ;

alter table blast_hit
  modify (bhit_target_accbk_pk_id int not null constraint bhit_target_accbk_pk_id_not_null);

alter table blast_hit
  drop bh_db_t ;

alter table blast_hit
  drop bh_type_t ;

alter table blast_hit
  drop bh_db_q;

alter table blast_hit
  drop bh_type_q ;


commit work ;
--rollback work; 
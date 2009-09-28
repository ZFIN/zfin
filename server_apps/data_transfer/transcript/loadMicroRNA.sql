begin work ;

create temp table tmp_load (tl_sangerName varchar(30),
       	    	  	   tl_accNum varchar(30),
			   tl_genus varchar(30),
			   tl_species varchar(30),
			   tl_nodre varchar(30),
			   tl_seq varchar(50))
with no log ;

load from matureFa.unl
  insert into tmp_load;

delete from tmp_load
  where tl_genus != "Danio";

delete from tmp_load
  where tl_accNum = '>';

create temp table tmp_t (t_zdb_id varchar(50),
       	    	  	t_nodreName varchar(50),
			t_accNum varchar(50),
			t_seq lvarchar)
 with no log ;
	
select first 3 * from tmp_load;
		
insert into tmp_t
  select get_id('TSCRIPT'),
  	 replace(tl_nodre, 'miR-','mirn'),
	 tl_accNum,
	 tl_seq
    from tmp_load;


create temp table tmp_loadH (tlh_sangerName varchar(30),
       	    	  	   tlh_accNum varchar(30),
			   tlh_genus varchar(30),
			   tlh_species varchar(30),
			   tlh_noDre varchar(30),
			   tlh_seq lvarchar)
with no log ;

load from matureHa.unl
  insert into tmp_loadh;

delete from tmp_loadh
  where tlh_genus != "Danio";

delete from tmp_loadh
  where tlh_sangerName = '>';

update tmp_loadH
  set tlh_seq = replace(tlh_seq,">",'');

update tmp_loadH
  set tlh_seq = replace(tlh_seq,"stem-loop",'');

create temp table tmp_th (th_zdb_id varchar(50),
       	    	  	th_noDreName varchar(50),
			th_accNum varchar(50),
			th_seq lvarchar)
 with no log ;
			
insert into tmp_th
  select get_id('TSCRIPT'),
  	 tlh_noDre,
	 tlh_accNum,
	 tlh_seq
    from tmp_loadh;

set constraints all deferred ;

select first 3 * from tmp_t;

insert into marker (mrkr_zdb_id, mrkr_name, mrkr_abbrev, mrkr_type, mrkr_owner)
  select t_zdb_id, t_noDrename||"-001", lower(t_noDrename)||"-001", 'TSCRIPT', 'ZDB-PERS-030520-3'
    from tmp_t;


insert into transcript (tscript_mrkr_zdb_id, tscript_type_id,tscript_status_id)
  select t_zdb_id, (select tscriptt_pk_id from transcript_type where tscriptt_type="miRNA"),
  	 	   (select tscripts_pk_id from transcript_status where tscripts_status = 'microRNA registry')
    from tmp_t;

select th_noDreName from marker_sequence, tmp_th
 where mrkrseq_sequence = th_seq ;

select first 3 t_nodrename from tmp_t;

update marker_sequence
  set mrkrseq_sequence = (select t_seq from tmp_t, tmp_th
      		       	 	 where mrkrseq_sequence = th_seq
				 and th_noDreName = t_nodrename)
  where exists (select 'x'
  	       	       from marker
		       where mrkr_zdb_id = mrkrseq_mrkr_zdb_id
		       and mrkr_name like 'mirn%');

!echo "add relations HERE" ;

insert into marker_relationship (mrel_zdb_id, mrel_mrkr_1_zdb_id, mrel_mrkr_2_zdb_id, mrel_type)
  select get_id('MREL'), mrkr_zdb_id, t_zdb_id, 'gene produces transcript'
    from marker, marker_sequence, tmp_t
    where mrkr_zdb_id = mrkrseq_mrkr_zdb_id
    and trim(t_seq) = trim(mrkrseq_sequence);

insert into sequence_type
  values ('RNA','rna sequences');

insert into marker_Sequence (mrkrseq_zdb_id, mrkrseq_mrkr_Zdb_id, mrkrseq_sequence, mrkrseq_seq_type)
  select get_id('MRKRSEQ'),mrkr_zdb_id, t_seq, 'RNA'
     from marker, tmp_t
     where mrkr_abbrev = t_noDrename
     and mrkr_zdb_id like 'ZDB-TSCRIPT%';

unload to microRnaGeneComments.txt
select mrkr_abbrev, mrkr_comments
  from marker where mrkr_abbrev like 'mirn%';

update marker
  set mrkr_comments = null 
  where mrkr_abbrev like 'mirn%';

insert into zdb_active_data
  select mrel_zdb_id from marker_relationship
    where not exists (Select 'x' 
    	      	     	     from zdb_active_data
  			     where zactvd_zdb_id = mrel_zdb_id);
insert into zdb_active_data
  select mrkrseq_zdb_id from marker_sequence
    where not exists (Select 'x' 
    	      	     	     from zdb_active_data
  			     where zactvd_zdb_id = mrkrseq_zdb_id);

insert into zdb_active_data
  select mrkr_zdb_id from marker
    where not exists (Select 'x' 
    	      	     	     from zdb_active_data
  			     where zactvd_zdb_id = mrkr_zdb_id);


set constraints all immediate;

----------HAIRPINS------------------


set constraints all deferred ;

insert into db_link (dblink_zdb_id, dblink_linked_recid, dblink_acc_num, dblink_fdbcont_zdb_id)
  select get_id('DBLINK'), t_zdb_id, th_accNum, (select fdbcont_zdb_id 
  	 		   	     	       	       from foreign_db_contains, 
						       	    foreign_db, foreign_db_data_type
						       where fdbcont_fdb_db_id = fdb_db_pk_id
						       and fdbcont_fdbdt_id = fdbdt_pk_id
						       and fdbdt_data_type = 'other'
						       and fdbdt_super_type = 'summary page'
						       and fdb_db_name = 'miRBASE' )
   from tmp_th, tmp_t
   where th_nodrename = t_nodrename;

insert into zdb_active_data
  select dblink_zdb_id from db_link
    where not exists (Select 'x' 
    	      	     	     from zdb_active_data
  			     where zactvd_zdb_id = dblink_zdb_id);

insert into zdb_active_data
  select fdbcont_zdb_id from foreign_db_Contains
    where not exists (Select 'x' 
    	      	     	     from zdb_active_data
  			     where zactvd_zdb_id = fdbcont_zdb_id);

set constraints all immediate ;


unload to seqs_not_in_zfin.txt
select t_nodrename, t_accNum, t_seq from tmp_T
 where not exists (Select 'x' from marker_sequence
       	   	  	      where mrkrseq_sequence = t_seq)
 order by t_nodrename;

unload to zfin.txt
select * from tmp_T
 where exists (Select 'x' from marker_sequence
       	   	  	      where mrkrseq_sequence = t_seq);

unload to sequences_in_zfin_not_in_mirbase.txt
  select mrkrseq_sequence, mrkr_abbrev, mrkr_zdb_id 
   from marker_Sequence,marker
   where not exists (Select 'x' from tmp_t where t_seq = mrkrseq_sequence)
   and mrkr_abbrev like 'mirn%'
   and mrkr_Zdb_id = mrkrseq_mrkr_zdb_id
   order by mrkr_abbrev;

select * from sequence_type ;

select count(*) from marker 
  where mrkr_abbrev like 'mirn%';

commit work ;
--rollback work ;


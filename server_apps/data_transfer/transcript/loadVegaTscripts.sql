begin work ;

create temp table tmp_load (ottdart varchar(50),
       tName varchar(255),
       ttype varchar(50),
       tVersion int,
       geneID varchar(50),
       clonePrefix varchar(60),
       gVersion int)
with no log ;
       
load from vega_fasta_090113.unl
 insert into tmp_load; 

--update tmp_load
--  set ottdart = replace(ottdart,'>','');

--select first 1 * from tmp_load where gtype like 'proc%';

create temp table tmp_new_tscripts
       (ot_id varchar(50),
       	     tName varchar(255),
             t_id varchar(50),
	     tVersion int,
	     gVersion int,
       	     g_id varchar(50),
	     cloneName varchar(60),
	     cloneZdbId varchar(50),
	     t_type varchar(60),
	     t_type_conv varchar(60),
	     status varchar(60),	     
	     geneAbbrev varchar(60))
	     --num varchar(10),
	     --clonetype varchar(40))
with no log ;


insert into tmp_new_tscripts (ot_id, tVersion,tname, g_id, gVersion, t_type, cloneName)
  select ottdart,tVersion, tName, geneID, gVersion,ttype, clonePrefix
    from tmp_load;

update tmp_new_tscripts
  set t_type = scrub_char(t_type);

update tmp_new_tscripts
  set cloneName = scrub_char(cloneName);

create temp table tmp_clone (counter int, clone_id varchar(50), clone_acc varchar(30))
 with no log;

insert into tmp_clone 
  select count(*),dblink_linked_recid, dblink_acc_num
    from db_link, tmp_new_tscripts
    where dblink_acc_num = cloneName
    and get_obj_type(dblink_linked_recid) in ('BAC','PAC','FOSMID')
    group by dblink_linked_recid, dblink_acc_num
    having count(*) > 1;

--causing duplicate problems.

delete from tmp_clone
 where clone_Acc in ('BX548249','AL627094','CU138546');

select count(*), clone_acc
  from tmp_clone
  group by clone_Acc
  having count(*) > 1;

update tmp_new_tscripts
  set cloneZdbId = (select distinct clone_id from tmp_clone
      		     where cloneName=clone_acc
		     )
   where cloneZdbId is null;

delete from tmp_new_tscripts
  where not exists (Select 'x' from db_link
  	    	   	   where ot_id = dblink_acc_num);


delete from tmp_new_tscripts
  where cloneZdbId is null;


update tmp_new_tscripts
  set t_type_conv = 'V-gene'
  where t_type = 'ig_gene';

update tmp_new_tscripts
  set t_type_conv = 'mRNA'
  where t_type = 'protein_coding';

update tmp_new_tscripts
  set t_type_conv = 'mRNA'
  where t_type = 'protein_coding_in_progress';

update tmp_new_tscripts
  set t_type_conv = 'ncRNA'
  where t_type = 'processed_transcript';

update tmp_new_tscripts
  set t_type_conv = 'pseudogenic transcript'
  where t_type in ('polymorphic_pseudogene','processed_pseudogene','transcribed_pseudogene','unprocessed_pseudogene','pseudogene');

update tmp_new_tscripts
  set t_type_conv = 'aberant processed transcript'
  where t_type in ('nonsense_mediated_decay','artifact');

update tmp_new_tscripts
  set t_type_conv = 'transposable element'
  where t_type = 'transposon';

update tmp_new_tscripts
  set t_type_conv = 'ncRNA'
  where t_type = 'retained_intron';

update tmp_new_tscripts
  set t_type_conv = 'ncRNA'
  where t_type = 'non_coding';


update tmp_new_tscripts
  set status = 'retained intron'
  where t_type = 'retained_intron';

update tmp_new_tscripts
  set status = 'processed pseudogene'
  where t_type = 'processed_pseudogene';

update tmp_new_tscripts
  set status = 'polymorphic pseudogene'
  where t_type = 'polymorphic_pseudogene';

update tmp_new_tscripts
  set status = 'processed transcript'
  where t_type = 'processed_transcript';

update tmp_new_tscripts
  set status = 'unprocessed pseudogene'
  where t_type = 'unprocessed_pseudogene';

update tmp_new_tscripts
  set status = 'artifact'
  where t_type = 'artifact';

update tmp_new_tscripts
  set status = 'NMD'
  where t_type = 'nonsense_mediated_decay';

update tmp_new_tscripts
  set status = 'protein coding in progress'
  where t_type = 'protein_coding_in_progress';

update tmp_new_tscripts
  set t_type_conv = t_type
  where t_type = 'antisense';

select distinct t_type from tmp_new_tscripts
  where t_type_Conv is null;

select distinct t_type_conv from tmp_new_tscripts
  where not exists (Select 'x' from transcript_type
  	    	   	   where tscriptt_type = t_type_conv);

select distinct t_type_conv 
  from tmp_new_tscripts
  where not exists (Select 'x' from transcript_type
  	    	   	   where tscriptt_type = t_type_conv);

create temp table tmp_tscript (ot_id varchar(50),tname varchar(255), 
       	    	  	      	     t_zdb_id varchar(50))
with no log;

insert into tmp_tscript (ot_id, tname)
  select distinct ot_id,tname from tmp_new_tscripts;

update tmp_tscript
  set t_zdb_id = get_id('TSCRIPT') 
  where exists (select 'x' from db_link where dblink_acc_num = ot_id);

--select count(*), tName
--  from tmp_tscript
--  group by tName
--  having count(*) > 1;

create temp table tmp_dup_names (name varchar(255), counter int)
with no log;

insert into tmp_dup_names
  select tname, count(*) 
    from tmp_new_tscripts
    group by tname
    having count(*) > 1;

create index tname_dup_index
  on tmp_dup_names (name)
  using btree in idxdbs2;

update statistics high for table tmp_dup_names;

set constraints all deferred ;

insert into marker (mrkr_Zdb_id, mrkr_name, mrkr_owner, mrkr_abbrev, mrkr_type, mrkr_commentS)
  select distinct t_zdb_id, tName, "ZDB-PERS-030520-1", lower(tName), "TSCRIPT", "test run"
    from tmp_tscript
    where ot_id is not null
    and exists (Select 'x' from db_link where dblink_acc_num = ot_id)
    and not exists (select 'x' from tmp_dup_names where name = tname);

insert into marker (mrkr_Zdb_id, mrkr_name, mrkr_owner, mrkr_abbrev, mrkr_type, mrkr_commentS)
  select distinct t_zdb_id, ot_id, "ZDB-PERS-030520-1", lower(ot_id), "TSCRIPT", "test run"
    from tmp_tscript, tmp_dup_names
    where ot_id is not null
    and exists (Select 'x' from db_link where dblink_acc_num = ot_id)
    and name = tname;

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
  select mrkr_zdb_id, 'ZDB-PUB-030703-1'
    from marker
    where mrkr_zdb_id like 'ZDB-TSCRIPT%';

create index tscript_type 
  on tmp_new_tscripts (t_type_conv)
  using btree in idxdbs2;

create index tscript_status
  on tmp_new_tscripts (status)
  using btree in idxdbs1;

create unique index t_zdb_id_index
 on tmp_tscript (t_zdb_id) 
 using btree in idxdbs3;

create index ot_id_index on tmp_new_tscripts (ot_id)
  using btree in idxdbs1;

create index ot_id_index_new on tmp_tscript (ot_id)
 using btree in idxdbs2;

update statistics high for table tmp_new_tscripts;
update statistics high for table transcript;
update statistics high for table tmp_tscript;

insert into transcript (tscript_mrkr_zdb_id, tscript_type_id, tscript_status_id)
  select distinct mrkr_zdb_id, (select tscriptt_pk_id 
  	 	  	       	       from transcript_type 
  	 	  	       	       where tscriptt_type = t_type_conv),
			       (select tscripts_pk_id
			       	       from transcript_status
				       where tscripts_status = status)
 from tmp_new_tscripts, marker, tmp_tscript
  where mrkr_type ='TSCRIPT'
  and tmp_new_tscripts.ot_id = tmp_tscript.ot_id
  and t_zdb_id = mrkr_zdb_id;


insert into accession_version
 select distinct dblink_acc_num, tversion
  from db_link, tmp_new_tscripts
  where dblink_acc_num = ot_id
  and tversion is not null;

insert into accession_version
 select distinct dblink_acc_num, gversion
  from db_link, tmp_new_tscripts
  where dblink_acc_num = g_id
  and gversion is not null;

--select * from transcript where tscript_type_id is null;

update transcript
  set tscript_status_id = null
  where exists (Select 'x' from transcript_status
  	       	       where tscripts_pk_id = tscript_status_id
		       and tscripts_status = 'unknown');

--select count(*) from marker
-- where mrkr_zdb_id like 'ZDB-TSCRIPT%';

insert into zdb_active_data
  select mrkr_zdb_id
  	 from marker
	 where not exists (select 'x' from zdb_active_data
	       	   	  	  where zactvd_zdb_id = mrkr_zdb_id);



insert into marker_relationship (mrel_zdb_id, mrel_mrkr_1_zdb_id,
       mrel_mrkr_2_zdb_id, mrel_type)
  select get_id("MREL"), cloneZdbId, t_zdb_id, 'clone contains transcript'
    from tmp_new_tscripts, tmp_tscript
    where tmp_tscript.ot_id = tmp_new_tscripts.ot_id
    and cloneZdbId is not null
    and tmp_tscript.ot_id not in ('OTTDART00000031459',
               		      	  'OTTDART00000032739',
               			  'OTTDART00000039337')
    and not exists (Select 'x' from marker_relationship b
    	    	   	   where cloneZdbId = b.mrel_mrkr_1_zdb_id
			   and t_zdb_id = b.mrel_mrkr_2_zdb_id
			   and mrel_type = b.mrel_type);

insert into marker_relationship (mrel_zdb_id, mrel_mrkr_1_zdb_id,
       mrel_mrkr_2_zdb_id, mrel_type) 
  select get_id("MREL"), dblink_linked_recid, t_zdb_id, 'gene produces transcript'
    from tmp_tscript, db_link
    where dblink_acc_num = ot_id
    and dblink_linked_recid like 'ZDB-GENE%'
    and tmp_tscript.ot_id not in ('OTTDART00000031459',
               		      	  'OTTDART00000032739',
               			  'OTTDART00000039337')
    and not exists (Select 'x' from marker_relationship b
    	    	   	   where dblink_linked_recid = b.mrel_mrkr_1_zdb_id
			   and t_zdb_id = b.mrel_mrkr_2_zdb_id
			   and mrel_type = b.mrel_type);

insert into marker_relationship (mrel_mrkr_1_zdb_id,
       mrel_mrkr_2_zdb_id, mrel_type)
  select distinct dblink_linked_recid, t_zdb_id, 'gene produces transcript'
    from tmp_tscript, db_link
    where dblink_acc_num = ot_id
    and dblink_linked_recid like 'ZDB-GENE%'
    and tmp_tscript.ot_id in ('OTTDART00000031459',
               		      	  'OTTDART00000032739',
               			  'OTTDART00000039337')
    and not exists (Select 'x' from marker_relationship b
    	    	   	   where dblink_linked_recid = b.mrel_mrkr_1_zdb_id
			   and t_zdb_id = b.mrel_mrkr_2_zdb_id
			   and mrel_type = b.mrel_type);
update marker_relationship
  set mrel_zdb_id = get_id('MREL')
      where mrel_zdb_id is null ;

select count(*), mrel_mrkr_1_zdb_id, mrel_mrkr_2_zdb_id, mrel_type
  from marker_relationship
  group by mrel_mrkr_1_zdb_id, mrel_mrkr_2_zdb_id, mrel_type
  having count(*) > 1;


insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
  select mrel_zdb_id, 'ZDB-PUB-030703-1'
    from marker_relationship
    where mrel_mrkr_2_zdb_id like 'ZDB-TSCRIPT%'
    and not exists (Select 'x' from record_attribution b
   	    	   	   where b.recattrib_data_zdb_id = mrel_zdb_id
			   and b.recattrib_source_zdb_id = 'ZDB-PUB-030703-1');

insert into zdb_active_data
  select mrel_zdb_id from marker_relationship
where not exists (Select 'x' from zdb_active_data
      	  	 	 where mrel_zdb_id = zactvd_zdb_id);

set constraints all immediate;

update statistics high for table tmp_new_tscripts;
update statistics high for table marker;
update statistics high for table db_link;
update statistics high for table tmp_tscript;

update db_link
  set dblink_linked_recid = (Select t_zdb_id
      			    	    from tmp_tscript
				    where ot_id = dblink_acc_num)
  where dblink_acc_num like 'OTTDART%'
  and exists (Select 'x' from tmp_tscript
      	     	     where ot_id = dblink_acc_num)
  and dblink_acc_num not in ('OTTDART00000031459',
               		      	  'OTTDART00000032739',
               			  'OTTDART00000039337');


--!echo 'leftover dblink tscripts' ;

--select count(*) from accession_version;


rollback work ;
--commit work ;
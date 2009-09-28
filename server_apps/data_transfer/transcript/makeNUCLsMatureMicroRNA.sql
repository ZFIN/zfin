begin work ;

--curated microRNA mature tscript sequences
select mrkrseq_zdb_id as mseq_zdb_id, 
       		       getZfinAccessionNumber('ZFINNUCL') as
 		       nuclNum, mrkrseq_sequence as sequence, 
		       mrkrseq_mrkr_zdb_id as mrkr_id, 
		       mrkr_abbrev as mrkr_abbrev
   from marker_sequence, marker
   where mrkrseq_mrkr_zdb_id like 'ZDB-TSCRIPT%'
   and mrkrseq_mrkr_zdb_id = mrkr_zdb_id
   and length(mrkrseq_sequence) < 30
   and exists (Select 'x' from marker
       	      	      where mrkrseq_mrkr_zdb_id = mrkr_zdb_id
		      and mrkr_abbrev like 'mir%')
   and not exists (select 'x' from db_link
       	      	      	  where mrkr_zdb_id = dblink_linked_recid
			  and dblink_acc_num like "MI%")
into temp tmp_mseq_nucl_map; 
       	  
select first 1 * from tmp_mseq_nucl_map;

set constraints all deferred ;
insert into db_link (dblink_zdb_id, dblink_linked_recid, dblink_acc_num,
       	    	    		    dblink_fdbcont_zdb_id)
 select get_id('DBLINK'), mrkrseq_mrkr_Zdb_id, nuclNum, 
 			  (select fdbcont_zdb_id
			  	  from foreign_db_contains, foreign_db
				  where fdbcont_fdb_db_id = fdb_db_pk_id
				  and fdb_db_name = 'Curated miRNA Mature')
  from marker_sequence, tmp_mseq_nucl_map
  where mrkrseq_zdb_id = mseq_zdb_id;

insert into zdb_active_data
  select dblink_zdb_id from db_link
    where not exists (Select 'x' from zdb_active_Data
    	      	     	     where zactvd_zdb_id =dblink_Zdb_id);

--insert into record_attribution 

set constraints all immediate;

unload to matureNUCLs.txt
  select ">lcl",
  	 nuclnum,
	 mrkr_id,
	 'CuratedMicroRNAMature',
	 mrkr_abbrev,
	 tscriptt_type,
	 length(sequence)||"bp",
  	 sequence
    from tmp_mseq_nucl_map,
	 transcript, 
    	 transcript_type
    where mrkr_id = tscript_mrkr_zdb_id
    and tscript_type_id = tscriptt_pk_id
   ; 

--delete from marker_sequence
--  where exists (select 'x' from tmp_mseq_nucl_map
--  	       	       where mseq_zdb_id = mrkrseq_zdb_id);

drop table tmp_mseq_nucl_map;

--curated microRNA stem loop tscript sequences
select mrkrseq_zdb_id as mseq_zdb_id, getZfinAccessionNumber('ZFINNUCL') as
 		       nuclNum, mrkrseq_sequence as sequence, mrkrseq_mrkr_zdb_id as mrkr_id, mrkr_abbrev as mrkr_abbrev, mrkr_type as mrkr_type
   from marker_sequence, marker
   where mrkrseq_mrkr_zdb_id like 'ZDB-GENE%'
   and mrkrseq_mrkr_zdb_id = mrkr_zdb_id
   and length(mrkrseq_sequence) > 30
   and exists (Select 'x' from marker
       	      	      where mrkrseq_mrkr_zdb_id = mrkr_zdb_id
		      and mrkr_abbrev like 'mir%')
   and not exists (select 'x' from db_link
       	      	      	  where mrkr_zdb_id = dblink_linked_recid
			  and dblink_acc_num like "MI%")
into temp tmp_mseq_nucl_map; 
       	  
select first 1 * from tmp_mseq_nucl_map;

unload to stemLoopNUCLs.txt
  select ">lcl",
  	 nuclnum,
	 mrkr_id,
	 'CuratedMicroRNAStemLoop',
	 mrkr_abbrev,
	 mrkr_type,
	 length(sequence)||"bp",
  	 sequence
    from tmp_mseq_nucl_map; 
	     

--loaded microRNA mature tscript sequences
drop table tmp_mseq_nucl_map;


select mrkrseq_zdb_id as mseq_zdb_id, 
       		       getZfinAccessionNumber('ZFINNUCL') as
 		       nuclNum, mrkrseq_sequence as sequence, 
		       mrkrseq_mrkr_zdb_id as mrkr_id, 
		       mrkr_abbrev as mrkr_abbrev
   from marker_sequence, marker
   where mrkrseq_mrkr_zdb_id like 'ZDB-TSCRIPT%'
   and mrkrseq_mrkr_zdb_id = mrkr_zdb_id
   and length(mrkrseq_sequence) < 30
   and exists (Select 'x' from marker
       	      	      where mrkrseq_mrkr_zdb_id = mrkr_zdb_id
		      and mrkr_abbrev like 'mir%')
   and exists (select 'x' from db_link
       	      	      	  where mrkr_zdb_id = dblink_linked_recid
			  and dblink_acc_num like "MI%")
into temp tmp_mseq_nucl_map; 
       	  
select first 1 * from tmp_mseq_nucl_map;

unload to matureLoadedNUCLs.txt
  select ">lcl",
  	 dblink_acc_num,
	 dblink_linked_recid,
	 'LoadedMicroRNAMature',
	 mrkr_abbrev,
	 tscriptt_type,
	 length(sequence)||"bp",
  	 sequence
    from tmp_mseq_nucl_map,
	 transcript, 
    	 transcript_type,
	 db_link
    where mrkr_id = tscript_mrkr_zdb_id
    and tscript_type_id = tscriptt_pk_id
    and mrkr_id = dblink_linked_recid; 

--delete from marker_sequence
--  where exists (select 'x' from tmp_mseq_nucl_map
--  	       	       where mseq_zdb_id = mrkrseq_zdb_id);

drop table tmp_mseq_nucl_map;

--loaded microRNA stem loop gene sequences
select mrkrseq_zdb_id as mseq_zdb_id, getZfinAccessionNumber('ZFINNUCL') as
 		       nuclNum, mrkrseq_sequence as sequence, mrkrseq_mrkr_zdb_id as mrkr_id, mrkr_abbrev as mrkr_abbrev, mrkr_type as mrkr_type
   from marker_sequence, marker
   where mrkrseq_mrkr_zdb_id like 'ZDB-GENE%'
   and mrkrseq_mrkr_zdb_id = mrkr_zdb_id
   and length(mrkrseq_sequence) > 30
   and exists (Select 'x' from marker
       	      	      where mrkrseq_mrkr_zdb_id = mrkr_zdb_id
		      and mrkr_abbrev like 'mir%')
   and exists (select 'x' from db_link
       	      	      	  where mrkr_zdb_id = dblink_linked_recid
			  and dblink_acc_num like "MI%")
into temp tmp_mseq_nucl_map; 
       	  
select first 1 * from tmp_mseq_nucl_map;

unload to stemLoopLoadedNUCLs.txt
  select ">lcl",
  	 dblink_acc_num,
	 dblink_linked_recid,
	 'LoadedMicroRNAMature',
	 mrkr_abbrev,
	 tscriptt_type,
	 length(sequence)||"bp",
  	 sequence
    from tmp_mseq_nucl_map,
	 transcript, 
    	 transcript_type,
	 db_link
    where mrkr_id = tscript_mrkr_zdb_id
    and tscript_type_id = tscriptt_pk_id
    and mrkr_id = dblink_linked_recid
   ; 


select max(length(mrkrseq_sequence)) as max, min(length(mrkrseq_sequence)) from marker_sequence as min, marker
 where mrkrseq_mrkr_zdb_id = mrkr_zdb_id
  and mrkr_abbrev like 'mir%';

--delete from marker_sequence
--  where exists (select 'x' from tmp_mseq_nucl_map
--  	       	       where mseq_zdb_id = mrkrseq_zdb_id);

--commit work ;

rollback work ;
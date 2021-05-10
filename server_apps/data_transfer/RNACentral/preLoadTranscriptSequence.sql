-- preloadTranscriptSequence
--
-- This SQL script prepares the list of transcripts in teh databse that need to be popultaed with sequence.


begin work;
                      
create temp table tscripts (tscriptid text, tscriptottdart text );

		
insert into tscripts
select distinct tscript_mrkr_zdb_id,tscript_load_id
  from transcript,marker_relationship,marker,marker_type_group_member
 where tscript_load_id like 'OTTDART%'
 and tscript_mrkr_zdb_id=mrel_mrkr_2_zdb_id
 and mrel_mrkr_1_zdb_id=mrkr_zdb_id
 and mrel_type='gene produces transcript'
 and mtgrpmem_mrkr_type=mrkr_type
 and mtgrpmem_mrkr_type_group='RNAGENE' and (tscript_status_id!=1 or tscript_status_id is null);

\copy (select * from tscripts) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/RNACentral/getSequence' with delimiter as ',' null as '';



drop  table tscripts;

--rollback work;

commit work;




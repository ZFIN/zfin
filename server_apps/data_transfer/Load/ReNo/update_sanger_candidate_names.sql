begin work ;

--drop index candidate_cnd_suggested_name_idx;

create temp table tmp_ids(cnd_suggested_name text);

insert into tmp_ids(cnd_suggested_name)
select cnd_suggested_name
  from candidate
 where cnd_suggested_name like 'ENSDART%'
 and exists (Select 'x' from run_candidate, run
                        where runcan_run_Zdb_id = run_zdb_id 
        and runcan_cnd_zdb_id = cnd_zdb_id  
and run_name = 'SangerMutantLoad_131217');

create temp table tmp_update (ensm_ensdarg_id text, cnd_suggested_name text);

insert into tmp_update(ensm_ensdarg_id, cnd_suggested_name)
select ensm_Ensdarg_id, cnd_suggested_name
 from tmp_ids, ensdar_mapping
 where ensm_ensdart_id = cnd_suggested_name;

--Delete candidates from run table that alreday have existed in runs before but do not have any last done date

--update accession bak fdbconts to be ensembl's
update accession_bank set accbk_fdbcont_zdb_id='ZDB-FDBCONT-061018-1' where accbk_acc_num like 'ENSDARG%';

update db_link set dblink_fdbcont_zdb_id='ZDB-FDBCONT-061018-1' where dblink_acc_num like 'ENSDARG%';

\copy (select * from candidate where cnd_suggested_name like 'ENSDARG%' and cnd_zdb_id not like 'ZDB-CND-131217%' and cnd_last_done_date is null and cnd_suggested_name in (select ensm_ensdarg_id from tmp_update)) to 'alreadyinCND.txt' ;

delete  from candidate where cnd_suggested_name like 'ENSDARG%' and cnd_zdb_id not like 'ZDB-CND-131217%' and cnd_last_done_date is null and cnd_suggested_name in (select ensm_ensdarg_id from tmp_update);


--Delete candidates from run table that alreday have existed in runs before but  have a last done date

\copy (select * from candidate  where cnd_suggested_name in (select ensm_ensdarg_id from tmp_update) and cnd_last_done_date is not null) to 'alreadyinCNDnotnull.txt' ;

delete from candidate  where cnd_suggested_name in (select ensm_ensdarg_id from tmp_update) and cnd_last_done_date is not null;

select count(*) as counter, ensm_ensdarg_id as g_id
  from tmp_update
 group by ensm_ensdarg_id
 having count(*) > 1;
 

--set constraints all deferred;
update candidate
  set cnd_suggested_name = (select ensm_ensdarg_id 
       from  ensdar_mapping
        where 
	 cnd_suggested_name = ensm_ensdart_id
        )
where exists (Select 'x' from run_candidate, run
                        where runcan_run_Zdb_id = run_zdb_id 
        and runcan_cnd_zdb_id = cnd_zdb_id  
and run_name = 'SangerMutantLoad_131217') ;

select count(*), cnd_suggested_name
 from candidate
 group by cnd_suggested_name
 having count(*) > 1;

--set constraints all immediate;
update run
 set run_nomen_pub_zdb_id = 'ZDB-PUB-130725-1'
 where run_date=NOW();

update run
 set run_relation_pub_zdb_id ='ZDB-PUB-130211-1'
where run_date=NOW();

--create unique index candidate_cnd_suggested_name_idx 
--    on candidate (cnd_suggested_name) using btree  
--    in idxdbs3;
rollback work ;
--commit work ;

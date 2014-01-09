begin work ;

drop index candidate_cnd_suggested_name_idx;

delete from candidate
 where cnd_zdb_id like 'ZDB-CND-131210%'
 and cnd_suggested_name like 'zmp:%';

delete from candidate
 where cnd_zdb_id like 'ZDB-CND-130403-%'
 and cnd_suggested_name like 'zmp:%';

delete from candidate
 where cnd_zdb_id ='ZDB-CND-080728-1022';

select cnd_suggested_name, ensm_ensdarg_id, mrkr_Abbrev
 from candidate, marker, db_link, ensdar_mapping
     where mrkr_zdb_id = dblink_linked_recid
     and dblink_Acc_num = ensm_ensdarg_id
     and cnd_suggested_name = ensm_ensdart_id
    and exists (Select 'x' from run_candidate
       	      	      where cnd_zdb_id = runcan_cnd_zdb_id
 		      and runcan_run_zdb_id like 'ZDB-RUN-14%')
into temp tmp_ids;

select count(*) as counter, mrkr_abbrev as abbrev
 from tmp_ids
 group by mrkr_abbrev 
 having count(*) > 1
into temp tmp_no_update;

select mrkr_abbrev, ensm_ensdart_id
  from tmp_no_update, marker, db_link, ensdar_mapping
 where mrkr_Abbrev = abbrev
 and mrkr_zdb_id = dblink_linked_recid
 and dblink_Acc_num = ensm_ensdarg_id
into temp tmp_ensdart;


update candidate 
 set cnd_suggested_name = (select mrkr_abbrev from marker, db_link, ensdar_mapping
     where mrkr_zdb_id = dblink_linked_recid
     and dblink_Acc_num = ensm_ensdarg_id
     and cnd_suggested_name = ensm_ensdart_id)
     where exists (Select 'x' from run_candidate, run
       	      	      where run_zdb_id = runcan_run_zdb_id
		      and cnd_zdb_id = runcan_cnd_zdb_id
		      )
   and cnd_suggested_name not in (select ensm_ensdart_id from tmp_ensdart)
   and cnd_Suggested_name like 'ENSDART%';

                

select cnd_suggested_name, count(*)
 from candidate
 group by cnd_suggested_name
 having count(*) > 1;

select * from candidate 
  where cnd_suggested_name = 'zmpste24';

create unique index candidate_cnd_suggested_name_idx 
    on candidate (cnd_suggested_name) using btree  
    in idxdbs3;


rollback work ;
--commit work;

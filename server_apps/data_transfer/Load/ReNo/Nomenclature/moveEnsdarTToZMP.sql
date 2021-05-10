begin work ;

delete from candidate
 where cnd_zdb_id like 'ZDB-CND-131210%'
 and cnd_suggested_name like 'zmp:%';

delete from candidate
 where cnd_zdb_id like 'ZDB-CND-130403-%'
 and cnd_suggested_name like 'zmp:%';

delete from candidate
 where cnd_zdb_id ='ZDB-CND-080728-1022';

create temp table tmp_ids (cnd_suggested_name varchar(100),
       	    	  	   ensm_ensdarg_id varchar(100),
			   mrkr_abbrev varchar(100))
;

insert into tmp_ids
select cnd_suggested_name, ensm_ensdarg_id, mrkr_Abbrev
 from candidate, marker, db_link, ensdar_mapping
     where mrkr_zdb_id = dblink_linked_recid
     and dblink_Acc_num = ensm_ensdarg_id
     and cnd_suggested_name = ensm_ensdart_id
    and exists (Select 'x' from run_candidate
       	      	      where cnd_zdb_id = runcan_cnd_zdb_id
 		      and runcan_run_zdb_id like 'ZDB-RUN-14%');


create temp table tmp_no_update (counter int, abbrev varchar(100));

insert into tmp_no_update
select count(*) as counter, mrkr_abbrev as abbrev
 from tmp_ids
 group by mrkr_abbrev 
 having count(*) > 1;


create temp table tmp_ensdart (mrkr_abbrev varchar(100), ensm_ensdart_id varchar(100));

insert into tmp_ensdart
select mrkr_abbrev, ensm_ensdart_id
  from tmp_no_update, marker, db_link, ensdar_mapping
 where mrkr_Abbrev = abbrev
 and mrkr_zdb_id = dblink_linked_recid
 and dblink_Acc_num = ensm_ensdarg_id;


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


rollback work ;
--commit work;

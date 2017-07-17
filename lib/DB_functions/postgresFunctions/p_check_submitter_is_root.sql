create or replace function p_check_submitter_is_root (vSubmitter_ID text)
returns void as $$

--p_check_submitter_is_root
-------------------------------
--variables passed in: person_zdb_id varchar(50), required
--
--output: checks that zdb_submitters.access equals root 
--  where zdb_submitters.zdb_id
--  equals person_zdb_id.
--
--currently called only by trigger on expression_pattern_infrastructure
--  but could be used for other tables.
--------------------------

    declare vOk_root_submitter integer := 0 ;
    begin 
      vOk_root_submitter = (select count(*)
			 	from zdb_submitters
				where access = 'root'
				and zdb_id = vSubmitter_ID) ;

      if vOk_root_submitter = 0

      then
	
	 raise exception 'FAIL!: you are not an authorized submitter' ;
  
      end if;
    end
$$ LANGUAGE plpgsql

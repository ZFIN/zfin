
create or replace function insert_clone() returns integer as $cnt$
    declare cnt integer :=0;
    	    rtn integer;
     	    id text;
     	    abbrev varchar(150);

    for id, abbrev in  
    select mrkr_zdb_id,mrkr_abbrev 
        from marker
        where mrkr_type = 'EST'                                     -- existing est
        and mrkr_zdb_id not in (select clone_mrkr_zdb_id from clone)-- not redundant
        and mrkr_abbrev[1,4] in (select fli_fest from fest_lib_inst)-- have key defined
	loop
         rtn = 1;
         select insrt_cln(id,abbrev) into rtn;
        if rtn = 0 then 
             cnt = cnt + 1; 
        end if;
    end loop;
    return cnt;
$cnt$ LANGUAGE plpgsql

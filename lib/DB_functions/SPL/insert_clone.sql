-- insert_clone seeks out new EST and builds clone records for them
-- returns the number of clones inserted
-- realies on the probe_lib and fest_lib_inst for parts
 
drop function insrt_cln;

create function insrt_cln( est_id varchar(50),est_abbrev varchar(60) ) returning integer
    insert into clone (   
        clone_mrkr_zdb_id,
        clone_comments,
        clone_vector_name,
      --clone_polymerase_name,
      --clone_insert_size,
      --clone_cloning_site,
      --clone_digest,
        clone_probelib_zdb_id,
        clone_sequence_type
    )
    select distinct
           est_id,                   --clone_mrkr_zdb_id
           "link EST to a probe_lib",--clone_comments
           pl_vector,                --clone_vector_name
         --null,                     --clone_polymerase_name
         --null,                     --clone_insert_size
         --null,                     --clone_cloning_site
         --null ,                    --clone_digest
           probelib_zdb_id,          --clone_probelib_zdb_id
           'cDNA'                   --clone_sequence_type

    from probe_library,
         probe_lib,
         fest_lib_inst

    where est_abbrev[1,4] = fli_fest
    and   probelib_name   = fli_lib
    and   pl_lib_name     = fli_lib     
    and   probelib_name   = pl_lib_name ;

    return 0;
end function; --insrt_cln
update statistics for function insrt_cln;

-- a wrapper for the previous function
drop function insert_clone;
create function insert_clone() returning integer;
    define cnt, rtn integer;
    define id varchar(50);
    define abbrev varchar(60);
    let cnt = 0;
    foreach  
    select mrkr_zdb_id,mrkr_abbrev into id,abbrev
        from marker
        where mrkr_type = 'EST'                                     -- existing est
        and mrkr_zdb_id not in (select clone_mrkr_zdb_id from clone)-- not redundant
        and mrkr_abbrev[1,4] in (select fli_fest from fest_lib_inst)-- have key defined
        let rtn = 1;
        call insrt_cln(id,abbrev) returning rtn;
        if rtn = 0 then 
            let cnt = cnt + 1; 
        end if;
    end foreach;
    return cnt;
end function;
update statistics for function insert_clone;


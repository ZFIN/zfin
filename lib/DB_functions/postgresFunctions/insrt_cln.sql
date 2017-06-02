-- insert_clone seeks out new EST and builds clone records for them
-- returns the number of clones inserted
-- realies on the probe_lib and fest_lib_inst for parts

create or replace function insrt_cln( est_id varchar(50),est_abbrev varchar(150) ) returns int as $$

begin
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

    where substring(est_abbrev,1,4) = fli_fest
    and   probelib_name   = fli_lib
    and   pl_lib_name     = fli_lib     
    and   probelib_name   = pl_lib_name ;

    return 0;
end
$$ LANGUAGE plpgsql

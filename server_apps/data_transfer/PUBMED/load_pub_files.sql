begin work ;

create temp table tmp_files_to_load (pub_zdb_id text,
       pmc_id text,
       filename text,
       filename_no_path text);

\copy tmp_files_to_load from  'pdfsToLoad.txt' with delimiter '|' ;

update publication_file 
 set pf_file_type_id = 3
where exists (select 'x' from tmp_files_to_load
                     where pf_pub_zdb_id = pub_zdb_id
                     and pf_file_name = filename);

delete from tmp_files_to_load
 where exists (select 'x' from publication_file
                      where pf_pub_zdb_id = pub_zdb_id
                      and pf_original_file_name = filename_no_path);

with added as (
  insert into publication_file (pf_pub_zdb_id,
              pf_file_name,
              pf_file_type_id,
              pf_original_file_name,
              pf_source)
   select pub_zdb_id,
          filename,
          3,
          filename_no_path,
          'PMC_LOADER'
     from tmp_files_to_load
  -- publication_file_pub_file_name_uq: a publication records a stored file once
  on conflict (pf_pub_zdb_id, pf_file_name) do nothing
  returning pf_pub_zdb_id, pf_file_name
)
insert into updates (rec_id, field_name, new_value, comments, upd_when)
 select pf_pub_zdb_id, 'publication file', pf_file_name || ' (Supplemental Material)',
        'Add file (PMC loader)', now()
   from added;

--rollback work ;

commit work;

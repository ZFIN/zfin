begin work ;

create temp table tmp_files_to_load (pub_zdb_id text,
       pmc_id text,
       filename text);

\copy tmp_files_to_load from  'pdfsToLoad.txt' with delimiter '|' ;

insert into publication_file (pf_pub_zdb_id, pf_file_name, pf_file_type_id)
 select pub_zdb_id, filename, 3
  from tmp_files_to_load
 where not exists (select 'x' from publication_file
                          where pf_pub_zdb_id = pub_zdb_id
                          and filename = pf_file_name);


--rollback work ;

commit work;

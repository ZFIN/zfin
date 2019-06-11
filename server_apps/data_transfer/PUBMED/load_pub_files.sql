begin work ;

create temp table tmp_files_to_load (pub_zdb_id text,
       pmc_id text,
       filename text);

\copy tmp_files_to_load from  'pdfsToLoad.txt' with delimiter '|' ;

update publication_file 
 set pf_file_type_id = 3
where exists (select 'x' from tmp_files_to_load
                     where pf_pub_zdb_id = pub_zdb_id);

--rollback work ;

commit work;

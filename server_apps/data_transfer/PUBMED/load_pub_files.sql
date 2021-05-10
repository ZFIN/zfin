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

insert into publication_file (pf_pub_zdb_id,
            pf_file_name,
            pf_file_type_id,
            pf_original_file_name)
 select pub_zdb_id,
        filename,
        3,
        filename_no_path
   from tmp_files_to_load;

--rollback work ;

commit work;

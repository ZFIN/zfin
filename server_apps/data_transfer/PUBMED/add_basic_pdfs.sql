begin work;

create temp table tmp_pdf_to_load (pub_zdb_id text,
       pmc_id text,
       filename text);

\copy tmp_pdf_to_load from  'pdfBasicFilesToLoad.txt' with delimiter '|' ;

insert into publication_file (pf_pub_zdb_id, pf_file_name, pf_file_type_id)
 select pub_zdb_id, filename, 1
  from tmp_pdf_to_load
 where not exists (select 'x' from publication_file where pub_zdb_id = pf_pub_zdb_id and pf_file_name = filename and pf_file_type_id = 1);


--rollback work ;

commit work;

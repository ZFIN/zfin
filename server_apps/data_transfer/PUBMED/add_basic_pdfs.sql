begin work;

create temp table tmp_pdf_to_load (pub_zdb_id text,
       pmc_id text,
       filename text,
       filename_no_path text);

\copy tmp_pdf_to_load from  'pdfBasicFilesToLoad.txt' with delimiter '|' ;

-- ZFIN-10579: the main PDF is renamed to <zdbId>.pdf, so any accession-named
-- (PMC<id>.<v>.pdf) row left by an earlier run points at a file that no longer exists.
-- Remove it so the publication has a single Original Article.
-- Every change to publication_file is also recorded in the updates table (no submitter: automated load).
with removed as (
  delete from publication_file pf
   where pf.pf_file_type_id = 1
     and pf.pf_original_file_name ~ '^PMC[0-9]+(\.[0-9]+)?\.pdf$'
     and exists (select 'x' from tmp_pdf_to_load where pub_zdb_id = pf.pf_pub_zdb_id
                   and filename <> pf.pf_file_name)
  returning pf.pf_pub_zdb_id, pf.pf_file_name
)
insert into updates (rec_id, field_name, old_value, comments, upd_when)
 select pf_pub_zdb_id, 'publication file', pf_file_name || ' (Original Article)',
        'Delete file: stale accession-named PDF (PMC loader)', now()
   from removed;

-- The renamed file may already be recorded as another type (e.g. Supplemental Material).
with removed as (
  delete from publication_file pf
   where pf.pf_file_type_id <> 1
     and exists (select 'x' from tmp_pdf_to_load where pub_zdb_id = pf.pf_pub_zdb_id
                   and filename = pf.pf_file_name)
  returning pf.pf_pub_zdb_id, pf.pf_file_name
)
insert into updates (rec_id, field_name, old_value, comments, upd_when)
 select pf_pub_zdb_id, 'publication file', pf_file_name,
        'Delete file: duplicate of Original Article (PMC loader)', now()
   from removed;

with added as (
  insert into publication_file (pf_pub_zdb_id, pf_file_name, pf_file_type_id, pf_original_file_name, pf_source)
   select pub_zdb_id, filename, 1, filename_no_path, 'PMC_LOADER'
    from tmp_pdf_to_load
   where not exists (select 'x' from publication_file where pub_zdb_id = pf_pub_zdb_id and pf_file_name = filename and pf_file_type_id = 1)
  -- publication_file_pub_file_name_uq: a publication records a stored file once
  on conflict (pf_pub_zdb_id, pf_file_name) do nothing
  returning pf_pub_zdb_id, pf_file_name
)
insert into updates (rec_id, field_name, new_value, comments, upd_when)
 select pf_pub_zdb_id, 'publication file', pf_file_name || ' (Original Article)',
        'Add file (PMC loader)', now()
   from added;

--rollback work ;

commit work;

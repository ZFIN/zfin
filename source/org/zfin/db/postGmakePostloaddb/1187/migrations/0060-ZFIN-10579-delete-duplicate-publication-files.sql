--liquibase formatted sql

--changeset rtaylor:ZFIN-10579-delete-duplicate-publication-files
-- Delete publication_file rows that repeat the same stored file (same publication, same pf_file_name).
-- The rows point at one file on disk, so no file is removed. One updates row is written per group (not
-- per deleted row) so a 564-copy group is one entry.
--
-- Step 0: three files of ZDB-PUB-170125-8 where the Supplemental Material row is the right one.
-- Step 1: same file, same type: keep the lowest pf_pk_id.
-- Step 2: same file, different types: keep the Original Article row and delete the others
--         (Supplemental Material, Other, ...). Groups with no Original Article row are left alone:
--         which type is right there is a curator decision. They are listed by the
--         DUPLICATE_FILE_MIXED_TYPES issue of Find-Publication-File-Issues_w and must be resolved
--         before a unique (publication, file) constraint is added.
--
-- This only deletes rows, so there is nothing to roll back.

-- Step 0: ZDB-PUB-170125-8. A 2019-07-23 bulk import re-added three curator-uploaded supplements as
-- Original Article rows. The 2017 Supplemental Material rows are the correct ones, so delete the
-- Original Article duplicates here, before step 2 would do the opposite.
with removed as (
  delete from publication_file pf
   using publication_file_type t
   where t.pft_pk_id = pf.pf_file_type_id
     and t.pft_type = 'Original Article'
     and pf.pf_pub_zdb_id = 'ZDB-PUB-170125-8'
     and pf.pf_file_name in ('2017/ZDB-PUB-170125-8/ZDB-PUB-170125-8-Figure S1.pdf',
                             '2017/ZDB-PUB-170125-8/ZDB-PUB-170125-8-Figure S2.pdf',
                             '2017/ZDB-PUB-170125-8/ZDB-PUB-170125-8-Table S1-S4.pdf')
     and exists (select 'x' from publication_file keep
                  where keep.pf_pub_zdb_id = pf.pf_pub_zdb_id
                    and keep.pf_file_name = pf.pf_file_name
                    and keep.pf_pk_id <> pf.pf_pk_id
                    and keep.pf_file_type_id <> pf.pf_file_type_id)
  returning pf.pf_pub_zdb_id, pf.pf_file_name
)
insert into updates (rec_id, field_name, old_value, comments, upd_when)
select pf_pub_zdb_id, 'publication file', pf_file_name || ' (Original Article)',
       'Delete file: mistyped duplicate of the Supplemental Material row (ZFIN-10579 migration)', now()
  from removed;

-- Step 1
with removed as (
  delete from publication_file pf
   using (select pf_pub_zdb_id, pf_file_name, pf_file_type_id, min(pf_pk_id) as keep_id
            from publication_file
           group by pf_pub_zdb_id, pf_file_name, pf_file_type_id
          having count(*) > 1) d
   where pf.pf_pub_zdb_id = d.pf_pub_zdb_id
     and pf.pf_file_name = d.pf_file_name
     and pf.pf_file_type_id = d.pf_file_type_id
     and pf.pf_pk_id <> d.keep_id
  returning pf.pf_pub_zdb_id, pf.pf_file_name, pf.pf_file_type_id, d.keep_id
)
insert into updates (rec_id, field_name, old_value, comments, upd_when)
select r.pf_pub_zdb_id, 'publication file',
       r.pf_file_name || ' (' || t.pft_type || ')',
       'Delete file: ' || count(*) || ' duplicate row(s) for the same file and type, kept row id '
         || r.keep_id || ' (ZFIN-10579 migration)',
       now()
  from removed r
  join publication_file_type t on t.pft_pk_id = r.pf_file_type_id
 group by r.pf_pub_zdb_id, r.pf_file_name, r.pf_file_type_id, r.keep_id, t.pft_type;

-- Step 2
with removed as (
  delete from publication_file pf
   using publication_file keep
   join publication_file_type kt on kt.pft_pk_id = keep.pf_file_type_id and kt.pft_type = 'Original Article'
   where pf.pf_pub_zdb_id = keep.pf_pub_zdb_id
     and pf.pf_file_name = keep.pf_file_name
     and pf.pf_pk_id <> keep.pf_pk_id
  returning pf.pf_pub_zdb_id, pf.pf_file_name, pf.pf_file_type_id, keep.pf_pk_id as keep_id
)
insert into updates (rec_id, field_name, old_value, comments, upd_when)
select r.pf_pub_zdb_id, 'publication file',
       r.pf_file_name || ' (' || string_agg(t.pft_type, ', ' order by t.pft_type) || ')',
       'Delete file: same file also recorded as Original Article, kept row id '
         || r.keep_id || ' (ZFIN-10579 migration)',
       now()
  from removed r
  join publication_file_type t on t.pft_pk_id = r.pf_file_type_id
 group by r.pf_pub_zdb_id, r.pf_file_name, r.keep_id;
-- Step 3: the four mixed-type groups that have no Original Article row, decided by hand. Each is keyed on
-- publication, stored path and type, so it does nothing where the rows are not present.
-- Keep one type, delete the other row.
with keep_type(pub, path, kept, dropped) as (values
  ('ZDB-PUB-160604-6',  '2016/ZDB-PUB-160604-6/ZDB-PUB-160604-6-160604-6.docx',                  'Correspondence Details', 'Other'),
  ('ZDB-PUB-190207-4',  '2019/ZDB-PUB-190207-4/ZDB-PUB-190207-4-Supplementary .pdf',             'Supplemental Material',  'Other'),
  ('ZDB-PUB-210317-11', '2021/ZDB-PUB-210317-11/ZDB-PUB-210317-11-FigS1 Nr5a1 genomics.ai',     'Supplemental Material',  'Correspondence Details')
), removed as (
  delete from publication_file pf
   using keep_type k, publication_file_type dt, publication_file_type kt
   where pf.pf_pub_zdb_id = k.pub
     and pf.pf_file_name = k.path
     and dt.pft_type = k.dropped and pf.pf_file_type_id = dt.pft_pk_id
     and kt.pft_type = k.kept
     and exists (select 'x' from publication_file keep
                  where keep.pf_pub_zdb_id = pf.pf_pub_zdb_id and keep.pf_file_name = pf.pf_file_name
                    and keep.pf_file_type_id = kt.pft_pk_id)
  returning pf.pf_pub_zdb_id, pf.pf_file_name, k.dropped, k.kept
)
insert into updates (rec_id, field_name, old_value, comments, upd_when)
select pf_pub_zdb_id, 'publication file', pf_file_name || ' (' || dropped || ')',
       'Delete file: same file also recorded as ' || kept || ' (ZFIN-10579 migration)', now()
  from removed;

-- ZDB-PUB-210807-8: the file is Correspondence Details. Keep the earlier row, retype it, delete the other.
with dropped as (
  delete from publication_file pf
   where pf.pf_pub_zdb_id = 'ZDB-PUB-210807-8'
     and pf.pf_file_name = '2021/ZDB-PUB-210807-8/ZDB-PUB-210807-8-210807-8.docx'
     and pf.pf_file_type_id = (select pft_pk_id from publication_file_type where pft_type = 'Supplemental Material')
     and exists (select 'x' from publication_file o
                  where o.pf_pub_zdb_id = pf.pf_pub_zdb_id and o.pf_file_name = pf.pf_file_name
                    and o.pf_file_type_id = (select pft_pk_id from publication_file_type where pft_type = 'Other'))
  returning pf.pf_pub_zdb_id, pf.pf_file_name
), retyped as (
  update publication_file pf
     set pf_file_type_id = (select pft_pk_id from publication_file_type where pft_type = 'Correspondence Details')
   where pf.pf_pub_zdb_id = 'ZDB-PUB-210807-8'
     and pf.pf_file_name = '2021/ZDB-PUB-210807-8/ZDB-PUB-210807-8-210807-8.docx'
     and pf.pf_file_type_id = (select pft_pk_id from publication_file_type where pft_type = 'Other')
     and exists (select 'x' from dropped)
  returning pf.pf_pub_zdb_id, pf.pf_file_name
), log_drop as (
  insert into updates (rec_id, field_name, old_value, comments, upd_when)
  select pf_pub_zdb_id, 'publication file', pf_file_name || ' (Supplemental Material)',
         'Delete file: duplicate of the same file, which is Correspondence Details (ZFIN-10579 migration)', now()
    from dropped
)
insert into updates (rec_id, field_name, old_value, new_value, comments, upd_when)
select pf_pub_zdb_id, 'publication file', pf_file_name || ' (Other)', pf_file_name || ' (Correspondence Details)',
       'Edit file type: Other to Correspondence Details (ZFIN-10579 migration)', now()
  from retyped;
--rollback empty

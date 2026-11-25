--liquibase formatted sql
--changeset rtaylor:ZFIN-10503-fix-abregistry-ids

-- ZFIN-10503: repair Antibody Registry IDs in db_link, 50 fixes.
--
--   normalize (2): IDs stored in a non-standard form whose normalized ID is in the registry's bulk
--     export (CC-0, downloaded 2026-11-23): RRID:AB_1603723 -> AB_1603723 (Ab1-tau5), Ab_2763643 -> AB_2763643 (Ab1-wbp11).
--   replace (48): IDs the registry retired when it consolidated records. Each replacement is the
--     record whose registry comment names the old ID ("Consolidation 6/2023: AB_572253", "consolidated with AB_528173"),
--     found with the registry's search API on 2026-11-23, with a single claimant, the replacement present in the export,
--     and not already on a different ZFIN antibody.
-- Dead IDs with no such replacement are NOT changed here; they are on the curator review list.
--
-- Each fix updates dblink_acc_num in place (nothing references these db_links; the db_link trigger refreshes
-- dblink_acc_num_display) and adds a data_note on the antibody, authored by ZDB-PERS-210917-1 (see "note_author"
-- below), in the form the curator used for Ab1-npy, saying the fix was automatic:
--   normalize:  Changed Antibody registry ID from X to Y. Automatically fixed as part of ZFIN-10503.
--   replace:    Changed Antibody registry ID from X to Y (consolidated by the Antibody Registry). Automatically fixed as part of ZFIN-10503.
--   duplicate:  Removed Antibody registry ID X; the antibody already has Y. Automatically fixed as part of ZFIN-10503.
-- If the antibody already has a link to the new ID (unique key: antibody, accession, foreign_db_contains), the old
-- db_link is deleted instead ("duplicate"). Every row is guarded on its old value, so a rerun, or a curator having
-- fixed the row already, changes nothing.

with fixes(atb, old_id, new_id, kind) as (
    values ('ZDB-ATB-260515-1', 'RRID:AB_1603723', 'AB_1603723', 'normalize'),
           ('ZDB-ATB-261006-1', 'Ab_2763643', 'AB_2763643', 'normalize'),
           ('ZDB-ATB-081120-1', 'AB_2288840', 'AB_330337', 'replace'),
           ('ZDB-ATB-081120-3', 'AB_330329', 'AB_330330', 'replace'),
           ('ZDB-ATB-081124-3', 'AB_528173', 'AB_2314683', 'replace'),
           ('ZDB-ATB-081201-1', 'AB_528480', 'AB_2315387', 'replace'),
           ('ZDB-ATB-081229-12', 'AB_2118011', 'AB_2118010', 'replace'),
           ('ZDB-ATB-081229-6', 'AB_331492', 'AB_10831817', 'replace'),
           ('ZDB-ATB-090106-1', 'AB_331640', 'AB_331641', 'replace'),
           ('ZDB-ATB-090615-1', 'AB_2200566', 'AB_887805', 'replace'),
           ('ZDB-ATB-090623-3', 'AB_331593', 'AB_2315225', 'replace'),
           ('ZDB-ATB-100108-3', 'AB_331611', 'AB_2314204', 'replace'),
           ('ZDB-ATB-100728-2', 'AB_331675', 'AB_10698742', 'replace'),
           ('ZDB-ATB-100903-3', 'AB_2109815', 'AB_11212597', 'replace'),
           ('ZDB-ATB-110203-1', 'AB_331440', 'AB_2341188', 'replace'),
           ('ZDB-ATB-110802-2', 'AB_560837', 'AB_560835', 'replace'),
           ('ZDB-ATB-110822-1', 'AB_331790', 'AB_2285392', 'replace'),
           ('ZDB-ATB-120726-4', 'AB_836882', 'AB_2616028', 'replace'),
           ('ZDB-ATB-120820-1', 'AB_10013822', 'AB_2341193', 'replace'),
           ('ZDB-ATB-121101-2', 'AB_94235', 'AB_2223041', 'replace'),
           ('ZDB-ATB-121128-1', 'AB_331534', 'AB_331535', 'replace'),
           ('ZDB-ATB-121128-3', 'AB_10013749', 'AB_10835213', 'replace'),
           ('ZDB-ATB-121207-4', 'AB_330955', 'AB_823488', 'replace'),
           ('ZDB-ATB-130128-3', 'AB_331169', 'AB_331168', 'replace'),
           ('ZDB-ATB-130717-2', 'AB_2262427', 'AB_330232', 'replace'),
           ('ZDB-ATB-131120-5', 'AB_2269801', 'AB_2269803', 'replace'),
           ('ZDB-ATB-140115-2', 'AB_1031003', 'AB_561053', 'replace'),
           ('ZDB-ATB-140502-2', 'AB_2314825', 'AB_331783', 'replace'),
           ('ZDB-ATB-141119-3', 'AB_330945', 'AB_330944', 'replace'),
           ('ZDB-ATB-150407-5', 'AB_630834', 'AB_630836', 'replace'),
           ('ZDB-ATB-150721-2', 'AB_915785', 'AB_915783', 'replace'),
           ('ZDB-ATB-150723-2', 'AB_2235073', 'AB_10695739', 'replace'),
           ('ZDB-ATB-150811-1', 'AB_2129572', 'AB_2129575', 'replace'),
           ('ZDB-ATB-150812-1', 'AB_2314371', 'AB_528219', 'replace'),
           ('ZDB-ATB-150929-1', 'AB_823530', 'AB_10205451', 'replace'),
           ('ZDB-ATB-160224-1', 'AB_2085876', 'AB_2561044', 'replace'),
           ('ZDB-ATB-160314-4', 'AB_10099880', 'AB_310342', 'replace'),
           ('ZDB-ATB-160511-1', 'AB_10830895', 'AB_330947', 'replace'),
           ('ZDB-ATB-161110-3', 'AB_11178381', 'AB_11179085', 'replace'),
           ('ZDB-ATB-161219-1', 'AB_329836', 'AB_10694066', 'replace'),
           ('ZDB-ATB-161219-3', 'AB_2163703', 'AB_330855', 'replace'),
           ('ZDB-ATB-170109-3', 'AB_329920', 'AB_10694844', 'replace'),
           ('ZDB-ATB-170703-1', 'AB_1903925', 'AB_1903923', 'replace'),
           ('ZDB-ATB-170703-4', 'AB_1549596', 'AB_10695860', 'replace'),
           ('ZDB-ATB-171009-3', 'AB_659829', 'AB_10698601', 'replace'),
           ('ZDB-ATB-171218-1', 'AB_916182', 'AB_10235585', 'replace'),
           ('ZDB-ATB-180118-1', 'AB_331667', 'AB_10692100', 'replace'),
           ('ZDB-ATB-180327-4', 'AB_2074655', 'AB_331460', 'replace'),
           ('ZDB-ATB-180913-1', 'AB_2231885', 'AB_2262884', 'replace'),
           ('ZDB-ATB-190416-1', 'AB_1080574', 'AB_611880', 'replace')
),
note_author(person_zdb_id) as (
    values ('ZDB-PERS-210917-1')
),
todo as (
    select d.dblink_zdb_id, d.dblink_linked_recid as atb, f.old_id, f.new_id, f.kind,
           exists (select 1
                     from db_link x
                    where x.dblink_linked_recid = d.dblink_linked_recid
                      and x.dblink_acc_num = f.new_id
                      and x.dblink_fdbcont_zdb_id = d.dblink_fdbcont_zdb_id) as already_has_new
      from fixes f
      join db_link d on d.dblink_linked_recid = f.atb and d.dblink_acc_num = f.old_id
      join foreign_db_contains c on c.fdbcont_zdb_id = d.dblink_fdbcont_zdb_id
      join foreign_db fdb on fdb.fdb_db_pk_id = c.fdbcont_fdb_db_id and fdb.fdb_db_name = 'ABRegistry'
),
add_note as (
    insert into data_note (dnote_zdb_id, dnote_data_zdb_id, dnote_curator_zdb_id, dnote_date, dnote_text)
    select get_id_and_insert_active_data('DNOTE'), t.atb, (select person_zdb_id from note_author), now(),
           case when t.already_has_new
                then 'Removed Antibody registry ID ' || t.old_id || '; the antibody already has ' || t.new_id
                     || '. Automatically fixed as part of ZFIN-10503.'
                when t.kind = 'replace'
                then 'Changed Antibody registry ID from ' || t.old_id || ' to ' || t.new_id
                     || ' (consolidated by the Antibody Registry). Automatically fixed as part of ZFIN-10503.'
                else 'Changed Antibody registry ID from ' || t.old_id || ' to ' || t.new_id
                     || '. Automatically fixed as part of ZFIN-10503.'
           end
      from todo t
    returning 1
),
remove_duplicate as (
    delete from zdb_active_data
     where zactvd_zdb_id in (select dblink_zdb_id from todo where already_has_new)
    returning 1
)
update db_link d
   set dblink_acc_num = t.new_id
  from todo t
 where d.dblink_zdb_id = t.dblink_zdb_id
   and not t.already_has_new;

-- Rollback works from the notes, which carry the old and new IDs: recreate links that were deleted as duplicates,
-- put updated links back, then remove the notes.
--rollback insert into db_link (dblink_zdb_id, dblink_linked_recid, dblink_acc_num, dblink_fdbcont_zdb_id)
--rollback select get_id_and_insert_active_data('DBLINK'), n.dnote_data_zdb_id, substring(n.dnote_text from 'ID ([^; ]+);'), nl.dblink_fdbcont_zdb_id
--rollback   from data_note n
--rollback   join db_link nl on nl.dblink_linked_recid = n.dnote_data_zdb_id and nl.dblink_acc_num = substring(n.dnote_text from 'already has (AB_[0-9]+)')
--rollback  where n.dnote_text like 'Removed Antibody registry ID %Automatically fixed as part of ZFIN-10503.';
--rollback update db_link d set dblink_acc_num = substring(n.dnote_text from 'from ([^ ]+) to ')
--rollback   from data_note n
--rollback  where n.dnote_text like 'Changed Antibody registry ID from %Automatically fixed as part of ZFIN-10503.'
--rollback    and d.dblink_linked_recid = n.dnote_data_zdb_id and d.dblink_acc_num = substring(n.dnote_text from ' to (AB_[0-9]+)');
--rollback delete from zdb_active_data where zactvd_zdb_id in (select dnote_zdb_id from data_note where dnote_text like '%Automatically fixed as part of ZFIN-10503.');

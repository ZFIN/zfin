--liquibase formatted sql

-- ZFIN-10305: Leyla can't delete 4 fish from retracted publication
-- ZDB-PUB-140513-391 because they're also attributed to the data-model-
-- change blanket pub ZDB-PUB-150729-10 ("Data Model Change: Sequence
-- Targeting Reagents Removed from Environment"). Dissociating the 4 fish
-- from the blanket pub unblocks the normal pub-curation delete flow.
--
-- The 2 MRELs (ZDB-MREL-140811-1, -140811-2) and 4 MRPHLNOs
-- (ZDB-MRPHLNO-140725-1/-2 and -140811-1/-2) listed in the same ticket
-- are only attributed to the retracted pub and need no SQL — Leyla
-- removes them through the curation UI on ZDB-PUB-140513-391.
--
-- splitStatements:false because the CTE-driven DELETE+INSERT must run
-- as a single statement so the updates rows can only land if the
-- record_attribution rows actually went away.

--changeset cmpich:ZFIN-10305-dissociate-fish-from-blanket-pub splitStatements:false
WITH removed AS (
    DELETE FROM record_attribution
    WHERE  recattrib_data_zdb_id IN (
               'ZDB-FISH-150901-21061',
               'ZDB-FISH-150901-23817',
               'ZDB-FISH-150901-26480',
               'ZDB-FISH-150901-6497')
      AND  recattrib_source_zdb_id = 'ZDB-PUB-150729-10'
    RETURNING recattrib_data_zdb_id   AS data_zdb_id,
              recattrib_source_zdb_id AS pub_zdb_id
)
INSERT INTO updates (
        submitter_id, submitter_name, rec_id, field_name,
        new_value, old_value, comments, upd_when)
SELECT  'ZDB-PERS-040722-4',          -- Ruzicka, Leyla (ticket reporter)
        'Ruzicka, Leyla',
        data_zdb_id,
        'record attribution',
        NULL,
        pub_zdb_id,
        'ZFIN-10305: dissociated from ZDB-PUB-150729-10 so the retracted-pub cleanup under ZDB-PUB-140513-391 can delete this fish.',
        now()
FROM removed;

-- Once Leyla deletes the MRPHLNOs through the pub-curation UI, the linked
-- MRELs cascade out of marker_relationship (FK marker → marker_relationship
-- is ON DELETE CASCADE), but their rows in record_attribution survive
-- because recattrib_data_zdb_id is a free-text string with no FK. Those
-- orphaned attributions still show up in the pub's "Directly Attributed
-- Data" panel pointing at MREL ZDB-IDs that no longer resolve. Sweep them.

--changeset cmpich:ZFIN-10305-remove-orphaned-mrel-attributions splitStatements:false
WITH removed AS (
    DELETE FROM record_attribution
    WHERE  recattrib_data_zdb_id   IN ('ZDB-MREL-140811-1','ZDB-MREL-140811-2')
      AND  recattrib_source_zdb_id = 'ZDB-PUB-140513-391'
    RETURNING recattrib_data_zdb_id   AS data_zdb_id,
              recattrib_source_zdb_id AS pub_zdb_id
)
INSERT INTO updates (
        submitter_id, submitter_name, rec_id, field_name,
        new_value, old_value, comments, upd_when)
SELECT  'ZDB-PERS-040722-4',
        'Ruzicka, Leyla',
        data_zdb_id,
        'record attribution',
        NULL,
        pub_zdb_id,
        'ZFIN-10305: removed orphaned attribution to ZDB-PUB-140513-391 — the MREL was cascade-deleted when its linked MRPHLNO was deleted.',
        now()
FROM removed;

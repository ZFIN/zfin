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

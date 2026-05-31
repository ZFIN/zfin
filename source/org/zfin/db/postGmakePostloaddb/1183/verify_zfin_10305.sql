-- ZFIN-10305 post-migration verification.
--
-- Run with `psql -v ON_ERROR_STOP=1 -f verify_zfin_10305.sql` — the script
-- exits non-zero on the first failing assertion. Silent success means
-- both changesets in 0010-ZFIN-10305-…sql landed as intended.

\set ON_ERROR_STOP on

-- ─── 1. blanket-pub blockers are gone for the 4 fish ──────────────────
DO $$
DECLARE n int;
BEGIN
    SELECT count(*) INTO n
    FROM   record_attribution
    WHERE  recattrib_data_zdb_id IN (
               'ZDB-FISH-150901-21061','ZDB-FISH-150901-23817',
               'ZDB-FISH-150901-26480','ZDB-FISH-150901-6497')
      AND  recattrib_source_zdb_id = 'ZDB-PUB-150729-10';
    IF n <> 0 THEN
        RAISE EXCEPTION
            'ZFIN-10305 check 1 failed: % blanket-pub attributions still on the 4 fish (expected 0)', n;
    END IF;
END $$;

-- ─── 2. orphaned MREL attributions on the retracted pub are gone ──────
DO $$
DECLARE n int;
BEGIN
    SELECT count(*) INTO n
    FROM   record_attribution
    WHERE  recattrib_data_zdb_id   IN ('ZDB-MREL-140811-1','ZDB-MREL-140811-2')
      AND  recattrib_source_zdb_id = 'ZDB-PUB-140513-391';
    IF n <> 0 THEN
        RAISE EXCEPTION
            'ZFIN-10305 check 2 failed: % MREL attributions still on the retracted pub (expected 0)', n;
    END IF;
END $$;

-- ─── 3. audit trail: 4 fish dissociation rows in updates ──────────────
DO $$
DECLARE n int;
BEGIN
    SELECT count(*) INTO n
    FROM   updates
    WHERE  comments LIKE '%ZFIN-10305%'
      AND  rec_id IN ('ZDB-FISH-150901-21061','ZDB-FISH-150901-23817',
                       'ZDB-FISH-150901-26480','ZDB-FISH-150901-6497')
      AND  old_value = 'ZDB-PUB-150729-10';
    IF n <> 4 THEN
        RAISE EXCEPTION
            'ZFIN-10305 check 3 failed: % fish-dissociation audit rows (expected 4)', n;
    END IF;
END $$;

-- ─── 4. audit trail: 2 MREL cleanup rows in updates ───────────────────
DO $$
DECLARE n int;
BEGIN
    SELECT count(*) INTO n
    FROM   updates
    WHERE  comments LIKE '%ZFIN-10305%'
      AND  rec_id IN ('ZDB-MREL-140811-1','ZDB-MREL-140811-2')
      AND  old_value = 'ZDB-PUB-140513-391';
    IF n <> 2 THEN
        RAISE EXCEPTION
            'ZFIN-10305 check 4 failed: % MREL-cleanup audit rows (expected 2)', n;
    END IF;
END $$;

-- ─── 5. retracted-pub attribution still on the 4 fish (so the manual ──
--      deletion through pub-curation UI can still find them) ──────────
DO $$
DECLARE n int;
BEGIN
    SELECT count(*) INTO n
    FROM   record_attribution
    WHERE  recattrib_data_zdb_id IN (
               'ZDB-FISH-150901-21061','ZDB-FISH-150901-23817',
               'ZDB-FISH-150901-26480','ZDB-FISH-150901-6497')
      AND  recattrib_source_zdb_id = 'ZDB-PUB-140513-391';
    -- Once Leyla actually deletes the fish in the UI this drops to 0;
    -- we accept either 0 or 4, but never something in between, which
    -- would indicate a partial UI delete.
    IF n NOT IN (0, 4) THEN
        RAISE EXCEPTION
            'ZFIN-10305 check 5 failed: % retracted-pub attributions on the fish (expected 0 or 4)', n;
    END IF;
END $$;

-- ─── Summary readout (informational; never fails) ─────────────────────
SELECT 'ZFIN-10305 PASS' AS result;

SELECT 'audit log' AS section, rec_id, old_value, left(comments, 60) AS comments
FROM   updates
WHERE  comments LIKE '%ZFIN-10305%'
ORDER  BY rec_id;

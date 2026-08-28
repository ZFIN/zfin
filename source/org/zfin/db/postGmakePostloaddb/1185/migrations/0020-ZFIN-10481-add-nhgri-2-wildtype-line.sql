--liquibase formatted sql

-- ZFIN-10481: add the NHGRI-2 wild type line requested by ZIRC (Amy Singer).
--
-- Modelled on NHGRI-1 (ZDB-GENO-150204-3) rather than the AB record linked in the
-- ticket: NHGRI-1 is the same naming family, uses the same nhgri-<n> sort key, and
-- carries its provenance text in an external_note exactly as requested here. AB
-- differs (plain 'ab' sort key, a supplier stock number) and would have produced an
-- inconsistent record.
--
-- Three rows, matching NHGRI-1's footprint for a wild type line:
--   genotype       the line itself (no genotype_feature / genotype_background rows:
--                  a wild type line has neither)
--   external_note  the description, note type 'genotype'
--   fish           the base wild type fish, so the line is usable in annotations
--
-- Values left to database triggers rather than hard-coded:
--   geno_name_order       zero_pad() turns 'nhgri-2' into 'nhgri-0000000002'
--   geno_complexity_order recomputed by update_geno_sort_order()
--   geno_display_name     scrub_char()
--   geno_handle           scrub_char()
--   fish_name_order       zero_pad(fish_name)
--   fish_full_name        copied from fish_name
-- The columns are still supplied because they are NOT NULL; the triggers overwrite
-- them on the way in.
--
-- fish_order is 10000000024 because that is the constant wild type band value: all
-- 32 existing wild type fish share it, it is not allocated per row.
--
-- extnote_source_zdb_id is NOT NULL and the ticket cites no publication, so the note
-- is sourced to ZDB-PUB-020723-5 "Scientific Curation" -- the generic curation pub
-- already attributed to NHGRI-1. Four other wild type lines instead use
-- ZDB-PUB-160331-1, but that is titled "Mutation Details Curation of Older Features",
-- which does not describe a newly added line. Swap the id if curators prefer the
-- precedent over the accurate title.
--
-- No record_attribution row: the ticket states there is no publication.
--
-- Guarded on the handle, so re-running is a no-op.

--changeset cmpich:ZFIN-10481-add-nhgri-2-wildtype-line splitStatements:false
DO $$
DECLARE
    v_geno_id text;
    v_fish_id text;
BEGIN
    IF EXISTS (SELECT 1 FROM genotype WHERE geno_handle = 'NHGRI-2') THEN
        RAISE NOTICE 'NHGRI-2 already present, skipping';
        RETURN;
    END IF;

    v_geno_id := get_id_and_insert_active_data('GENO');

    INSERT INTO genotype (geno_zdb_id, geno_display_name, geno_handle, geno_nickname,
                          geno_is_wildtype, geno_is_extinct, geno_name_order,
                          geno_complexity_order, geno_date_entered)
    VALUES (v_geno_id, 'NHGRI-2', 'NHGRI-2', 'NHGRI-2',
            true, false, 'nhgri-2',
            999999999, now());

    INSERT INTO external_note (extnote_zdb_id, extnote_data_zdb_id, extnote_note,
                               extnote_note_type, extnote_source_zdb_id)
    VALUES (get_id_and_insert_active_data('EXTNOTE'), v_geno_id,
            'The line is descended from one fish from each cross of male WIK x female TL '
            || 'and male TU x female AB, which were then crossed multiple times to generate '
            || 'a large cohort of fish that represent a mix of one haplotype from each background.',
            'genotype', 'ZDB-PUB-020723-5');

    v_fish_id := get_id_and_insert_active_data('FISH');

    INSERT INTO fish (fish_zdb_id, fish_genotype_zdb_id, fish_name, fish_handle,
                      fish_name_order, fish_order, fish_functional_affected_gene_count,
                      fish_is_wildtype, fish_full_name, fish_phenotypic_construct_count,
                      fish_modified)
    VALUES (v_fish_id, v_geno_id, 'NHGRI-2', 'NHGRI-2',
            'nhgri-2', 10000000024, 0,
            true, 'NHGRI-2', 0,
            false);

    RAISE NOTICE 'NHGRI-2 created: genotype=% fish=%', v_geno_id, v_fish_id;
END $$;

--rollback DELETE FROM fish WHERE fish_genotype_zdb_id IN (SELECT geno_zdb_id FROM genotype WHERE geno_handle = 'NHGRI-2');
--rollback DELETE FROM external_note WHERE extnote_data_zdb_id IN (SELECT geno_zdb_id FROM genotype WHERE geno_handle = 'NHGRI-2');
--rollback DELETE FROM zdb_active_data WHERE zactvd_zdb_id IN (SELECT geno_zdb_id FROM genotype WHERE geno_handle = 'NHGRI-2');
--rollback DELETE FROM genotype WHERE geno_handle = 'NHGRI-2';

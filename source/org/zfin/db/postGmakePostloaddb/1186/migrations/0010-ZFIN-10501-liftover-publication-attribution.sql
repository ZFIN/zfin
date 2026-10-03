--liquibase formatted sql

-- ZFIN-10501: the GRCz11 -> GRCz12tu liftover copied each location forward and
-- brought the ORIGINAL submitting publication with it, so Mapping Details
-- credits the paper the GRCz11 coordinates came from rather than the liftover.
-- a289 is the example on the ticket:
--
--   GRCz11    ZDB-SFCL-200218-13    pub ZDB-PUB-190402-7   (Thyme et al., 2019)
--   GRCz12tu  ZDB-SFCL-260731-2131  pub ZDB-PUB-190402-7   <- should be the liftover pub
--
-- Re-attributes the lifted rows to ZDB-PUB-261001-17. The Source column is
-- deliberately left alone: it still reads DIRECT, which matches the precedent
-- set by the 2025-10-22 bulk load (36,268 rows, source DIRECT, attributed to
-- the single load pub ZDB-PUB-250905-18).
--
-- Identifying the lifted rows needs care, because none of the obvious signals
-- work:
--
--   * Assembly does not, because features are also curated directly against
--     GRCz12tu.
--   * The evidence code does not, because the liftover inherits it verbatim
--     from the source row -- 5,241 rows are 251-from-251 and 60 are 250-from-250
--     with no mixed cases, so it carries no information about being lifted.
--     (ZDB-TERM-170419-312 "automatic" does exist on GRCz12tu rows, but those
--     are the separate 2026-09-08 netnew insert, not this batch.)
--   * The ZDB ID date prefix alone does not, because one genuine direct
--     submission landed the same day -- see the exclusion below.
--
-- What does hold is "the same feature also has a GRCz11 location", which is
-- what being lifted means. Combined with the batch's ID date prefix it selects
-- 5,301 of the 5,302 rows inserted that day.
--
-- The one row excluded is ZDB-SFCL-260731-1 (hza24, ZDB-ALT-260731-1): a new
-- allele curated directly against GRCz12tu on the same day the liftover ran,
-- with no GRCz11 row to have been lifted from. Its attribution
-- (ZDB-PUB-260715-17) is correct and must not be touched. The predicate
-- excludes it without special-casing.

--changeset cmpich:ZFIN-10501-liftover-publication-attribution
--preconditions onFail:HALT onError:HALT
--precondition-sql-check expectedResult:1 SELECT count(*) FROM publication WHERE zdb_id = 'ZDB-PUB-261001-17'
--comment Re-attribute GRCz11->GRCz12tu lifted locations to the liftover publication

-- The precondition above is not a formality. ZDB-PUB-261001-17 was created on
-- 2026-10-01, after this work started, so a database restored from an older
-- snapshot will not have it -- and without the check the INSERT below would
-- fail on the record_attribution -> publication foreign key halfway through,
-- after the DELETE had already run.

-- Drop the inherited attributions. 5,301 locations carry 5,382 attribution
-- rows between them: 5,221 have one publication, 79 have two and one has
-- three. All of them are inherited from the GRCz11 source row, so all of them
-- go -- the liftover pub replaces the lot rather than being added alongside.
DELETE FROM record_attribution
 WHERE recattrib_data_zdb_id IN (
         SELECT l.sfcl_zdb_id
           FROM sequence_feature_chromosome_location l
          WHERE l.sfcl_assembly = 'GRCz12tu'
            AND l.sfcl_zdb_id LIKE 'ZDB-SFCL-260731-%'
            AND EXISTS (SELECT 1
                          FROM sequence_feature_chromosome_location z11
                         WHERE z11.sfcl_feature_zdb_id = l.sfcl_feature_zdb_id
                           AND z11.sfcl_assembly = 'GRCz11')
       );

-- recattrib_pk_id comes from its own sequence, so it is not listed.
-- 'standard' is the only recattrib_source_type in use on these locations.
-- ON CONFLICT guards the unique key on
-- (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type)
-- so a re-run is harmless.
INSERT INTO record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type)
SELECT l.sfcl_zdb_id, 'ZDB-PUB-261001-17', 'standard'
  FROM sequence_feature_chromosome_location l
 WHERE l.sfcl_assembly = 'GRCz12tu'
   AND l.sfcl_zdb_id LIKE 'ZDB-SFCL-260731-%'
   AND EXISTS (SELECT 1
                 FROM sequence_feature_chromosome_location z11
                WHERE z11.sfcl_feature_zdb_id = l.sfcl_feature_zdb_id
                  AND z11.sfcl_assembly = 'GRCz11')
ON CONFLICT (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type) DO NOTHING;

-- No rollback. The DELETE discards which publication each location used to
-- carry, and that mapping is not recoverable from anything this changeset
-- leaves behind. It IS recoverable from the corresponding GRCz11 row, which is
-- where the liftover took it from in the first place -- so an undo is possible,
-- but as a deliberate re-derivation rather than something Liquibase should do
-- automatically on a rollback.
--rollback empty

-- The Mapping Details page reads sequence_feature_chromosome_location_generated
-- (via FeatureGenomeLocation.sfclg_pub_zdb_id), not record_attribution directly.
-- That table is rebuilt by Refresh-GBrowse-Tracks_d, whose section G re-reads
-- record_attribution -- so the display changes on the next successful run of
-- that job, not when this changeset applies.

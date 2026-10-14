--liquibase formatted sql

-- ZFIN-10547: let curators add Ensembl gene IDs for the GRCz12tu, GRCz12ab and
-- GRCz13ab assemblies in the gene page's Genome Resources section.
--
-- The "Add Genome Resource" dialog (MarkerEditDbLinks) offers every
-- foreign_db_contains row in the "other marker pages" display group, and the
-- gene page shows those in "summary page". So each assembly needs the same
-- three pieces Ensembl(GRCz11) (ZDB-FDBCONT-061018-1) has: a foreign_db row,
-- a zebrafish foreign_db_contains row of data type other / summary page, and
-- membership in both groups with view/add/edit/delete enabled.
--
-- The names must also exist in ForeignDB.AvailableName; Hibernate maps
-- fdb_db_name onto that enum, and EnumValidationService checks the two agree.
--
-- Links use Ensembl's stable-ID resolver (https://www.ensembl.org/id/), the
-- same prefix as the plain Ensembl entry, for now. No assembly-specific gene
-- page URL is known yet; update fdb_db_query once one is.

--changeset cmpich:ZFIN-10547-ensembl-genome-resource-assemblies
--comment Add Ensembl(GRCz12tu), Ensembl(GRCz12ab) and Ensembl(GRCz13ab) as gene Genome Resource databases

-- The pk sequence trails the table (last_value 97, max 98 on a 2026-09 copy),
-- so nextval would collide. Move it to the larger of the two, never backwards.
SELECT setval('foreign_db_fdb_db_pk_id_seq',
              GREATEST((SELECT max(fdb_db_pk_id) FROM foreign_db),
                       (SELECT last_value FROM foreign_db_fdb_db_pk_id_seq)));

INSERT INTO foreign_db (fdb_db_name, fdb_db_display_name, fdb_db_query, fdb_db_significance)
SELECT v.name, v.name, 'https://www.ensembl.org/id/', 3
  FROM (VALUES ('Ensembl(GRCz12tu)'), ('Ensembl(GRCz12ab)'), ('Ensembl(GRCz13ab)')) AS v(name)
 WHERE NOT EXISTS (SELECT 1 FROM foreign_db f WHERE f.fdb_db_name = v.name);

-- Two data types are named "other"; Ensembl(GRCz11) uses the summary page one.
INSERT INTO foreign_db_contains (fdbcont_zdb_id, fdbcont_organism_common_name, fdbcont_fdbdt_id, fdbcont_fdb_db_id)
SELECT get_id_and_insert_active_data('FDBCONT'), 'Zebrafish', dt.fdbdt_pk_id, f.fdb_db_pk_id
  FROM foreign_db f
  JOIN foreign_db_data_type dt
    ON dt.fdbdt_data_type = 'other' AND dt.fdbdt_super_type = 'summary page'
 WHERE f.fdb_db_name IN ('Ensembl(GRCz12tu)', 'Ensembl(GRCz12ab)', 'Ensembl(GRCz13ab)')
   AND NOT EXISTS (SELECT 1 FROM foreign_db_contains c
                    WHERE c.fdbcont_fdb_db_id = f.fdb_db_pk_id
                      AND c.fdbcont_fdbdt_id = dt.fdbdt_pk_id
                      AND c.fdbcont_organism_common_name = 'Zebrafish');

-- can_view/add/edit/delete default to true, matching Ensembl(GRCz11).
INSERT INTO foreign_db_contains_display_group_member (fdbcdgm_fdbcont_zdb_id, fdbcdgm_group_id)
SELECT c.fdbcont_zdb_id, g.fdbcdg_pk_id
  FROM foreign_db_contains c
  JOIN foreign_db f ON f.fdb_db_pk_id = c.fdbcont_fdb_db_id
  JOIN foreign_db_contains_display_group g ON g.fdbcdg_name IN ('other marker pages', 'summary page')
 WHERE f.fdb_db_name IN ('Ensembl(GRCz12tu)', 'Ensembl(GRCz12ab)', 'Ensembl(GRCz13ab)')
   AND c.fdbcont_organism_common_name = 'Zebrafish'
   AND NOT EXISTS (SELECT 1 FROM foreign_db_contains_display_group_member m
                    WHERE m.fdbcdgm_fdbcont_zdb_id = c.fdbcont_zdb_id
                      AND m.fdbcdgm_group_id = g.fdbcdg_pk_id);

--rollback DELETE FROM foreign_db_contains_display_group_member WHERE fdbcdgm_fdbcont_zdb_id IN (SELECT c.fdbcont_zdb_id FROM foreign_db_contains c JOIN foreign_db f ON f.fdb_db_pk_id = c.fdbcont_fdb_db_id WHERE f.fdb_db_name IN ('Ensembl(GRCz12tu)', 'Ensembl(GRCz12ab)', 'Ensembl(GRCz13ab)'));
--rollback DELETE FROM zdb_active_data WHERE zactvd_zdb_id IN (SELECT c.fdbcont_zdb_id FROM foreign_db_contains c JOIN foreign_db f ON f.fdb_db_pk_id = c.fdbcont_fdb_db_id WHERE f.fdb_db_name IN ('Ensembl(GRCz12tu)', 'Ensembl(GRCz12ab)', 'Ensembl(GRCz13ab)'));
--rollback DELETE FROM foreign_db_contains WHERE fdbcont_fdb_db_id IN (SELECT fdb_db_pk_id FROM foreign_db WHERE fdb_db_name IN ('Ensembl(GRCz12tu)', 'Ensembl(GRCz12ab)', 'Ensembl(GRCz13ab)'));
--rollback DELETE FROM foreign_db WHERE fdb_db_name IN ('Ensembl(GRCz12tu)', 'Ensembl(GRCz12ab)', 'Ensembl(GRCz13ab)');

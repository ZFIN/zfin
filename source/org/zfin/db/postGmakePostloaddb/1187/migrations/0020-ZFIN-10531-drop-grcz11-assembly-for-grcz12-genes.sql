--liquibase formatted sql

-- ZFIN-10531: the gene page's Genome Assembly field should show GRCz11 only when
-- the gene is on no GRCz12 assembly. The field is read from marker_assembly, which
-- today links 22,347 genes to both GRCz12tu and GRCz11 (on a 2026-09 copy): the
-- GRCz11 rows predate the GRCz12tu ones and nothing removed them once a gene was
-- placed on GRCz12tu.
--
-- This drops those GRCz11 rows once. Going forward the loads keep it that way:
-- markerAssemblyUpdate.sql no longer adds GRCz11 to a gene with a GRCz12 assembly
-- and removes it when one is added, and NCBIGff3Processor removes GRCz11 when it
-- adds GRCz12tu.
--
-- Matched on assembly name rather than a_pk_id, and on "GRCz12%" so GRCz12ab is
-- covered once that assembly is loaded.

--changeset cmpich:ZFIN-10531-drop-grcz11-assembly-for-grcz12-genes
--comment Remove the GRCz11 marker_assembly link from genes that are on a GRCz12 assembly

DELETE FROM marker_assembly AS ma
 USING assembly AS a
 WHERE a.a_pk_id = ma.ma_a_pk_id
   AND a.a_name = 'GRCz11'
   AND EXISTS (SELECT 1
                 FROM marker_assembly AS z12
                 JOIN assembly AS a12 ON a12.a_pk_id = z12.ma_a_pk_id
                WHERE z12.ma_mrkr_zdb_id = ma.ma_mrkr_zdb_id
                  AND a12.a_name LIKE 'GRCz12%');

-- No automatic rollback: which genes carried a GRCz11 row is not kept anywhere
-- else. It can be re-derived the way markerAssemblyUpdate.sql originally derived
-- it (an Ensembl ENSDARG GRCz11 location), but that is a deliberate re-run, not
-- something Liquibase should do on its own.
--rollback empty

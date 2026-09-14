--liquibase formatted sql
--changeset rtaylor:0020-ZFIN-10461-gff3-ncbi-pk-sequences.sql

-- gff3_ncbi and gff3_ncbi_attribute each have two sequences for their primary key, and the
-- column default points at the wrong one.
--
-- 1171/gff3-ncbi.sql declared both columns `serial`, which implicitly creates
-- gff3_ncbi_gff_pk_id_seq and gff3_ncbi_attribute_gna_pk_id_seq, and then separately created
-- gff3_ncbi_seq and gff3_ncbi_attribute_seq. The entities (Gff3Ncbi, Gff3NcbiAttributePair)
-- allocate from the explicitly created pair via @GenericGenerator, so those track the tables.
-- The implicit ones have never been used and never advanced:
--
--   gff3_ncbi_attribute    max 12,581,191   gff3_ncbi_attribute_seq 12,581,191   default seq   580
--   gff3_ncbi              max  3,194,107   gff3_ncbi_seq            3,194,107   default seq     1
--
-- Any SQL insert that omits the key therefore gets one that is already taken.
-- markerAssemblyUpdate.sql did exactly that, and its ON CONFLICT (gna_pk_id) DO NOTHING
-- swallowed the collision - so on any database whose low ids are occupied the statement
-- silently inserted nothing. It surfaced only because the GFF3 load truncates these tables
-- and refills them from the high end, which frees the low range and hides the problem until
-- a load runs against a restored database.
--
-- Point each default at the sequence actually in use and drop the unused one. nextval is
-- atomic, so Hibernate and plain SQL can share a sequence safely.

ALTER TABLE gff3_ncbi_attribute
    ALTER COLUMN gna_pk_id SET DEFAULT nextval('gff3_ncbi_attribute_seq');
DROP SEQUENCE IF EXISTS gff3_ncbi_attribute_gna_pk_id_seq;

ALTER TABLE gff3_ncbi
    ALTER COLUMN gff_pk_id SET DEFAULT nextval('gff3_ncbi_seq');
DROP SEQUENCE IF EXISTS gff3_ncbi_gff_pk_id_seq;

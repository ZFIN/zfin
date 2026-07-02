--liquibase formatted sql
--changeset cmpich:0010-grcz11-sfcl-chromosome-accessions.sql

-- Normalize sfcl_chromosome_reference_accession_number for GRCz11 feature
-- locations so the accession always agrees with the chromosome.
--
-- Two problems this fixes, both in one pass:
--   1. ~2,366 GRCz11 rows had a NULL accession (curated since the original
--      2018 backfill in 1124/add_sfcl_accession_number.sql, which only stamped
--      the rows that existed then). Variant-eligible features among these were
--      being dropped from the Alliance variant export, which requires a
--      non-null reference accession.
--   2. Three rows carried an accession belonging to a DIFFERENT chromosome
--      (a single mis-curated row each): chr9 had NC_007118.7 (chr7's),
--      chr23 had NC_007133.7 (chr22's), chr25 had NC_007123.7 (chr12's).
--
-- Like the GRCz12tu changeset (1183/0040-grcz12tu-sfcl-chromosome-accessions.sql),
-- this uses NO "is null" guard: it sets every GRCz11 row of a chromosome to the
-- canonical GRCz11 (RefSeq) accession NC_(007111+n).7 for chromosome n. That
-- fills the nulls AND overwrites the stale/wrong values, and is idempotent for
-- rows already correct. MT is excluded, matching the original 1124 scope.

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007112.7'
 where sfcl_assembly = 'GRCz11' and sfcl_chromosome = '1';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007113.7'
 where sfcl_assembly = 'GRCz11' and sfcl_chromosome = '2';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007114.7'
 where sfcl_assembly = 'GRCz11' and sfcl_chromosome = '3';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007115.7'
 where sfcl_assembly = 'GRCz11' and sfcl_chromosome = '4';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007116.7'
 where sfcl_assembly = 'GRCz11' and sfcl_chromosome = '5';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007117.7'
 where sfcl_assembly = 'GRCz11' and sfcl_chromosome = '6';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007118.7'
 where sfcl_assembly = 'GRCz11' and sfcl_chromosome = '7';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007119.7'
 where sfcl_assembly = 'GRCz11' and sfcl_chromosome = '8';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007120.7'
 where sfcl_assembly = 'GRCz11' and sfcl_chromosome = '9';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007121.7'
 where sfcl_assembly = 'GRCz11' and sfcl_chromosome = '10';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007122.7'
 where sfcl_assembly = 'GRCz11' and sfcl_chromosome = '11';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007123.7'
 where sfcl_assembly = 'GRCz11' and sfcl_chromosome = '12';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007124.7'
 where sfcl_assembly = 'GRCz11' and sfcl_chromosome = '13';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007125.7'
 where sfcl_assembly = 'GRCz11' and sfcl_chromosome = '14';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007126.7'
 where sfcl_assembly = 'GRCz11' and sfcl_chromosome = '15';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007127.7'
 where sfcl_assembly = 'GRCz11' and sfcl_chromosome = '16';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007128.7'
 where sfcl_assembly = 'GRCz11' and sfcl_chromosome = '17';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007129.7'
 where sfcl_assembly = 'GRCz11' and sfcl_chromosome = '18';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007130.7'
 where sfcl_assembly = 'GRCz11' and sfcl_chromosome = '19';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007131.7'
 where sfcl_assembly = 'GRCz11' and sfcl_chromosome = '20';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007132.7'
 where sfcl_assembly = 'GRCz11' and sfcl_chromosome = '21';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007133.7'
 where sfcl_assembly = 'GRCz11' and sfcl_chromosome = '22';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007134.7'
 where sfcl_assembly = 'GRCz11' and sfcl_chromosome = '23';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007135.7'
 where sfcl_assembly = 'GRCz11' and sfcl_chromosome = '24';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007136.7'
 where sfcl_assembly = 'GRCz11' and sfcl_chromosome = '25';

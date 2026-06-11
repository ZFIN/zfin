--liquibase formatted sql
--changeset cmpich:0010-grcz12tu-sfcl-chromosome-accessions.sql

-- Backfill sfcl_chromosome_reference_accession_number for GRCz12tu feature
-- locations. The original 2018 backfill (1124/add_sfcl_accession_number.sql)
-- keyed on chromosome only, with no assembly filter, and used the GRCz11
-- RefSeq accessions (NC_0071xx.7). As a result the few GRCz12tu rows that
-- existed at that time were stamped with GRCz11 accessions, while every
-- GRCz12tu row loaded since was left null.
--
-- This changeset assigns the correct GRCz12tu (GCF_049306965.1) RefSeq
-- chromosome accessions (NC_1331(75+n).1 for chromosome n; MT is excluded,
-- matching the original 1124 scope). It does NOT use an "is null" guard:
-- rows still holding a stale GRCz11 accession are overwritten with the right
-- GRCz12tu one, so the accession always agrees with the chromosome.

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_133176.1'
 where sfcl_assembly = 'GRCz12tu' and sfcl_chromosome = '1';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_133177.1'
 where sfcl_assembly = 'GRCz12tu' and sfcl_chromosome = '2';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_133178.1'
 where sfcl_assembly = 'GRCz12tu' and sfcl_chromosome = '3';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_133179.1'
 where sfcl_assembly = 'GRCz12tu' and sfcl_chromosome = '4';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_133180.1'
 where sfcl_assembly = 'GRCz12tu' and sfcl_chromosome = '5';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_133181.1'
 where sfcl_assembly = 'GRCz12tu' and sfcl_chromosome = '6';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_133182.1'
 where sfcl_assembly = 'GRCz12tu' and sfcl_chromosome = '7';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_133183.1'
 where sfcl_assembly = 'GRCz12tu' and sfcl_chromosome = '8';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_133184.1'
 where sfcl_assembly = 'GRCz12tu' and sfcl_chromosome = '9';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_133185.1'
 where sfcl_assembly = 'GRCz12tu' and sfcl_chromosome = '10';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_133186.1'
 where sfcl_assembly = 'GRCz12tu' and sfcl_chromosome = '11';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_133187.1'
 where sfcl_assembly = 'GRCz12tu' and sfcl_chromosome = '12';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_133188.1'
 where sfcl_assembly = 'GRCz12tu' and sfcl_chromosome = '13';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_133189.1'
 where sfcl_assembly = 'GRCz12tu' and sfcl_chromosome = '14';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_133190.1'
 where sfcl_assembly = 'GRCz12tu' and sfcl_chromosome = '15';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_133191.1'
 where sfcl_assembly = 'GRCz12tu' and sfcl_chromosome = '16';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_133192.1'
 where sfcl_assembly = 'GRCz12tu' and sfcl_chromosome = '17';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_133193.1'
 where sfcl_assembly = 'GRCz12tu' and sfcl_chromosome = '18';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_133194.1'
 where sfcl_assembly = 'GRCz12tu' and sfcl_chromosome = '19';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_133195.1'
 where sfcl_assembly = 'GRCz12tu' and sfcl_chromosome = '20';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_133196.1'
 where sfcl_assembly = 'GRCz12tu' and sfcl_chromosome = '21';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_133197.1'
 where sfcl_assembly = 'GRCz12tu' and sfcl_chromosome = '22';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_133198.1'
 where sfcl_assembly = 'GRCz12tu' and sfcl_chromosome = '23';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_133199.1'
 where sfcl_assembly = 'GRCz12tu' and sfcl_chromosome = '24';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_133200.1'
 where sfcl_assembly = 'GRCz12tu' and sfcl_chromosome = '25';

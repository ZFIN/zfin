--liquibase formatted sql
--changeset sierra:add_sfcl_accession_number.sql

alter table sequence_feature_chromosome_location
 add column sfcl_chromosome_reference_accession_number text;

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007112.7'
 where sfcl_chromosome = '1';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007113.7'
 where sfcl_chromosome = '2';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007114.7'
 where sfcl_chromosome = '3';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007115.7'
 where sfcl_chromosome = '4';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007116.7'
 where sfcl_chromosome = '5';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007117.7'
 where sfcl_chromosome = '6';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007118.7'
 where sfcl_chromosome = '7';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007119.7'
 where sfcl_chromosome = '8';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007120.7'
 where sfcl_chromosome = '9';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007121.7'
 where sfcl_chromosome = '10';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007122.7'
 where sfcl_chromosome = '11';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007123.7'
 where sfcl_chromosome = '12';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007124.7'
 where sfcl_chromosome = '13';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007125.7'
 where sfcl_chromosome = '14';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007126.7'
 where sfcl_chromosome = '15';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007127.7'
 where sfcl_chromosome = '16';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007128.7'
 where sfcl_chromosome = '17';


update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007129.7'
 where sfcl_chromosome = '18';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007130.7'
 where sfcl_chromosome = '19';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007131.7'
 where sfcl_chromosome = '20';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007132.7'
 where sfcl_chromosome = '21';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007133.7'
 where sfcl_chromosome = '22';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007134.7'
 where sfcl_chromosome = '23';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007135.7'
 where sfcl_chromosome = '24';

update sequence_feature_chromosome_location
  set sfcl_chromosome_reference_accession_number = 'NC_007136.7'
 where sfcl_chromosome = '25';

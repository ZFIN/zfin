--liquibase formatted sql
--changeset cmpich:ZFIN-9839

-- GRZc12 records in sequence_feature_chromosome_location_generated for all ZMP alleles

create temp table temp_zmp_z12 as
select feature_zdb_id, assembly, chromosome, position
from feature,
     temp_zmp
where feature_abbrev = allele
  and assembly = 'GRCz12tu'
;

--sequence_feature_chromosome_location_generated
insert into sequence_feature_chromosome_location_generated (sfclg_chromosome,
                                                            sfclg_data_zdb_id,
                                                            sfclg_start,
                                                            sfclg_end,
                                                            sfclg_location_source,
                                                            sfclg_pub_zdb_id,
                                                            sfclg_assembly,
--                                                            sfclg_evidence_code,
                                                            sfclg_gbrowse_track)
select chromosome,
       feature_zdb_id,
       position::integer,
       position::integer,
       'DirectSubmission',
       'ZDB-PUB-250905-18',
       'GRCz12tu',
       'zmp'
from temp_zmp_z12
;

-- insert into sequence_feature_chromosome_location
insert into sequence_feature_chromosome_location (sfcl_zdb_id,
                                                  sfcl_feature_zdb_id,
                                                  sfcl_start_position,
                                                  sfcl_end_position,
                                                  sfcl_assembly,
                                                  sfcl_chromosome,
                                                  sfcl_evidence_code)
--                                                  sfcl_chromosome_reference_accession_number)
select get_id_and_insert_active_data('SFCL'),
       feature_zdb_id,
       position::integer,
       position::integer,
       'GRCz12tu',
       chromosome,
       'ZDB-TERM-170419-251'
from temp_zmp_z12
;

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type)
select sfcl_zdb_id, 'ZDB-PUB-250905-18', 'standard'
from sequence_feature_chromosome_location,
     temp_zmp_z12
where sfcl_assembly = 'GRCz12tu'
  AND sfcl_feature_zdb_id = feature_zdb_id and sfcl_start_position = position::integer
;


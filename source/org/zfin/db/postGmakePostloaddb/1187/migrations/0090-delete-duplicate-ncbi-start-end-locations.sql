--liquibase formatted sql

--changeset rtaylor:delete-duplicate-ncbi-start-end-locations
-- Seven genes have two NCBIStartEndLoader rows in sequence_feature_chromosome_location_generated (same gene and
-- accession, same source, assembly and evidence code, slightly different start/end). The rows were created together
-- on 2025-11-10. Load-NCBIStartEndPositions assumes one row per gene and accession: it updates every matching row to
-- the same coordinates, which makes the twins identical and fails uq_sfclg_unique_location.
--
-- One delete per gene, by explicit pk. Each deletes the higher-pk row (d) of the pair, and only if its twin (k, the
-- lower pk, which is kept) is still there with the same gene, accession and source, so a statement does nothing if
-- the rows are not what is expected. The kept row is moved to the NCBI coordinates by the next run of the loader, so
-- it does not matter which of the two is kept.

-- ZDB-GENE-030131-1059 (564377): delete 752638980, keep 281695282
delete from sequence_feature_chromosome_location_generated d
 using sequence_feature_chromosome_location_generated k
 where d.sfclg_pk_id = 752638980
   and k.sfclg_pk_id = 281695282
   and d.sfclg_data_zdb_id = 'ZDB-GENE-030131-1059'
   and k.sfclg_data_zdb_id = d.sfclg_data_zdb_id
   and d.sfclg_acc_num = '564377'
   and k.sfclg_acc_num = d.sfclg_acc_num
   and d.sfclg_location_source = 'NCBIStartEndLoader'
   and k.sfclg_location_source = d.sfclg_location_source;

-- ZDB-GENE-030131-7737 (101886382): delete 752638669, keep 281710217
delete from sequence_feature_chromosome_location_generated d
 using sequence_feature_chromosome_location_generated k
 where d.sfclg_pk_id = 752638669
   and k.sfclg_pk_id = 281710217
   and d.sfclg_data_zdb_id = 'ZDB-GENE-030131-7737'
   and k.sfclg_data_zdb_id = d.sfclg_data_zdb_id
   and d.sfclg_acc_num = '101886382'
   and k.sfclg_acc_num = d.sfclg_acc_num
   and d.sfclg_location_source = 'NCBIStartEndLoader'
   and k.sfclg_location_source = d.sfclg_location_source;

-- ZDB-GENE-060526-215 (777729): delete 752640029, keep 281706089
delete from sequence_feature_chromosome_location_generated d
 using sequence_feature_chromosome_location_generated k
 where d.sfclg_pk_id = 752640029
   and k.sfclg_pk_id = 281706089
   and d.sfclg_data_zdb_id = 'ZDB-GENE-060526-215'
   and k.sfclg_data_zdb_id = d.sfclg_data_zdb_id
   and d.sfclg_acc_num = '777729'
   and k.sfclg_acc_num = d.sfclg_acc_num
   and d.sfclg_location_source = 'NCBIStartEndLoader'
   and k.sfclg_location_source = d.sfclg_location_source;

-- ZDB-GENE-060825-355 (562920): delete 752639335, keep 281711031
delete from sequence_feature_chromosome_location_generated d
 using sequence_feature_chromosome_location_generated k
 where d.sfclg_pk_id = 752639335
   and k.sfclg_pk_id = 281711031
   and d.sfclg_data_zdb_id = 'ZDB-GENE-060825-355'
   and k.sfclg_data_zdb_id = d.sfclg_data_zdb_id
   and d.sfclg_acc_num = '562920'
   and k.sfclg_acc_num = d.sfclg_acc_num
   and d.sfclg_location_source = 'NCBIStartEndLoader'
   and k.sfclg_location_source = d.sfclg_location_source;

-- ZDB-GENE-070912-156 (557435): delete 752638377, keep 281708502
delete from sequence_feature_chromosome_location_generated d
 using sequence_feature_chromosome_location_generated k
 where d.sfclg_pk_id = 752638377
   and k.sfclg_pk_id = 281708502
   and d.sfclg_data_zdb_id = 'ZDB-GENE-070912-156'
   and k.sfclg_data_zdb_id = d.sfclg_data_zdb_id
   and d.sfclg_acc_num = '557435'
   and k.sfclg_acc_num = d.sfclg_acc_num
   and d.sfclg_location_source = 'NCBIStartEndLoader'
   and k.sfclg_location_source = d.sfclg_location_source;

-- ZDB-GENE-090313-38 (563603): delete 752639857, keep 281710662
delete from sequence_feature_chromosome_location_generated d
 using sequence_feature_chromosome_location_generated k
 where d.sfclg_pk_id = 752639857
   and k.sfclg_pk_id = 281710662
   and d.sfclg_data_zdb_id = 'ZDB-GENE-090313-38'
   and k.sfclg_data_zdb_id = d.sfclg_data_zdb_id
   and d.sfclg_acc_num = '563603'
   and k.sfclg_acc_num = d.sfclg_acc_num
   and d.sfclg_location_source = 'NCBIStartEndLoader'
   and k.sfclg_location_source = d.sfclg_location_source;

-- ZDB-GENE-091204-443 (796836): delete 752638860, keep 281711262
delete from sequence_feature_chromosome_location_generated d
 using sequence_feature_chromosome_location_generated k
 where d.sfclg_pk_id = 752638860
   and k.sfclg_pk_id = 281711262
   and d.sfclg_data_zdb_id = 'ZDB-GENE-091204-443'
   and k.sfclg_data_zdb_id = d.sfclg_data_zdb_id
   and d.sfclg_acc_num = '796836'
   and k.sfclg_acc_num = d.sfclg_acc_num
   and d.sfclg_location_source = 'NCBIStartEndLoader'
   and k.sfclg_location_source = d.sfclg_location_source;

--rollback empty

--liquibase formatted sql
--changeset rtaylor:ZFIN-8426-construct-cleanup-for-GFP

-- delete all the marker_relationship rows that correspond to the constructs that have been re-assigned to sfGFP instead of GFP
DELETE FROM marker_relationship WHERE "mrel_type" = 'coding sequence of' AND "mrel_mrkr_1_zdb_id" = 'ZDB-TGCONSTRCT-200901-8' AND "mrel_mrkr_2_zdb_id" = 'ZDB-EFG-070117-2' AND "mrel_comments" IS NULL AND "mrel_zdb_id" = 'ZDB-MREL-200901-31';
DELETE FROM marker_relationship WHERE "mrel_type" = 'coding sequence of' AND "mrel_mrkr_1_zdb_id" = 'ZDB-TGCONSTRCT-170106-3' AND "mrel_mrkr_2_zdb_id" = 'ZDB-EFG-070117-2' AND "mrel_comments" IS NULL AND "mrel_zdb_id" = 'ZDB-MREL-200311-120';
DELETE FROM marker_relationship WHERE "mrel_type" = 'coding sequence of' AND "mrel_mrkr_1_zdb_id" = 'ZDB-TGCONSTRCT-200901-10' AND "mrel_mrkr_2_zdb_id" = 'ZDB-EFG-070117-2' AND "mrel_comments" IS NULL AND "mrel_zdb_id" = 'ZDB-MREL-200901-39';
DELETE FROM marker_relationship WHERE "mrel_type" = 'coding sequence of' AND "mrel_mrkr_1_zdb_id" = 'ZDB-TGCONSTRCT-170106-2' AND "mrel_mrkr_2_zdb_id" = 'ZDB-EFG-070117-2' AND "mrel_comments" IS NULL AND "mrel_zdb_id" = 'ZDB-MREL-170106-16';
DELETE FROM marker_relationship WHERE "mrel_type" = 'coding sequence of' AND "mrel_mrkr_1_zdb_id" = 'ZDB-TGCONSTRCT-141218-4' AND "mrel_mrkr_2_zdb_id" = 'ZDB-EFG-070117-2' AND "mrel_comments" IS NULL AND "mrel_zdb_id" = 'ZDB-MREL-141218-22';
DELETE FROM marker_relationship WHERE "mrel_type" = 'coding sequence of' AND "mrel_mrkr_1_zdb_id" = 'ZDB-TGCONSTRCT-141217-2' AND "mrel_mrkr_2_zdb_id" = 'ZDB-EFG-070117-2' AND "mrel_comments" IS NULL AND "mrel_zdb_id" = 'ZDB-MREL-170620-86';
DELETE FROM marker_relationship WHERE "mrel_type" = 'coding sequence of' AND "mrel_mrkr_1_zdb_id" = 'ZDB-TGCONSTRCT-150618-4' AND "mrel_mrkr_2_zdb_id" = 'ZDB-EFG-070117-2' AND "mrel_comments" IS NULL AND "mrel_zdb_id" = 'ZDB-MREL-151208-39';
DELETE FROM marker_relationship WHERE "mrel_type" = 'coding sequence of' AND "mrel_mrkr_1_zdb_id" = 'ZDB-TGCONSTRCT-211228-6' AND "mrel_mrkr_2_zdb_id" = 'ZDB-EFG-070117-2' AND "mrel_comments" IS NULL AND "mrel_zdb_id" = 'ZDB-MREL-211228-16';
DELETE FROM marker_relationship WHERE "mrel_type" = 'coding sequence of' AND "mrel_mrkr_1_zdb_id" = 'ZDB-TGCONSTRCT-181207-3' AND "mrel_mrkr_2_zdb_id" = 'ZDB-EFG-070117-2' AND "mrel_comments" IS NULL AND "mrel_zdb_id" = 'ZDB-MREL-181207-9';
DELETE FROM marker_relationship WHERE "mrel_type" = 'coding sequence of' AND "mrel_mrkr_1_zdb_id" = 'ZDB-TGCONSTRCT-200615-1' AND "mrel_mrkr_2_zdb_id" = 'ZDB-EFG-070117-2' AND "mrel_comments" IS NULL AND "mrel_zdb_id" = 'ZDB-MREL-200615-1';
DELETE FROM marker_relationship WHERE "mrel_type" = 'coding sequence of' AND "mrel_mrkr_1_zdb_id" = 'ZDB-TGCONSTRCT-200615-2' AND "mrel_mrkr_2_zdb_id" = 'ZDB-EFG-070117-2' AND "mrel_comments" IS NULL AND "mrel_zdb_id" = 'ZDB-MREL-200615-5';
DELETE FROM marker_relationship WHERE "mrel_type" = 'coding sequence of' AND "mrel_mrkr_1_zdb_id" = 'ZDB-TGCONSTRCT-211103-2' AND "mrel_mrkr_2_zdb_id" = 'ZDB-EFG-070117-2' AND "mrel_comments" IS NULL AND "mrel_zdb_id" = 'ZDB-MREL-211103-3';
DELETE FROM marker_relationship WHERE "mrel_type" = 'coding sequence of' AND "mrel_mrkr_1_zdb_id" = 'ZDB-TGCONSTRCT-200901-11' AND "mrel_mrkr_2_zdb_id" = 'ZDB-EFG-070117-2' AND "mrel_comments" IS NULL AND "mrel_zdb_id" = 'ZDB-MREL-200901-43';
DELETE FROM marker_relationship WHERE "mrel_type" = 'coding sequence of' AND "mrel_mrkr_1_zdb_id" = 'ZDB-TGCONSTRCT-211116-5' AND "mrel_mrkr_2_zdb_id" = 'ZDB-EFG-070117-2' AND "mrel_comments" IS NULL AND "mrel_zdb_id" = 'ZDB-MREL-211116-23';
DELETE FROM marker_relationship WHERE "mrel_type" = 'coding sequence of' AND "mrel_mrkr_1_zdb_id" = 'ZDB-TGCONSTRCT-141217-1' AND "mrel_mrkr_2_zdb_id" = 'ZDB-EFG-070117-2' AND "mrel_comments" IS NULL AND "mrel_zdb_id" = 'ZDB-MREL-170620-80';
DELETE FROM marker_relationship WHERE "mrel_type" = 'coding sequence of' AND "mrel_mrkr_1_zdb_id" = 'ZDB-TGCONSTRCT-210715-3' AND "mrel_mrkr_2_zdb_id" = 'ZDB-EFG-070117-2' AND "mrel_comments" IS NULL AND "mrel_zdb_id" = 'ZDB-MREL-210715-62';
DELETE FROM marker_relationship WHERE "mrel_type" = 'coding sequence of' AND "mrel_mrkr_1_zdb_id" = 'ZDB-TGCONSTRCT-181212-4' AND "mrel_mrkr_2_zdb_id" = 'ZDB-EFG-070117-2' AND "mrel_comments" IS NULL AND "mrel_zdb_id" = 'ZDB-MREL-181212-12';
DELETE FROM marker_relationship WHERE "mrel_type" = 'coding sequence of' AND "mrel_mrkr_1_zdb_id" = 'ZDB-TGCONSTRCT-220110-3' AND "mrel_mrkr_2_zdb_id" = 'ZDB-EFG-070117-2' AND "mrel_comments" IS NULL AND "mrel_zdb_id" = 'ZDB-MREL-220110-12';
DELETE FROM marker_relationship WHERE "mrel_type" = 'coding sequence of' AND "mrel_mrkr_1_zdb_id" = 'ZDB-TGCONSTRCT-211116-9' AND "mrel_mrkr_2_zdb_id" = 'ZDB-EFG-070117-2' AND "mrel_comments" IS NULL AND "mrel_zdb_id" = 'ZDB-MREL-211116-39';
DELETE FROM marker_relationship WHERE "mrel_type" = 'coding sequence of' AND "mrel_mrkr_1_zdb_id" = 'ZDB-TGCONSTRCT-200928-1' AND "mrel_mrkr_2_zdb_id" = 'ZDB-EFG-070117-2' AND "mrel_comments" IS NULL AND "mrel_zdb_id" = 'ZDB-MREL-200928-1';
DELETE FROM marker_relationship WHERE "mrel_type" = 'coding sequence of' AND "mrel_mrkr_1_zdb_id" = 'ZDB-TGCONSTRCT-220901-5' AND "mrel_mrkr_2_zdb_id" = 'ZDB-EFG-070117-2' AND "mrel_comments" IS NULL AND "mrel_zdb_id" = 'ZDB-MREL-220901-14';
DELETE FROM marker_relationship WHERE "mrel_type" = 'coding sequence of' AND "mrel_mrkr_1_zdb_id" = 'ZDB-TGCONSTRCT-200901-9' AND "mrel_mrkr_2_zdb_id" = 'ZDB-EFG-070117-2' AND "mrel_comments" IS NULL AND "mrel_zdb_id" = 'ZDB-MREL-200901-35';
DELETE FROM marker_relationship WHERE "mrel_type" = 'coding sequence of' AND "mrel_mrkr_1_zdb_id" = 'ZDB-TGCONSTRCT-181207-4' AND "mrel_mrkr_2_zdb_id" = 'ZDB-EFG-070117-2' AND "mrel_comments" IS NULL AND "mrel_zdb_id" = 'ZDB-MREL-181207-13';
DELETE FROM marker_relationship WHERE "mrel_type" = 'coding sequence of' AND "mrel_mrkr_1_zdb_id" = 'ZDB-TGCONSTRCT-181207-6' AND "mrel_mrkr_2_zdb_id" = 'ZDB-EFG-070117-2' AND "mrel_comments" IS NULL AND "mrel_zdb_id" = 'ZDB-MREL-181207-20';
DELETE FROM marker_relationship WHERE "mrel_type" = 'coding sequence of' AND "mrel_mrkr_1_zdb_id" = 'ZDB-TGCONSTRCT-190812-11' AND "mrel_mrkr_2_zdb_id" = 'ZDB-EFG-070117-2' AND "mrel_comments" IS NULL AND "mrel_zdb_id" = 'ZDB-MREL-190812-49';

-- delete associated record attributions
DELETE FROM record_attribution WHERE "recattrib_data_zdb_id" = 'ZDB-MREL-170106-16' AND "recattrib_source_zdb_id" = 'ZDB-PUB-151126-6' AND "recattrib_source_significance" IS NULL AND "recattrib_source_type" = 'standard' AND "recattrib_created_at" IS NULL AND "recattrib_modified_at" IS NULL AND "recattrib_modified_count" IS NULL AND "recattrib_pk_id" = 56621200;
DELETE FROM record_attribution WHERE "recattrib_data_zdb_id" = 'ZDB-MREL-211228-16' AND "recattrib_source_zdb_id" = 'ZDB-PUB-210220-11' AND "recattrib_source_significance" IS NULL AND "recattrib_source_type" = 'standard' AND "recattrib_created_at" IS NULL AND "recattrib_modified_at" IS NULL AND "recattrib_modified_count" IS NULL AND "recattrib_pk_id" = 107384510;
DELETE FROM record_attribution WHERE "recattrib_data_zdb_id" = 'ZDB-MREL-200928-1' AND "recattrib_source_zdb_id" = 'ZDB-PUB-191001-3' AND "recattrib_source_significance" IS NULL AND "recattrib_source_type" = 'standard' AND "recattrib_created_at" IS NULL AND "recattrib_modified_at" IS NULL AND "recattrib_modified_count" IS NULL AND "recattrib_pk_id" = 105480224;
DELETE FROM record_attribution WHERE "recattrib_data_zdb_id" = 'ZDB-MREL-181207-20' AND "recattrib_source_zdb_id" = 'ZDB-PUB-180908-2' AND "recattrib_source_significance" IS NULL AND "recattrib_source_type" = 'standard' AND "recattrib_created_at" IS NULL AND "recattrib_modified_at" IS NULL AND "recattrib_modified_count" IS NULL AND "recattrib_pk_id" = 82040448;
DELETE FROM record_attribution WHERE "recattrib_data_zdb_id" = 'ZDB-MREL-170620-86' AND "recattrib_source_zdb_id" = 'ZDB-PUB-131024-18' AND "recattrib_source_significance" IS NULL AND "recattrib_source_type" = 'standard' AND "recattrib_created_at" IS NULL AND "recattrib_modified_at" IS NULL AND "recattrib_modified_count" IS NULL AND "recattrib_pk_id" = 63218833;
DELETE FROM record_attribution WHERE "recattrib_data_zdb_id" = 'ZDB-MREL-151208-39' AND "recattrib_source_zdb_id" = 'ZDB-PUB-140925-8' AND "recattrib_source_significance" IS NULL AND "recattrib_source_type" = 'standard' AND "recattrib_created_at" IS NULL AND "recattrib_modified_at" IS NULL AND "recattrib_modified_count" IS NULL AND "recattrib_pk_id" = 40587501;
DELETE FROM record_attribution WHERE "recattrib_data_zdb_id" = 'ZDB-MREL-200311-120' AND "recattrib_source_zdb_id" = 'ZDB-PUB-151126-6' AND "recattrib_source_significance" IS NULL AND "recattrib_source_type" = 'standard' AND "recattrib_created_at" IS NULL AND "recattrib_modified_at" IS NULL AND "recattrib_modified_count" IS NULL AND "recattrib_pk_id" = 102933180;
DELETE FROM record_attribution WHERE "recattrib_data_zdb_id" = 'ZDB-MREL-211103-3' AND "recattrib_source_zdb_id" = 'ZDB-PUB-200208-5' AND "recattrib_source_significance" IS NULL AND "recattrib_source_type" = 'standard' AND "recattrib_created_at" IS NULL AND "recattrib_modified_at" IS NULL AND "recattrib_modified_count" IS NULL AND "recattrib_pk_id" = 107363447;
DELETE FROM record_attribution WHERE "recattrib_data_zdb_id" = 'ZDB-MREL-181207-13' AND "recattrib_source_zdb_id" = 'ZDB-PUB-180908-2' AND "recattrib_source_significance" IS NULL AND "recattrib_source_type" = 'standard' AND "recattrib_created_at" IS NULL AND "recattrib_modified_at" IS NULL AND "recattrib_modified_count" IS NULL AND "recattrib_pk_id" = 82040412;
DELETE FROM record_attribution WHERE "recattrib_data_zdb_id" = 'ZDB-MREL-200901-31' AND "recattrib_source_zdb_id" = 'ZDB-PUB-200212-8' AND "recattrib_source_significance" IS NULL AND "recattrib_source_type" = 'standard' AND "recattrib_created_at" IS NULL AND "recattrib_modified_at" IS NULL AND "recattrib_modified_count" IS NULL AND "recattrib_pk_id" = 105369240;
DELETE FROM record_attribution WHERE "recattrib_data_zdb_id" = 'ZDB-MREL-170620-80' AND "recattrib_source_zdb_id" = 'ZDB-PUB-131024-18' AND "recattrib_source_significance" IS NULL AND "recattrib_source_type" = 'standard' AND "recattrib_created_at" IS NULL AND "recattrib_modified_at" IS NULL AND "recattrib_modified_count" IS NULL AND "recattrib_pk_id" = 63218827;
DELETE FROM record_attribution WHERE "recattrib_data_zdb_id" = 'ZDB-MREL-220901-14' AND "recattrib_source_zdb_id" = 'ZDB-PUB-210513-3' AND "recattrib_source_significance" IS NULL AND "recattrib_source_type" = 'standard' AND "recattrib_created_at" IS NULL AND "recattrib_modified_at" IS NULL AND "recattrib_modified_count" IS NULL AND "recattrib_pk_id" = 108279994;
DELETE FROM record_attribution WHERE "recattrib_data_zdb_id" = 'ZDB-MREL-200901-35' AND "recattrib_source_zdb_id" = 'ZDB-PUB-200212-8' AND "recattrib_source_significance" IS NULL AND "recattrib_source_type" = 'standard' AND "recattrib_created_at" IS NULL AND "recattrib_modified_at" IS NULL AND "recattrib_modified_count" IS NULL AND "recattrib_pk_id" = 105369253;
DELETE FROM record_attribution WHERE "recattrib_data_zdb_id" = 'ZDB-MREL-200901-43' AND "recattrib_source_zdb_id" = 'ZDB-PUB-200212-8' AND "recattrib_source_significance" IS NULL AND "recattrib_source_type" = 'standard' AND "recattrib_created_at" IS NULL AND "recattrib_modified_at" IS NULL AND "recattrib_modified_count" IS NULL AND "recattrib_pk_id" = 105369273;
DELETE FROM record_attribution WHERE "recattrib_data_zdb_id" = 'ZDB-MREL-200615-5' AND "recattrib_source_zdb_id" = 'ZDB-PUB-191116-7' AND "recattrib_source_significance" IS NULL AND "recattrib_source_type" = 'standard' AND "recattrib_created_at" IS NULL AND "recattrib_modified_at" IS NULL AND "recattrib_modified_count" IS NULL AND "recattrib_pk_id" = 104516103;
DELETE FROM record_attribution WHERE "recattrib_data_zdb_id" = 'ZDB-MREL-200615-1' AND "recattrib_source_zdb_id" = 'ZDB-PUB-191116-7' AND "recattrib_source_significance" IS NULL AND "recattrib_source_type" = 'standard' AND "recattrib_created_at" IS NULL AND "recattrib_modified_at" IS NULL AND "recattrib_modified_count" IS NULL AND "recattrib_pk_id" = 104516093;
DELETE FROM record_attribution WHERE "recattrib_data_zdb_id" = 'ZDB-MREL-211116-39' AND "recattrib_source_zdb_id" = 'ZDB-PUB-200216-10' AND "recattrib_source_significance" IS NULL AND "recattrib_source_type" = 'standard' AND "recattrib_created_at" IS NULL AND "recattrib_modified_at" IS NULL AND "recattrib_modified_count" IS NULL AND "recattrib_pk_id" = 107365493;
DELETE FROM record_attribution WHERE "recattrib_data_zdb_id" = 'ZDB-MREL-141218-22' AND "recattrib_source_zdb_id" = 'ZDB-PUB-140513-414' AND "recattrib_source_significance" IS NULL AND "recattrib_source_type" = 'standard' AND "recattrib_created_at" IS NULL AND "recattrib_modified_at" IS NULL AND "recattrib_modified_count" IS NULL AND "recattrib_pk_id" = 13849314;
DELETE FROM record_attribution WHERE "recattrib_data_zdb_id" = 'ZDB-MREL-190812-49' AND "recattrib_source_zdb_id" = 'ZDB-PUB-190209-17' AND "recattrib_source_significance" IS NULL AND "recattrib_source_type" = 'standard' AND "recattrib_created_at" IS NULL AND "recattrib_modified_at" IS NULL AND "recattrib_modified_count" IS NULL AND "recattrib_pk_id" = 93672149;
DELETE FROM record_attribution WHERE "recattrib_data_zdb_id" = 'ZDB-MREL-200901-39' AND "recattrib_source_zdb_id" = 'ZDB-PUB-200212-8' AND "recattrib_source_significance" IS NULL AND "recattrib_source_type" = 'standard' AND "recattrib_created_at" IS NULL AND "recattrib_modified_at" IS NULL AND "recattrib_modified_count" IS NULL AND "recattrib_pk_id" = 105369264;
DELETE FROM record_attribution WHERE "recattrib_data_zdb_id" = 'ZDB-MREL-211116-23' AND "recattrib_source_zdb_id" = 'ZDB-PUB-200216-10' AND "recattrib_source_significance" IS NULL AND "recattrib_source_type" = 'standard' AND "recattrib_created_at" IS NULL AND "recattrib_modified_at" IS NULL AND "recattrib_modified_count" IS NULL AND "recattrib_pk_id" = 107365427;
DELETE FROM record_attribution WHERE "recattrib_data_zdb_id" = 'ZDB-MREL-181212-12' AND "recattrib_source_zdb_id" = 'ZDB-PUB-180908-2' AND "recattrib_source_significance" IS NULL AND "recattrib_source_type" = 'standard' AND "recattrib_created_at" IS NULL AND "recattrib_modified_at" IS NULL AND "recattrib_modified_count" IS NULL AND "recattrib_pk_id" = 82633527;
DELETE FROM record_attribution WHERE "recattrib_data_zdb_id" = 'ZDB-MREL-181207-9' AND "recattrib_source_zdb_id" = 'ZDB-PUB-180908-2' AND "recattrib_source_significance" IS NULL AND "recattrib_source_type" = 'standard' AND "recattrib_created_at" IS NULL AND "recattrib_modified_at" IS NULL AND "recattrib_modified_count" IS NULL AND "recattrib_pk_id" = 82040401;
DELETE FROM record_attribution WHERE "recattrib_data_zdb_id" = 'ZDB-MREL-210715-62' AND "recattrib_source_zdb_id" = 'ZDB-PUB-201002-107' AND "recattrib_source_significance" IS NULL AND "recattrib_source_type" = 'standard' AND "recattrib_created_at" IS NULL AND "recattrib_modified_at" IS NULL AND "recattrib_modified_count" IS NULL AND "recattrib_pk_id" = 107264539;
DELETE FROM record_attribution WHERE "recattrib_data_zdb_id" = 'ZDB-MREL-220110-12' AND "recattrib_source_zdb_id" = 'ZDB-PUB-220110-2' AND "recattrib_source_significance" IS NULL AND "recattrib_source_type" = 'standard' AND "recattrib_created_at" IS NULL AND "recattrib_modified_at" IS NULL AND "recattrib_modified_count" IS NULL AND "recattrib_pk_id" = 107387300;

-- delete the relationships from the zdb_active_data table
DELETE FROM zdb_active_data where zactvd_zdb_id in (
                                                    'ZDB-MREL-170106-16',
                                                    'ZDB-MREL-211228-16',
                                                    'ZDB-MREL-200928-1',
                                                    'ZDB-MREL-181207-20',
                                                    'ZDB-MREL-170620-86',
                                                    'ZDB-MREL-151208-39',
                                                    'ZDB-MREL-200311-120',
                                                    'ZDB-MREL-211103-3',
                                                    'ZDB-MREL-181207-13',
                                                    'ZDB-MREL-200901-31',
                                                    'ZDB-MREL-170620-80',
                                                    'ZDB-MREL-220901-14',
                                                    'ZDB-MREL-200901-35',
                                                    'ZDB-MREL-200901-43',
                                                    'ZDB-MREL-200615-5',
                                                    'ZDB-MREL-200615-1',
                                                    'ZDB-MREL-211116-39',
                                                    'ZDB-MREL-141218-22',
                                                    'ZDB-MREL-190812-49',
                                                    'ZDB-MREL-200901-39',
                                                    'ZDB-MREL-211116-23',
                                                    'ZDB-MREL-181212-12',
                                                    'ZDB-MREL-181207-9',
                                                    'ZDB-MREL-210715-62',
                                                    'ZDB-MREL-220110-12');




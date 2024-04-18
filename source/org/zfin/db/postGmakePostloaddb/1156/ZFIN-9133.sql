--liquibase formatted sql
--changeset rtaylor:ZFIN-9133.sql

-- merging feature ZDB-ALT-230130-2 (ue109) into ZDB-ALT-220822-1 (ue106)

-- ### Tables and actions to take on rows that contain old ZDB IDs:
-- - feature: delete unused ue109 (via zdb_active_data cascade)
-- - feature_assay: delete unused
-- - feature_dna_mutation_detail: update
-- - feature_genomic_mutation_detail: update
-- - feature_history: delete
-- - feature_marker_relationship: delete
-- - feature_tracking: update
-- - int_data_source: delete
-- - record_attribution: delete
-- - sequence_feature_chromosome_location_generated: update
-- - sequence_feature_chromosome_location: update
-- - updates: nothing
-- - variant_flanking_sequence: update
-- - zdb_active_data: delete
-- - zdb_replaced_data: insert


DELETE FROM feature_assay WHERE featassay_feature_zdb_id = 'ZDB-ALT-230130-2';
UPDATE feature_dna_mutation_detail SET fdmd_feature_zdb_id = 'ZDB-ALT-220822-1' WHERE fdmd_feature_zdb_id = 'ZDB-ALT-230130-2';
UPDATE feature_genomic_mutation_detail SET fgmd_feature_zdb_id = 'ZDB-ALT-220822-1' WHERE fgmd_feature_zdb_id = 'ZDB-ALT-230130-2';
DELETE FROM feature_history WHERE fhist_ftr_zdb_id = 'ZDB-ALT-230130-2';
DELETE FROM feature_marker_relationship WHERE fmrel_ftr_zdb_id = 'ZDB-ALT-230130-2';
UPDATE feature_tracking SET ft_feature_zdb_id = 'ZDB-ALT-220822-1' WHERE ft_feature_zdb_id = 'ZDB-ALT-230130-2';
DELETE FROM int_data_source WHERE ids_data_zdb_id = 'ZDB-ALT-230130-2';
DELETE FROM record_attribution WHERE recattrib_data_zdb_id = 'ZDB-ALT-230130-2';
UPDATE sequence_feature_chromosome_location_generated SET sfclg_data_zdb_id = 'ZDB-ALT-220822-1' WHERE sfclg_data_zdb_id = 'ZDB-ALT-230130-2';
UPDATE sequence_feature_chromosome_location SET sfcl_zdb_id = 'ZDB-ALT-220822-1' WHERE sfcl_zdb_id = 'ZDB-ALT-230130-2';
UPDATE variant_flanking_sequence SET vfseq_data_zdb_id = 'ZDB-ALT-220822-1' WHERE vfseq_data_zdb_id = 'ZDB-ALT-230130-2';
DELETE FROM zdb_active_data WHERE zactvd_zdb_id = 'ZDB-ALT-230130-2';
INSERT INTO zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id, zrepld_old_name) VALUES ('ZDB-ALT-230130-2', 'ZDB-ALT-220822-1', 'ue109');

-- Add alias:
INSERT INTO data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_group_id) VALUES
    (get_id_and_insert_active_data('DALIAS'), 'ZDB-ALT-220822-1', 'ue109', '1');

-- Relevant contents of postgres dump files that contained references to ZDB-ALT-230130-2
-- COPY public.feature (feature_zdb_id, feature_name, feature_abbrev, feature_type, feature_name_order, feature_abbrev_order, feature_date_entered, feature_lab_prefix_id, feature_line_number, feature_df_transloc_complex_prefix, feature_dominant, feature_unspecified, feature_unrecovered, feature_known_insertion_site, feature_tg_suffix, ftr_chr_info_date) FROM stdin;
-- ZDB-ALT-230130-2   ue109  ue109  INDEL  ue0000000109   ue0000000109   \N 257    109    \N f  f  ff Tg \N
-- 
-- COPY public.feature_assay (featassay_feature_zdb_id, featassay_mutagen, featassay_mutagee, featassay_pk_id) FROM stdin;
-- ZDB-ALT-230130-2   CRISPR embryos    98717
-- 
-- COPY public.feature_dna_mutation_detail (fdmd_zdb_id, fdmd_feature_zdb_id, fdmd_dna_mutation_term_zdb_id, fdmd_dna_sequence_of_reference_accession_number, fdmd_fdbcont_zdb_id, fdmd_dna_position_start, fdmd_dna_position_end, fdmd_number_additional_dna_base_pairs, fdmd_number_removed_dna_base_pairs, fdmd_exon_number, fdmd_intron_number, fdmd_gene_localization_term_zdb_id, fdmd_inserted_sequence, fdmd_deleted_sequence) FROM stdin;
-- ZDB-FDMD-230130-2  ZDB-ALT-230130-2   \N \N \N \N \N 7  5  4  \N ZDB-TERM-130401-150    \N \N
-- 
-- COPY public.feature_genomic_mutation_detail (fgmd_zdb_id, fgmd_feature_zdb_id, fgmd_sequence_of_reference, fgmd_sequence_of_variation, fgmd_sequence_of_reference_accession_number, fgmd_variation_strand, fgmd_padded_base) FROM stdin;
-- ZDB-FGMD-230130-2  ZDB-ALT-230130-2   GATGA  ATTTATT    \N +  \N
-- 
-- COPY public.feature_history (fhist_zdb_id, fhist_ftr_zdb_id, fhist_event, fhist_reason, fhist_date, fhist_ftr_name_on_fhist_date, fhist_ftr_abbrev_on_fhist_date, fhist_comments, fhist_dalias_zdb_id, fhist_ftr_prev_name) FROM stdin;
-- ZDB-FHIST-230130-2 ZDB-ALT-230130-2   assigned   Not Specified  2023-01-30 09:59:11.039209 ue109  ue109\N    \N
-- 
-- COPY public.feature_marker_relationship (fmrel_zdb_id, fmrel_type, fmrel_ftr_zdb_id, fmrel_mrkr_zdb_id) FROM stdin;
-- ZDB-FMREL-230130-3 is allele of   ZDB-ALT-230130-2   ZDB-GENE-050317-1
-- ZDB-FMREL-230130-4 created by ZDB-ALT-230130-2   ZDB-CRISPR-220128-12
-- ZDB-FMREL-230130-5 created by ZDB-ALT-230130-2   ZDB-CRISPR-220128-13
-- 
-- COPY public.feature_tracking (ft_pk_id, ft_feature_zdb_id, ft_feature_abbrev, ft_feature_name, ft_date_entered) FROM stdin;
-- 81952  ZDB-ALT-230130-2   ue109  ue109  2023-01-30 09:59:11.039209
-- 
-- COPY public.int_data_source (ids_pk_id, ids_data_zdb_id, ids_source_zdb_id) FROM stdin;
-- 123829 ZDB-ALT-230130-2   ZDB-LAB-010302-1
-- 
-- COPY public.record_attribution (recattrib_pk_id, recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_significance, recattrib_source_type, recattrib_created_at, recattrib_modified_at, recattrib_modified_count) FROM stdin;
-- 112133282  ZDB-ALT-230130-2   ZDB-PUB-210430-11  \N standard   2023-01-30 09:59:11.039209 2023-01-30 09:59:11.039209 1
-- 112133283  ZDB-ALT-230130-2   ZDB-PUB-210430-11  \N feature type   2023-01-30 09:59:11.039209 2023-01-30 09:59:11.039209 1
-- 
-- COPY public.sequence_feature_chromosome_location (sfcl_zdb_id, sfcl_feature_zdb_id, sfcl_start_position, sfcl_end_position, sfcl_assembly, sfcl_chromosome, sfcl_evidence_code, sfcl_chromosome_reference_accession_number) FROM stdin;
-- ZDB-SFCL-230130-2  ZDB-ALT-230130-2   27023450   27023454   GRCz11 19 ZDB-TERM-170419-251    \N
-- 
-- COPY public.sequence_feature_chromosome_location_generated (sfclg_chromosome, sfclg_data_zdb_id, sfclg_pk_id, sfclg_acc_num, sfclg_start, sfclg_end, sfclg_location_source, sfclg_location_subsource, sfclg_fdb_db_id, sfclg_pub_zdb_id, sfclg_assembly, sfclg_gbrowse_track, sfclg_evidence_code) FROM stdin;
-- 19 ZDB-ALT-230130-2   719220658  \N 27023450   27023454   DirectSubmission      \N ZDB-PUB-210430-11  GRCz11 \N \N
-- 
-- COPY public.updates (submitter_id, rec_id, field_name, new_value, old_value, comments, submitter_name, upd_pk_id, upd_when) FROM stdin;
-- ZDB-PERS-100329-1  ZDB-FMREL-230130-3 FeatureMarkerRelationship  FeatureMarkerRelationship{zdbID='ZDB-FMREL-230130-3', type='is allele of', feature='Feature{zdbID='ZDB-ALT-230130-2', name='ue109', lineNumber='109', labPrefix=FeaturePrefix{prefixString='ue', institute='University of Edinburgh'}, abbreviation='ue109', transgenicSuffix='Tg', isKnownInsertionSite=false, isDominantFeature=false, type=INDEL}', marker=MARKER\nzdbID: ZDB-GENE-050317-1\nsymbol: tnfa\nname: tumor necrosis factor a (TNF superfamily, member 2)\ntype: name[GENE]type[GENE]typeGroupStrings[GENEDOM_EFG_EREGION_K SEARCH_MK GENEDOM_AND_NTR GENEDOM SEARCHABLE_PROTEIN_CODING_GENE GENEDOM_PROD_PROTEIN CONSTRUCT_COMPONENTS GENE CAN_BE_PROMOTER DEFICIENCY_TLOC_MARK FEATURE SEARCH_MKSEG GENEDOM_AND_EFG CAN_HAVE_MRPHLN ]typeGroups[GENEDOM GENEDOM_AND_NTR CAN_HAVE_MRPHLN GENE FEATURE SEARCHABLE_PROTEIN_CODING_GENE GENEDOM_PROD_PROTEIN DEFICIENCY_TLOC_MARK GENEDOM_EFG_EREGION_K CONSTRUCT_COMPONENTS CAN_BE_PROMOTER GENEDOM_AND_EFG SEARCH_MKSEG SEARCH_MK ]\n}       \N Created feature marker relationship    Paddock, Holly 1825930    2023-01-30 10:00:32.721
-- ZDB-PERS-100329-1  ZDB-FMREL-230130-5 FeatureMarkerRelationship  FeatureMarkerRelationship{zdbID='ZDB-FMREL-230130-5', type='created by', feature='Feature{zdbID='ZDB-ALT-230130-2', name='ue109', lineNumber='109', labPrefix=FeaturePrefix{prefixString='ue', institute='University of Edinburgh'}, abbreviation='ue109', transgenicSuffix='Tg', isKnownInsertionSite=false, isDominantFeature=false, type=INDEL}', marker=MARKER\nzdbID: ZDB-CRISPR-220128-13\nsymbol: CRISPR8-tnfa\nname: CRISPR8-tnfa\ntype: name[CRISPR]type[CRISPR]typeGroupStrings[GENEDOM_EFG_EREGION_K CONSTRUCT_COMPONENTS CRISPR ABBREV_EQ_NAME KNOCKDOWN_REAGENT SEARCHABLE_STR SEARCH_MKSEG ]typeGroups[ABBREV_EQ_NAME GENEDOM_EFG_EREGION_K CONSTRUCT_COMPONENTS SEARCHABLE_STR KNOCKDOWN_REAGENT SEARCH_MKSEG CRISPR ]\n}   \N Created feature marker relationship    Paddock, Holly 1825932    2023-01-30 10:01:42.142
-- ZDB-PERS-100329-1  ZDB-FMREL-230130-4 FeatureMarkerRelationship  FeatureMarkerRelationship{zdbID='ZDB-FMREL-230130-4', type='created by', feature='Feature{zdbID='ZDB-ALT-230130-2', name='ue109', lineNumber='109', labPrefix=FeaturePrefix{prefixString='ue', institute='University of Edinburgh'}, abbreviation='ue109', transgenicSuffix='Tg', isKnownInsertionSite=false, isDominantFeature=false, type=INDEL}', marker=MARKER\nzdbID: ZDB-CRISPR-220128-12\nsymbol: CRISPR7-tnfa\nname: CRISPR7-tnfa\ntype: name[CRISPR]type[CRISPR]typeGroupStrings[GENEDOM_EFG_EREGION_K CONSTRUCT_COMPONENTS CRISPR ABBREV_EQ_NAME KNOCKDOWN_REAGENT SEARCHABLE_STR SEARCH_MKSEG ]typeGroups[ABBREV_EQ_NAME GENEDOM_EFG_EREGION_K CONSTRUCT_COMPONENTS SEARCHABLE_STR KNOCKDOWN_REAGENT SEARCH_MKSEG CRISPR ]\n}   \N Created feature marker relationship    Paddock, Holly 1825931    2023-01-30 10:01:17.442
-- 
-- COPY public.variant_flanking_sequence (vfseq_data_zdb_id, vfseq_type, vfseq_offset_start, vfseq_offset_stop, vfseq_sequence, vfseq_five_prime_flanking_sequence, vfseq_three_prime_flanking_sequence, vfseq_flanking_sequence_type, vfseq_flanking_sequence_origin, vfseq_variation, vfseq_zdb_id) FROM stdin;
-- ZDB-ALT-230130-2   Genomic    500    500    AAGCCATTTACATTTTTTGTGGGGTCGAGACCTCCTGCCTCATTCACTTCCATTCATTTTTAGACATTATTAACAACTTGTTATGCTGTTTGATGTTGCACACTGACAGTTTCTTATATTATTCTAATTTGTCTTTATTGTCATGCAAACACTTGTTTGTAATGCAAGTAGTTTGACAATTTTCTGCGGTTTATTATTCCTAGTAATTTCTCCCACATGCAGCTAAATCGAATGTTCTAACATAATTGCAAAAATGAGCGCACTTCCTCATTGAAGAAATAAGGCCAATCTGAAAAATAATTGTAGAATTTAAGTAAATTACCAAGTAAAATCTGCATTCTGAGGCAAAACCTGTTGAGAATCACTGACTTAGACCATGTTTTTTGTTTATCCTAGGTGGATACAACTCTGAATCAAAGACCTTAGACTGGAGAGATGACCAGGACCAGGCCTTTTCTTCAGGTGGCTTGAAATTAGTAAACAGGGAGATTATCATTCCC[GATGA/ATTTATT]TGGCATTTATTTTGTCTACAGCCAGGTGTCTTTGCACATCAGCTGCACGTCTGAACTGACTGAGGAACAAGTGCTTATGAGCCATGCAGTGATGCGCTTTTCTGAATCCTACGGAGGCAAAAAGCCACTTTTCAGTGCAATCCGCTCAATCTGCACGCAGGAGCCTGAATCTGAAAATCTGTGGTACAACACTATTTACCTCGGCGCTGCCTTCCATTTACGAGAAGGAGACAGACTGGGCACAGACACGACCACAGCACTTCTACCGATGGTTGAAAATGATAACGGAAAGACCTTCTTTGGGGTGTTTGGTTTGTGAACGAAAGTGAGGAAAATGTTTTATTTTAAAACAGAAAGAACCAATGACCTCTATCATACACTCTTGGCAATGTGCCGCCAACACATCTGTACATTGCTGACTGTTGTTCAGAGACTATTATGCTATTATGTGCAACTGGCTTTAAACTTGCTTTATTTTATTGTTATTTGTGTTGGTATTT    AAGCCATTTACATTTTTTGTGGGGTCGAGACCTCCTGCCTCATTCACTTCCATTCATTTTTAGACATTATTAACAACTTGTTATGCTGTTTGATGTTGCACACTGACAGTTTCTTATATTATTCTAATTTGTCTTTATTGTCATGCAAACACTTGTTTGTAATGCAAGTAGTTTGACAATTTTCTGCGGTTTATTATTCCTAGTAATTTCTCCCACATGCAGCTAAATCGAATGTTCTAACATAATTGCAAAAATGAGCGCACTTCCTCATTGAAGAAATAAGGCCAATCTGAAAAATAATTGTAGAATTTAAGTAAATTACCAAGTAAAATCTGCATTCTGAGGCAAAACCTGTTGAGAATCACTGACTTAGACCATGTTTTTTGTTTATCCTAGGTGGATACAACTCTGAATCAAAGACCTTAGACTGGAGAGATGACCAGGACCAGGCCTTTTCTTCAGGTGGCTTGAAATTAGTAAACAGGGAGATTATCATTCCC   TGGCATTTATTTTGTCTACAGCCAGGTGTCTTTGCACATCAGCTGCACGTCTGAACTGACTGAGGAACAAGTGCTTATGAGCCATGCAGTGATGCGCTTTTCTGAATCCTACGGAGGCAAAAAGCCACTTTTCAGTGCAATCCGCTCAATCTGCACGCAGGAGCCTGAATCTGAAAATCTGTGGTACAACACTATTTACCTCGGCGCTGCCTTCCATTTACGAGAAGGAGACAGACTGGGCACAGACACGACCACAGCACTTCTACCGATGGTTGAAAATGATAACGGAAAGACCTTCTTTGGGGTGTTTGGTTTGTGAACGAAAGTGAGGAAAATGTTTTATTTTAAAACAGAAAGAACCAATGACCTCTATCATACACTCTTGGCAATGTGCCGCCAACACATCTGTACATTGCTGACTGTTGTTCAGAGACTATTATGCTATTATGTGCAACTGGCTTTAAACTTGCTTTATTTTATTGTTATTTGTGTTGGTATTT   genomic    directly sequenced     GATGA/ATTTATT  ZDB-VFSEQ-230131-1
-- 
-- COPY public.zdb_active_data (zactvd_zdb_id) FROM stdin;
-- ZDB-ALT-230130-2




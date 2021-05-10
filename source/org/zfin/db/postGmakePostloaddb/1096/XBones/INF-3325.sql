--liquibase formatted sql
--changeset pkalita:INF-3325


DROP TABLE database_env_name_matrix;
DROP TABLE fig_comm;
DROP TABLE fig_tmp;
DROP TABLE functional_annotation;

DELETE FROM marker_history WHERE mhist_zdb_id = 'ZDB-NOMEN-100826-1';

UPDATE construct SET construct_comments = NULL WHERE construct_comments = ' ' OR construct_comments = '';
UPDATE image SET img_annotation = NULL WHERE img_annotation = ' ' OR img_annotation = '';
UPDATE linkage SET lnkg_comments = NULL WHERE lnkg_comments = ' ' OR lnkg_comments = '';
UPDATE marker_history SET mhist_comments = NULL WHERE mhist_comments = ' ' OR mhist_comments = '';
UPDATE marker_history SET mhist_mrkr_prev_name = NULL WHERE mhist_mrkr_prev_name = ' ' OR mhist_mrkr_prev_name = '';
UPDATE marker_relationship SET mrel_comments = NULL WHERE mrel_comments = ' ' OR mrel_comments = '';
UPDATE marker_sequence SET seq_sequence_2 = NULL WHERE seq_sequence_2 = ' ' OR seq_sequence_2 = '';
UPDATE primer_set SET comments = NULL WHERE comments = ' ' OR comments = '';
UPDATE vector_type SET vectype_comments = NULL WHERE vectype_comments = ' ' OR vectype_comments = '';

UPDATE attribution_type SET attype_definition = scrub_char(attype_definition);
UPDATE blast_database SET blastdb_description = scrub_char(blastdb_description);
UPDATE blast_database SET blastdb_tool_display_name = scrub_char(blastdb_tool_display_name);
UPDATE blast_database_origination_type SET bdot_definition = scrub_char(bdot_definition);
UPDATE clone SET clone_pcr_amplification = scrub_char(clone_pcr_amplification);
UPDATE company SET phone = scrub_char(phone);
UPDATE company SET url = scrub_char(url);
UPDATE company SET email = scrub_char(email);
UPDATE company SET fax = scrub_char(fax);
UPDATE company SET name = scrub_char(name);
UPDATE construct SET construct_comments = scrub_char(construct_comments);
UPDATE construct_component SET cc_component_category = scrub_char(cc_component_category);
UPDATE experiment SET exp_name = scrub_char(exp_name);
UPDATE external_note SET extnote_source_zdb_id = scrub_char(extnote_source_zdb_id);
UPDATE feature SET feature_line_number = scrub_char(feature_line_number);
UPDATE feature_group SET fg_group_name = scrub_char(fg_group_name);
-- UPDATE figure SET fig_caption = scrub_char(fig_caption);
UPDATE figure SET fig_label = scrub_char(fig_label);
UPDATE figure SET fig_full_label = scrub_char(fig_full_label);
UPDATE foreign_db_contains_display_group SET fdbcdg_definition = scrub_char(fdbcdg_definition);
UPDATE journal SET jrnl_print_issn = scrub_char(jrnl_print_issn);
UPDATE journal SET jrnl_online_issn = scrub_char(jrnl_online_issn);
UPDATE journal SET jrnl_publisher = scrub_char(jrnl_publisher);
UPDATE linkage SET lnkg_comments = scrub_char(lnkg_comments);
UPDATE map_metric SET mapmetric_description = scrub_char(mapmetric_description);
UPDATE mapped_marker SET comments = scrub_char(comments);
UPDATE marker SET mrkr_comments = scrub_char(mrkr_comments);
UPDATE marker_go_term_evidence SET mrkrgoev_notes = scrub_char(mrkrgoev_notes);
UPDATE marker_go_term_evidence_annotation_organization SET mrkrgoevas_definition = scrub_char(mrkrgoevas_definition);
UPDATE marker_history SET mhist_mrkr_prev_name = scrub_char(mhist_mrkr_prev_name);
UPDATE marker_history SET mhist_mrkr_name_on_mhist_date = scrub_char(mhist_mrkr_name_on_mhist_date);
UPDATE marker_history SET mhist_comments = scrub_char(mhist_comments);
UPDATE marker_relationship SET mrel_comments = scrub_char(mrel_comments);
UPDATE marker_sequence SET seq_sequence = scrub_char(seq_sequence);
UPDATE marker_sequence SET seq_sequence_2 = scrub_char(seq_sequence_2);
UPDATE marker_type_group SET mtgrp_comments = scrub_char(mtgrp_comments);
UPDATE mutation_detail_controlled_vocabulary SET mdcv_term_display_name = scrub_char(mdcv_term_display_name);
UPDATE name_precedence SET nmprec_comments = scrub_char(nmprec_comments);
-- UPDATE panels SET mappanel_comments = scrub_char(mappanel_comments);
UPDATE person SET name = scrub_char(name);
UPDATE person SET last_name = scrub_char(last_name);
UPDATE phenotype_figure_group_member SET pfiggm_member_name = scrub_char(pfiggm_member_name);
UPDATE primer_set SET fwd_primer = scrub_char(fwd_primer);
UPDATE primer_set SET rev_primer = scrub_char(rev_primer);
UPDATE publication SET keywords = scrub_char(keywords);
UPDATE publication SET pub_acknowledgment = scrub_char(pub_acknowledgment);
UPDATE publication SET pub_volume = scrub_char(pub_volume);
-- UPDATE publication SET pub_errata_and_notes = scrub_char(pub_errata_and_notes);
-- UPDATE publication SET authors = scrub_char(authors);
UPDATE publication SET pub_authors_lower = scrub_char(pub_authors_lower);
UPDATE pub_tracking_status SET pts_definition = scrub_char(pts_definition);
UPDATE record_attribution SET recattrib_source_zdb_id = scrub_char(recattrib_source_zdb_id);
UPDATE term_xref SET tx_accession = scrub_char(tx_accession);
UPDATE transcript_type SET tscriptt_definition = scrub_char(tscriptt_definition);
UPDATE tscript_type_status_definition SET ttsdef_definition = scrub_char(ttsdef_definition);
UPDATE withdrawn_data SET wd_display_note = scrub_char(wd_display_note);
UPDATE withdrawn_data SET wd_new_zdb_id = scrub_char(wd_new_zdb_id);
UPDATE withdrawn_data SET wd_old_zdb_id = scrub_char(wd_old_zdb_id);

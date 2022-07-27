
\copy (select * from tmp_dbg_clean_marker_go_term_evidence) to 'clean_marker_go_term_evidence.csv' with csv header;
\copy (select * from tmp_to_delete_marker_go_term_evidence) to 'to_delete_marker_go_term_evidence.csv' with csv header;
\copy (select * from tmp_inference_group_member_updates) to 'tmp_inference_group_member_updates.csv' with csv header;




\copy (select * from tmp_dbg_clean_marker_go_term_evidence1) to 'clean_marker_go_term_evidence1.csv' with csv header;
\copy (select * from tmp_dbg_clean_marker_go_term_evidence2) to 'clean_marker_go_term_evidence2.csv' with csv header;
\copy (select * from tmp_to_delete_marker_go_term_evidence1) to 'to_delete_marker_go_term_evidence1.csv' with csv header;
\copy (select * from tmp_to_delete_marker_go_term_evidence2) to 'to_delete_marker_go_term_evidence2.csv' with csv header;



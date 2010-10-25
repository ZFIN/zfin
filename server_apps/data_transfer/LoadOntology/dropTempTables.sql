-- dropTempTables.sql
-- this script drops all the temporary tables that are used during
-- the course of the script execution.
-- Somehow, when executed through Java, the temp tables are not dropped automatically
-- upon commit.

drop table tmp_header;

drop table tmp_syndef;

drop table tmp_term_onto_no_dups;

drop table tmp_suggestion;

drop table tmp_term_onto_with_dups;

drop table tmp_term;

drop table sec_dups;

drop table sec_oks;

drop table sec_unload;

drop table tmp_rels;

drop table tmp_rels_zdb;

drop table tmp_zfin_rels;

drop table tmp_syns;

drop table tmp_syns_with_ids;

drop table tmp_replaced;

drop table tmp_subset;

drop table tmp_term_subset;

drop table tmp_obsoletes;

drop table sec_unload_report;


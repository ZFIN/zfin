--liquibase formatted sql
--changeset pm:DLOAD-599

create temp table aa1 (alleleid text, allele text, filehtm text);
insert into aa1 (select feature_zdb_id, feature_abbrev,file from aa, feature where ftr1||'Tg'=feature_abbrev);
insert into aa1 (select feature_zdb_id, feature_abbrev,file from aa, feature where ftr2||'Tg'=feature_abbrev);
insert into aa1 (select feature_zdb_id, feature_abbrev,file from aa, feature where ftr3||'Tg'=feature_abbrev);
insert into aa1 (select feature_zdb_id, feature_abbrev,file from aa, feature where ftr4||'Tg'=feature_abbrev);
insert into aa1 (select feature_zdb_id, feature_abbrev,feature_line_number||'.htm' from feature where feature_zdb_id not in (select alleleid from aa1) and feature_abbrev like 'hi%');
insert into amsterdam_file (af_feature_zdb_id, af_file_location , af_is_overlapping_file) select distinct alleleid,filehtm,filehtm from aa1;


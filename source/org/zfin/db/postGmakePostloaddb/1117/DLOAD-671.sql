--liquibase formatted sql
--changeset pm:DLOAD-671
begin work;
update marker_go_term_evidence set mrkrgoev_relation_term_zdb_id ='ZDB-TERM-180228-3' where mrkrgoev_gflag_name='contributes to';
update marker_go_term_evidence set mrkrgoev_relation_term_zdb_id ='ZDB-TERM-180228-2' where mrkrgoev_gflag_name='colocalizes with';


update marker_go_term_evidence set mrkrgoev_relation_term_zdb_id ='ZDB-TERM-180228-4' where mrkrgoev_term_zdb_id in (select term_zdb_id from term where term_ontology='molecular_function') and mrkrgoev_relation_term_zdb_id is null;
update marker_go_term_evidence set mrkrgoev_relation_term_zdb_id ='ZDB-TERM-181002-287' where mrkrgoev_term_zdb_id in (select term_zdb_id from term where term_ontology='biological_process') and mrkrgoev_relation_term_zdb_id is null;
update marker_go_term_evidence set mrkrgoev_relation_term_zdb_id ='ZDB-TERM-180228-1' where mrkrgoev_term_zdb_id in (select term_zdb_id from term where term_ontology='cellular_component' and term_zdb_id='ZDB-TERM-091209-16423') and mrkrgoev_relation_term_zdb_id is null;
update marker_go_term_evidence set mrkrgoev_relation_term_zdb_id ='ZDB-TERM-200727-6' where mrkrgoev_term_zdb_id in (select term_zdb_id from term where term_ontology='cellular_component'and term_zdb_id!='ZDB-TERM-091209-16423') and mrkrgoev_relation_term_zdb_id is null;
commit work;
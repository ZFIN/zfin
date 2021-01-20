--liquibase formatted sql
--changeset sierra:add_qualifer_relation.sql


alter table marker_go_term_evidence
 add column mrkrgoev_relation_qualifier text;

alter table marker_go_term_evidence
 add column mrkrgoev_tag_submit_format text;

update marker_go_term_evidence set mrkrgoev_relation_qualifier ='ZDB-TERM-180228-3' where mrkrgoev_gflag_name ='contributes to';
update marker_go_term_evidence set mrkrgoev_relation_qualifier ='ZDB-TERM-180228-2' where mrkrgoev_gflag_name='colocalizes with';


update marker_go_term_evidence set mrkrgoev_relation_qualifier ='ZDB-TERM-180228-4' where mrkrgoev_term_zdb_id in (select term_ont_id from term where term_ontology='molecular_function') and mrkrgoev_gflag_name is null and mrkrgoev_contributed_by!='GO_Noctua';
update marker_go_term_evidence set mrkrgoev_relation_qualifier ='ZDB-TERM-181002-287' where mrkrgoev_term_zdb_id in (select term_ont_id from term where term_ontology='biological_process') and mrkrgoev_gflag_name is null and mrkrgoev_contributed_by!='GO_Noctua' ;
update marker_go_term_evidence set mrkrgoev_relation_qualifier ='ZDB-TERM-180228-1' where mrkrgoev_term_zdb_id in (select term_ont_id from term where term_ontology='cellular_component' and term_zdb_id='ZDB-TERM-091209-16423') and mrkrgoev_relation_qualifier is null and mrkrgoev_contributed_by!='GO_Noctua';

update marker_go_term_evidence set mrkrgoev_relation_qualifier =(select term_zdb_id from term where term_ont_id='RO:0001025')
   where mrkrgoev_term_zdb_id in (select term_ont_id from term where term_ontology='cellular_component'and term_zdb_id!='ZDB-TERM-091209-16423') and mrkrgoev_relation_qualifier is null and mrkrgoev_contributed_by!='GO_Noctua';

update marker_go_term_evidence set mrkrgoev_tag_submit_format =(select term_name from term where mrkrgoev_gflag_name=term_zdb_id) from term where mrkrgoev_relation_qualifier=term_zdb_id;
update marker_go_term_evidence set mrkrgoev_tag_submit_format ='contributes_to' where mrkrgoev_gflag_name='contributes to';
update marker_go_term_evidence set mrkrgoev_tag_submit_format ='colocalizes_with' where mrkrgoev_gflag_name='colocalizes with';

--liquibase formatted sql
--changeset prita:alterOrthoTable.sql

alter table ortholog_evidence
 add (oev_evidence_term_zdb_id varchar(50));


create temp table ortho_ev_map(tmp_ev_code varchar(2),tmp_ev_term_zdb varchar(55));
insert into ortho_ev_map (tmp_ev_code,tmp_ev_term_zdb) values('CL','ECO:0000177');
insert into ortho_ev_map (tmp_ev_code,tmp_ev_term_zdb) values('AA','ECO:0000031');
insert into ortho_ev_map(tmp_ev_code,tmp_ev_term_zdb) values('PT','ECO:0000080');
insert into ortho_ev_map(tmp_ev_code,tmp_ev_term_zdb) values('NT','ECO:0000032');
insert into ortho_ev_map(tmp_ev_code,tmp_ev_term_zdb) values('CE','ECO:0000075');
insert into ortho_ev_map(tmp_ev_code,tmp_ev_term_zdb) values('FC','ECO:0000088');
insert into ortho_ev_map(tmp_ev_code,tmp_ev_term_zdb) values('OT','ECO:0000204');

update ortholog_evidence set oev_evidence_term_zdb_id = (select term_zdb_id from ortho_ev_map,term  where tmp_ev_code=oev_evidence_code and tmp_ev_term_zdb=term_ont_id);
alter table ortholog_evidence
  add constraint (foreign key (oev_evidence_term_zdb_id)
  references term on delete cascade constraint oev_evidence_term_zdb_id_fk_odc);
  alter table ortholog_evidence
  modify (oev_evidence_term_zdb_id varchar(50) not null constraint oev_evidence_term_zdb_id_not_null);



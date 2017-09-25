--liquibase formatted sql
--changeset sierra:eco.sql

alter table disease_annotation
 add (dat_evidence_term_zdb_id varchar(50));

create index dat_evidence_term_zdb_id_index
 on disease_annotation (dat_evidence_term_zdb_id)
using btree in idxdbs2;

alter table disease_annotation
 add constraint (foreign key (dat_evidence_term_zdb_id) references
 term constraint dat_evidence_term_zdb_id_fk);

update disease_annotation
  set dat_evidence_term_zdb_id = 'ECO:0000205'
 where dat_evidence_code = 'IC';

update disease_annotation
 set dat_evidence_term_zdb_id = 'ECO:0000033'
where dat_evidence_code = 'TAS';

alter table disease_annotation
 drop dat_evidence_code;

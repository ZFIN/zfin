--liquibase formatted sql
--changeset sierra:addColumn17.sql

alter table marker_go_term_evidence
 add (mrkrgoev_protein_dblink_zdb_id varchar(50));

create index mrkrgoev_protein_dblink_id_index
 on marker_go_term_evidence (mrkrgoev_protein_dblink_zdb_id)
using btree in idxdbs1;

alter table marker_go_term_evidence
  add constraint (foreign key (mrkrgoev_protein_dblink_zdb_id)
  references db_link on delete cascade constraint mrkrgoev_protein_dblink_id_fk_odc);


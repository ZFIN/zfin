--liquibase formatted sql
--changeset christian:loadUberon

INSERT INTO ontology
            (ont_pk_id,
             ont_ontology_name,
             ont_default_namespace,
             ont_order)
VALUES      (23,
             'uberon',
             'uberon',
             23);

alter table term_xref add temp varchar(250);

update term_xref set temp = tx_accession;

alter table term_xref drop tx_accession;

alter table term_xref add tx_accession varchar(250);

update term_xref set tx_accession = temp;

alter table term_xref drop temp;

create unique index term_xref_alternate_key_index
    on term_xref (tx_term_zdb_id,tx_accession,tx_prefix)
    using btree in idxdbs2;

alter table term_xref add constraint unique(tx_term_zdb_id,
    tx_accession,tx_prefix) constraint term_xref_alternate_key;

--liquibase formatted sql
--changeset rtaylor:ZFIN-7977

-- ALTER TABLE marker_go_term_evidence RENAME COLUMN mrkrgoev_protein_dblink_zdb_id TO mrkrgoev_protein_accession;
ALTER TABLE marker_go_term_evidence ADD COLUMN mrkrgoev_protein_accession text;

CREATE INDEX "mrkrgoev_protein_accession_index" ON "public"."marker_go_term_evidence" USING btree (
    "mrkrgoev_protein_accession" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );


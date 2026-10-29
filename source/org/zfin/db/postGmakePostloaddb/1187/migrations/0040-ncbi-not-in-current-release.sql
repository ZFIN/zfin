--liquibase formatted sql
--changeset rtaylor:ncbi-not-in-current-release

-- NCBI's list of Danio rerio genes that are not in its current annotation release, keyed by NCBI Gene ID.
-- Until now this list only existed as the file notInCurrentReleaseGeneIDs.unl and as a per-gene
-- "Not in current annotation release" row in marker_annotation_status. Keeping it keyed by NCBI Gene ID
-- lets the annotation status be worked out from the gene's NCBI Gene link instead of being stored per gene.

create table external_resource.ncbi_not_in_current_release
(
    ncbi_gene_id text primary key,
    fetched_at   timestamptz not null default now()
);

comment on table external_resource.ncbi_not_in_current_release is
    'NCBI Gene IDs of Danio rerio genes not in the current annotation release. Source: NCBI E-utilities esearch, db=gene, term "Danio rerio"[Organism] NOT "annotated gene"[Properties] AND alive[prop]. Refilled by the NCBI Gene load, which keeps the stored list if a new fetch is empty or less than half the size. The annotation status depends on these rows, so they must be included in database dumps.';
comment on column external_resource.ncbi_not_in_current_release.ncbi_gene_id is 'NCBI Gene ID, matching db_link.dblink_acc_num for NCBI Gene links.';
comment on column external_resource.ncbi_not_in_current_release.fetched_at is 'When the NCBI Gene load stored this row.';

--rollback drop table external_resource.ncbi_not_in_current_release;

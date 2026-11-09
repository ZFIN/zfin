--liquibase formatted sql
--changeset rtaylor:load-publication-confers-current

-- Which of the NCBI Gene load's publications make a gene Current, read by marker_annotation_status_derived (0050).
-- The RNA publication and the 2026 supplement, which matched through GRCz12 assemblies, do. The 2023 Ensembl
-- supplement matched through GRCz11 Ensembl IDs, so on its own it does not (ZFIN-10530: "Unknown" if a gene only
-- has GRCz11 Ensembl). Neither does the legacy Vega publication.

alter table load_publication
    add column lp_confers_current_gene_status_via_ncbi boolean not null default false;

comment on column load_publication.lp_confers_current_gene_status_via_ncbi is
    'NCBI Gene load only: true if an NCBI Gene link attributed to this publication gives the gene the Current annotation status. Its meaning is defined by the annotation status view; other loads leave it false.';

update load_publication
   set lp_confers_current_gene_status_via_ncbi = true
 where lp_ld_id = (select ld_id from load where ld_name = 'NCBI Gene')
   and lp_pub_zdb_id in ('ZDB-PUB-020723-3', 'ZDB-PUB-261014-17');

--rollback alter table load_publication drop column lp_confers_current_gene_status_via_ncbi;

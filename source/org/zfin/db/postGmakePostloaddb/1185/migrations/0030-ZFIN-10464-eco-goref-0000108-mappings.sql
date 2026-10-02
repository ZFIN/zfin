--liquibase formatted sql
--changeset rtaylor:ZFIN-10464-eco-goref-0000108-mappings

-- Map the two ECO codes GO_REF:0000108 annotations arrive with, so the unified
-- DANRE-mod GPAD load can accept them (ZFIN-10464 item 3, decided: adopt).
--
-- GO_REF:0000108 is "automatic assertion of GO terms from logical inference over
-- existing annotations". In the DANRE-mod file it accounts for 7,766 rows / 3,157
-- distinct (gene, GO) pairs over 2,576 genes, and every one of those rows carries
-- one of exactly two ECO codes -- the sets are closed in both directions:
--
--   ECO:0000366  5,614 rows  logical inference from automatic annotation, automatic assertion
--   ECO:0000364  2,152 rows  logical inference from manual annotation, automatic assertion
--
-- Both map to IEA in GO's own derived mapping (gaf-eco-mapping-derived.txt), which
-- is also consistent with every other "...used in automatic assertion" code already
-- in this table (ECO:0000501, ECO:0000256, ECO:0007322 -> IEA).
--
-- Without these rows GpadParser.postProcessing cannot translate the ECO code and
-- rejects the annotation. That is not merely a failure to add: a rejected row
-- reaches none of GafJobData's new/update/existing sets, so findOutdatedEntries
-- treats it as absent from the file and DELETES any matching existing annotation.
-- See workbench/go-load-annotation-cycling.md.
--
-- The companion change is in Java: GoDefaultPublication needs a GO_REF:0000108
-- entry pointing at ZDB-PUB-260903-15, or the rows still fail at the publication
-- lookup with "Goref ID is not known or loaded". Both are required; neither alone
-- is sufficient.
--
-- Idempotent via the (egm_term_zdb_id, egm_go_evidence_code) unique index.

insert into eco_go_mapping (egm_term_zdb_id, egm_go_evidence_code)
  select term_zdb_id, 'IEA'
    from term
   where term_ont_id in ('ECO:0000366', 'ECO:0000364')
     and not exists (
       select 1 from eco_go_mapping m
        where m.egm_term_zdb_id = term.term_zdb_id
          and m.egm_go_evidence_code = 'IEA'
     );

--liquibase formatted sql
--changeset cmpich:0045-ZFIN-10330-hcr-assay

-- ZFIN-10330 / ZFINHELP-5462 : dedicated HCR assay.
--
-- Adds hybridization chain reaction as its own expression_pattern_assay row. This is a
-- controlled-vocabulary addition, not part of the Moens load itself -- curators pick it
-- from the assay dropdown for any future HCR paper. It is numbered 0045 only so that it
-- runs before migration 0050, whose expression_experiment2 rows carry the new name and
-- whose xpatex_assay_name is a FK to this table.
--
-- MMO has no HCR-specific term. MMO:0000643 "in situ expression assay" is the value the
-- source spreadsheet supplied and is a real, non-obsolete term, which matters because
-- the Alliance expression export resolves xpatassay_mmo_id through getTermByOboID() and
-- immediately dereferences the result (ZFINExpressionInfo) -- an id that is not in the
-- loaded MMO ontology would NPE the export rather than fail visibly here.
--
-- Display order 2 puts HCR directly after "mRNA in situ hybridization" (1); no existing
-- row uses 2. xpatassay_abbrev carries a unique constraint, so 'HCR' must stay unique.

insert into expression_pattern_assay
  (xpatassay_name, xpatassay_display_order, xpatassay_abbrev, xpatassay_mmo_id, xpatassay_comments)
select 'HCR in situ hybridization',
       2,
       'HCR',
       'MMO:0000643',
       'Hybridization chain reaction (HCR) RNA fluorescent in situ hybridization. A ' ||
       'signal-amplified RNA in situ method in which initiator-labelled probes trigger ' ||
       'self-assembly of fluorophore-tagged hairpins, allowing multiplexed detection of ' ||
       'several transcripts in the same specimen.'
where not exists (
  select 1 from expression_pattern_assay where xpatassay_name = 'HCR in situ hybridization'
);

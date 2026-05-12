--liquibase formatted sql

-- B10b: split lesion's "mutated amino acid(s)" into a free-text entry
-- and a dedicated HGVS.P nomenclature entry. Per the PDF "This could
-- be split into two fields: one with free-text and one with HGVS.P
-- nomenclature."
--
-- l_mutated_amino_acids stays as the natural-language note.
-- l_mutated_amino_acids_hgvs holds the formal HGVS.P string; client
-- adds an "i" link to the HGVS docs.

--changeset rtaylor:zirc-b10b-lesion-hgvs
ALTER TABLE zirc.lesion
    ADD COLUMN l_mutated_amino_acids_hgvs TEXT;

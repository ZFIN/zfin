--liquibase formatted sql

-- ZFIN-10450: a "Background on which the mutation was induced" question at the
-- bottom of the Mutagenesis section, answered from a picklist (AB, TU, WIK,
-- AB/TU, unknown) with an Other free-text escape.
--
-- One column, not two. m_mutagenesis_protocol sits beside a legacy
-- m_mutagenesis_protocol_other column from the original ZIRC schema, but
-- nothing reads that column: the selectWithOther widget writes an Other answer
-- into the bound field itself, so a standard value and a free-text override
-- land in the same place. Copying that pair would have added a second dead
-- column.
--
-- TEXT rather than a CHECK constraint against the picklist, matching
-- m_mutagenesis_stage and m_mutagenesis_protocol: the list lives in
-- ZircMutationFormSchema, an Other answer is by definition outside it, and a
-- database constraint would have to be migrated in lockstep with every label
-- change for no benefit.
--
-- Note the overlap with ga_sslp_induced_background, added by ZFIN-10442 as
-- free text on the SSLP assay and labelled "Mutation was induced on
-- background". That is the same fact recorded per assay rather than per
-- mutation, so a submitter with an SSLP assay can now enter it twice and have
-- the two disagree. Left as-is deliberately: this ticket asks only to add the
-- mutation-level question, and reconciling the two is a curator decision about
-- which one is authoritative.

--changeset cmpich:ZFIN-10450-mutation-induced-background
ALTER TABLE zirc.mutation
    ADD COLUMN IF NOT EXISTS m_induced_background TEXT;
--rollback ALTER TABLE zirc.mutation DROP COLUMN IF EXISTS m_induced_background;

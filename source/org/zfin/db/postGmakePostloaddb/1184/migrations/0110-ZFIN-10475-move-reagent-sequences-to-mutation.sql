--liquibase formatted sql

-- ZFIN-10475: the CRISPR / TALEN sequence boxes belong to the mutation, not
-- to each lesion.
--
-- ZFIN-10402 put the full mutagen vocabulary on the mutagenesis-protocol
-- picklist, which already names the mechanism (CRISPR, DNA and CRISPR, TALEN,
-- DNA and TALEN). The per-lesion "the insertion is a consequence of"
-- checklist was asking the same question a second time, one level down, and
-- the reagent used is a property of how the allele was made rather than of
-- each lesion that resulted. So the sequences move up beside the protocol and
-- the checklist goes away.
--
-- Values are not carried across. A mutation can own several lesions, so the
-- move is many-to-one and there is no non-arbitrary way to pick a winner; the
-- boxes have only existed in development, where re-entering an answer costs
-- nothing. Confirmed with the ticket owner before writing this.

--changeset zirc:zfin-10475-mutation-reagent-sequences

alter table zirc.mutation
    add column if not exists m_crispr_sequence  text,
    add column if not exists m_talen_sequence_1 text,
    add column if not exists m_talen_sequence_2 text;

comment on column zirc.mutation.m_crispr_sequence is
    'Guide sequence, collected when the mutagenesis protocol is CRISPR or DNA and CRISPR.';
comment on column zirc.mutation.m_talen_sequence_1 is
    'First arm of the TALEN pair, collected when the protocol is TALEN or DNA and TALEN.';
comment on column zirc.mutation.m_talen_sequence_2 is
    'Second arm of the TALEN pair. A TALEN cuts as a pair, so both arms are asked for.';

-- Separate changeset so the drop can be reviewed -- and if needed reverted --
-- without touching the add above.

--changeset zirc:zfin-10475-drop-lesion-reagent-sequences

alter table zirc.lesion
    drop column if exists l_insertion_origins,
    drop column if exists l_crispr_sequence,
    drop column if exists l_talen_sequence_1,
    drop column if exists l_talen_sequence_2;

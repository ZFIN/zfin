--liquibase formatted sql

-- ZFIN-10380: a ZIRC-owned place for vocabulary the Sequence Ontology does
-- not have yet.
--
-- "c-terminal peptide truncation" and "n-terminal peptide truncation" have no
-- SO term. This was checked against the loaded ontology (SO dated 2024-11-18)
-- and confirmed with the SO project: the terms do not exist. SO carries the
-- N/C-terminal split for *elongation* (SO:1000100, SO:1000101) but its
-- truncation branch (SO:1000098) has no such specialisation. Terms have been
-- requested upstream; this table holds the values in the meantime.
--
-- They cannot go in mutation_detail_controlled_vocabulary, whose primary key
-- mdcv_term_zdb_id is a foreign key to term. Two consequences of that key
-- rule out the obvious workarounds: a vocabulary entry must be an ontology
-- term, and -- since it is the primary key -- two entries can never share
-- one, so neither of these could borrow SO:0001617 (already held by
-- "polypeptide truncation").
--
-- Kept out of mdcv rather than weakening it. mdcv is read by three curation
-- tables, and its foreign key is what guarantees every curation vocabulary
-- entry is a real ontology term. That guarantee is worth more than the
-- convenience of one list.
--
-- Tokens carry a zirc: prefix so a provisional value is recognisable wherever
-- it is stored. Retiring this table is then mechanical: insert the real mdcv
-- rows, array_replace the tokens in zirc.lesion.l_protein_consequences with
-- the new term ZDB IDs, delete the rows here.

--changeset zirc:zfin-10380-zirc-vocabulary-supplement

create table if not exists zirc.vocabulary_term (
    vt_id         serial primary key,
    vt_vocabulary text    not null,
    vt_token      text    not null,
    vt_label      text    not null,
    vt_order      integer not null,
    constraint vt_vocabulary_token_unique unique (vt_vocabulary, vt_token)
);

comment on table zirc.vocabulary_term is
    'ZIRC-only vocabulary entries with no ontology term yet. Merged with '
    'mutation_detail_controlled_vocabulary by ZircVocabularyService. '
    'Provisional: retire a row once the real term exists.';

-- Renumber the protein consequences to leave 2 and 3 free for the new
-- entries. The relative order of the seven is unchanged, so the curation
-- interface -- which reads mdcv alone and will not see the supplement --
-- renders exactly as it does today.
update mutation_detail_controlled_vocabulary
set mdcv_term_order = v.new_order
from (values
    ('ZDB-TERM-130401-1609', 1),  -- polypeptide truncation
                                  -- 2, 3 reserved for the two below
    ('ZDB-TERM-130401-1598', 4),  -- amino acid substitution
    ('ZDB-TERM-130401-1596', 5),  -- amino acid deletion
    ('ZDB-TERM-130401-1597', 6),  -- amino acid insertion
    ('ZDB-TERM-130401-1600', 7),  -- non conservative amino acid substitution
    ('ZDB-TERM-130401-1601', 8),  -- elongated polypeptide
    ('ZDB-TERM-130401-1608', 9)   -- polypeptide fusion
) as v(zdb_id, new_order)
where mutation_detail_controlled_vocabulary.mdcv_term_zdb_id = v.zdb_id
  and mutation_detail_controlled_vocabulary.mdcv_used_in = 'protein_consequence_term';

insert into zirc.vocabulary_term (vt_vocabulary, vt_token, vt_label, vt_order)
select * from (values
    ('protein_consequence_term', 'zirc:c-terminal-peptide-truncation',
     'c-terminal peptide truncation', 2),
    ('protein_consequence_term', 'zirc:n-terminal-peptide-truncation',
     'n-terminal peptide truncation', 3)
) as v(vocabulary, token, label, ord)
where not exists (
    select 1 from zirc.vocabulary_term
    where vt_vocabulary = v.vocabulary and vt_token = v.token);

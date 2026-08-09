--liquibase formatted sql

-- ZFIN-10399: put the transcript-consequence pick list into curator-specified
-- order, and add "inframe insertion".
--
-- This vocabulary is shared: mutation_detail_controlled_vocabulary backs both
-- the GWT curation feature editor and the new ZIRC submission form, so this
-- changes the order curators see in both places. That is intended -- the
-- ticket records that Holly approved applying the new order to the curation
-- interface as well.
--
-- The existing mdcv_term_order values are not merely in the wrong order, they
-- contain ties: four rows share order 3, three share 6 and three share 7. A
-- tie leaves the rendered sequence to whatever the database returns, so the
-- list was never stably ordered to begin with. Assigning 17 distinct values
-- fixes the ordering and the nondeterminism together.
--
-- Terms are addressed by ZDB ID rather than display name because the display
-- name is the thing curators can edit.

--changeset zirc:zfin-10399-transcript-consequence-order

-- "inframe insertion" is new to the vocabulary but not to the ontology:
-- SO:0001821 is already loaded as ZDB-TERM-130401-1809. mdcv_term_zdb_id is
-- the primary key and carries a foreign key to term, so a vocabulary entry
-- can only ever be an already-loaded ontology term, and a term can belong to
-- only one vocabulary.
insert into mutation_detail_controlled_vocabulary
    (mdcv_term_zdb_id, mdcv_term_display_name, mdcv_term_abbreviation,
     mdcv_term_order, mdcv_used_in)
select 'ZDB-TERM-130401-1809', 'inframe insertion', null, 5, 'transcript_consequence_term'
where not exists (
    select 1 from mutation_detail_controlled_vocabulary
    where mdcv_term_zdb_id = 'ZDB-TERM-130401-1809');

update mutation_detail_controlled_vocabulary
set mdcv_term_order = v.new_order
from (values
    ('ZDB-TERM-130401-1580',  1),  -- premature stop
    ('ZDB-TERM-130401-1577',  2),  -- missense
    ('ZDB-TERM-130401-1581',  3),  -- frameshift
    ('ZDB-TERM-130401-1810',  4),  -- inframe deletion
    ('ZDB-TERM-130401-1809',  5),  -- inframe insertion (new)
    ('ZDB-TERM-130401-1573',  6),  -- stop loss
    ('ZDB-TERM-160331-88',    7),  -- start loss
    ('ZDB-TERM-130401-1616',  8),  -- 3' UTR variant
    ('ZDB-TERM-130401-1615',  9),  -- 5' UTR variant
    ('ZDB-TERM-130401-1563', 10),  -- splicing variant
    ('ZDB-TERM-130401-1620', 11),  -- splice site
    ('ZDB-TERM-130401-1564', 12),  -- cryptic splice site
    ('ZDB-TERM-130401-1566', 13),  -- cryptic donor splice site
    ('ZDB-TERM-130401-1565', 14),  -- cryptic acceptor splice site
    ('ZDB-TERM-130401-1568', 15),  -- intron gain
    ('ZDB-TERM-130401-1567', 16),  -- exon loss
    ('ZDB-TERM-160331-69',   17)   -- nonsynonymous
) as v(zdb_id, new_order)
where mutation_detail_controlled_vocabulary.mdcv_term_zdb_id = v.zdb_id
  and mutation_detail_controlled_vocabulary.mdcv_used_in = 'transcript_consequence_term';

--liquibase formatted sql
--changeset rtaylor:ZFIN-10394-repair-malformed-publication-doi

-- Repair the three pub_doi values that are not DOIs.
--
-- These survived 0030's prefix normalization because each is damaged in its own
-- way rather than merely prefixed. Every replacement below was confirmed against
-- the Europe PMC record for that PMID (title and journal both match ZFIN's):
--
--   ZDB-PUB-201222-1  PMID 33342432  Transl Neurodegener
--     "0.1186/s40035-020-00220-3" -> "10.1186/s40035-020-00220-3"
--     Leading "1" lost. Recoverable from our own value; Europe PMC agrees.
--
--   ZDB-PUB-220525-4  PMID 35609215  Mol Biol Cell
--     "doi/full/10.1091/mbc.E22-01-0015" -> "10.1091/mbc.E22-01-0015"
--     A fragment of a publisher URL. The DOI is intact inside it, so this strips
--     the "doi/full/" rather than taking Europe PMC's value -- theirs is
--     lower-cased ("mbc.e22-01-0015") while MBoC registers the uppercase E, and
--     this column deliberately preserves registered case (see 0030).
--
--   ZDB-PUB-250218-5  PMID 39959935  J Genet
--     "https://www.ias.ac.in/article/fulltext/jgen/104/0002" -> "10.1007/s12041-024-01489-3"
--     A link to the publisher's article page with no DOI in it at all. This is the
--     only one of the three that cannot be recovered from the stored value; the
--     replacement comes from Europe PMC. Consistent with the journal: s12041 is
--     Springer's code for Journal of Genetics, published by the Indian Academy of
--     Sciences (ias.ac.in).
--
-- A fourth row is repaired here for the same reason -- a wrong value with an
-- authoritative external answer, no judgement required:
--
--   ZDB-PUB-220603-8  PMID 35653336  PLoS Comput Biol
--     "10.1371/journal.pcbi.1008644" -> "10.1371/journal.pcbi.1010222"
--     This record is the "Correction:" notice for ZDB-PUB-210123-28, and it was
--     given the DOI of the article it corrects. Europe PMC assigns the correction
--     its own DOI, 10.1371/journal.pcbi.1010222. The two records are NOT
--     duplicates and both should be kept; only the DOI was wrong. Fixing it
--     removes one of the four DUPLICATE DOI groups from the report.
--
-- After this runs Check-Publication-DOI-Issues_m should report no MALFORMED rows
-- and three DUPLICATE DOI groups (6 rows), all of which need curation -- see the
-- follow-up ticket. One of those three is a legitimate publisher dual-listing and
-- may never be resolvable.
--
-- NOT a general fix. Nothing stops new malformed values arriving -- the evidence
-- points at manual entry, not Europe PMC, whose data was correct in all three
-- cases. Validating the curation UI write path is part of the follow-up.
--
-- Idempotent: each update is keyed on the exact damaged value, so a second run
-- matches nothing. Deliberately not keyed on zdb_id alone, so that a row someone
-- has already corrected by hand is left untouched.

update publication set pub_doi = '10.1186/s40035-020-00220-3'
 where zdb_id = 'ZDB-PUB-201222-1' and pub_doi = '0.1186/s40035-020-00220-3';

update publication set pub_doi = '10.1091/mbc.E22-01-0015'
 where zdb_id = 'ZDB-PUB-220525-4' and pub_doi = 'doi/full/10.1091/mbc.E22-01-0015';

update publication set pub_doi = '10.1007/s12041-024-01489-3'
 where zdb_id = 'ZDB-PUB-250218-5' and pub_doi = 'https://www.ias.ac.in/article/fulltext/jgen/104/0002';

update publication set pub_doi = '10.1371/journal.pcbi.1010222'
 where zdb_id = 'ZDB-PUB-220603-8' and pub_doi = '10.1371/journal.pcbi.1008644';

--liquibase formatted sql
--changeset rtaylor:ZFIN-10394-normalize-publication-doi

-- Normalize publication.pub_doi to the bare DOI form.
--
-- The column is free text and has accumulated resolver URLs and scheme prefixes:
--
--   https://doi.org/10.x  ·  http://dx.doi.org/10.x  ·  doi.org/10.x  ·  doi:10.x
--
-- and a handful with a DOUBLED prefix ("https://doi.org/doi:10.25335/e4ts-4572"),
-- which is why the replacement is applied twice. 297 rows change.
--
-- This matters because ZFIN-10358 made the GO loads resolve DOI: citations against
-- this column. Two of the eight DOIs on that ticket were stored as resolver URLs and
-- failed to match until the load normalized both sides at query time. Normalizing
-- the stored data is the other half of that fix, and a prerequisite for the
-- uniqueness and format constraints proposed in the follow-up ticket
-- (workbench/doi-normalization-dedupe-ticket.md).
--
-- CASE IS DELIBERATELY PRESERVED. DOI names are case-insensitive for resolution,
-- but publishers register a specific case and some of ours carry it meaningfully
-- (10.1091/mbc.E22-01-0015). Lower-casing would touch 2,969 rows and discard that.
-- The eventual uniqueness constraint should therefore be a functional index on
-- lower(pub_doi) rather than a plain UNIQUE.
--
-- Leaves alone, by design:
--   * NULL and blank values (5,703 rows) -- absence of a DOI is not an error.
--   * Three values that are not DOIs at all and need a curator, not a regex:
--       ZDB-PUB-201222-1   0.1186/s40035-020-00220-3          (leading "1" lost)
--       ZDB-PUB-220525-4   doi/full/10.1091/mbc.E22-01-0015   (publisher URL fragment)
--       ZDB-PUB-250218-5   https://www.ias.ac.in/article/...  (not a DOI)
--     Check-Publication-DOI-Issues_m reports these, along with the four DOIs that
--     currently map to two publications each.
--
-- Idempotent: a second run finds nothing left to strip.

update publication
   set pub_doi = regexp_replace(
                   regexp_replace(btrim(pub_doi),
                                  '^(https?://)?(dx[.])?doi[.]org/|^doi:[[:space:]]*', '', 'i'),
                                  '^(https?://)?(dx[.])?doi[.]org/|^doi:[[:space:]]*', '', 'i')
 where pub_doi is not null
   and btrim(pub_doi) <> ''
   and pub_doi <> regexp_replace(
                    regexp_replace(btrim(pub_doi),
                                   '^(https?://)?(dx[.])?doi[.]org/|^doi:[[:space:]]*', '', 'i'),
                                   '^(https?://)?(dx[.])?doi[.]org/|^doi:[[:space:]]*', '', 'i');

begin work;

create temp table tmp_pubs (
  pmcid text,
  mid text,
  pmid integer,
  keywords text,
  title text,
  pages text,
  abstract text,
  authors text,
  numAuthors int,
  year text,
  month text,
  day text,
  issn text,
  volume text,
  issue text,
  journaltitle text,
  iso text,
  status text,
  pubType text);

\copy tmp_pubs from 'parsePubs.log';

delete from tmp_pubs
where (authors = 'none' or authors is null)
      and numAuthors = '0';

create temp table tmp_new_pubs (
  pmcid text,
  mid text,
  zdb_id text,
  pmid integer,
  keywords text,
  title text,
  pages text,
  abstract text,
  authors text,
  year text,
  month text,
  day text,
  issn text,
  volume text,
  issue text,
  journaltitle text,
  iso text,
  status text,
  journal_zdb_id text,
  pubType text
);

insert into tmp_new_pubs
   (pmcid,
   mid ,
  pmid ,
  keywords ,
  title ,
  pages ,
  abstract ,
  authors ,
  year ,
  month ,
  day ,
  issn,
  volume ,
  issue ,
  journaltitle ,
  iso ,
  status ,
  journal_zdb_id, pubType)
  select pmcid,mid,
    pmid,
    keywords,
    title,
    pages,
    abstract,
    authors,
    year,
    month,
    day,
    issn,
    volume,
    issue,
    journaltitle,
    iso,
    status,
    journaltitle,
    pubType
  from tmp_pubs;

delete from tmp_new_pubs where pubtype='preprint';

\copy (select * from tmp_new_pubs) to 'existingPublicationsUpdating.txt' delimiter '|';
\copy (select zdb_id, pmid, title from tmp_new_pubs) to 'updatedPubSummary.txt' delimiter '|';

-- Refresh authors plus volume/pages for existing publications.
-- COALESCE+NULLIF: never overwrite an existing populated value with a blank
-- string from PubMed (avoids regressing data on a transient empty response),
-- but do refresh when PubMed has new info — needed because Epub-ahead-of-print
-- pubs are inserted without volume/pages and would otherwise stay empty
-- forever once the journal issue is published.
UPDATE publication
SET authors    = t.authors,
    pub_volume = COALESCE(NULLIF(t.volume, ''), publication.pub_volume),
    pub_pages  = COALESCE(NULLIF(t.pages,  ''), publication.pub_pages)
FROM tmp_pubs t
WHERE t.pmid = accession_no;

commit work;

--rollback work ;

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
  status text);

\copy tmp_pubs from '<!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/parsePubs.log';
\copy (select zdb_id from tmp_pubs,publication where accession_no = pmid) to '<!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/pubAlreadyinZFIN.txt' delimiter '|';

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
  journal_zdb_id text
);



insert into tmp_new_pubs
   (pmcid,
   mid,zdb_id ,
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
  journal_zdb_id)
  select pmcid,mid,get_id('PUB'),
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
    journaltitle
  from tmp_pubs
  where not exists (select 'x' from publication
  where accession_no = pmid::int);

create temp table tmp_journal_matches as 
select distinct jrnl_zdb_id, jrnl_abbrev_lower, jrnl_name_lower, jrnl_print_issn
from journal, tmp_pubs
where lower(journaltitle) = jrnl_name_lower
      or lower(iso) = jrnl_abbreV_lower
      or jrnl_print_issn = issn or jrnl_online_issn = issn
;

create temp table tmp_first_journal_to_match as 
select min(jrnl_zdb_id) as id, jrnl_abbrev_lower, jrnl_name_lower, jrnl_print_issn
from tmp_journal_matches
group by jrnl_abbrev_lower, jrnl_name_lower, jrnl_print_issn;

update tmp_new_pubs
set journal_zdb_id = (select id from tmp_first_journal_to_match
where trim(lower(journaltitle)) = jrnl_name_lower);

update tmp_new_pubs
set journal_zdb_id = (select id from tmp_first_journal_to_match
where trim(lower(iso)) = jrnl_abbrev_lower)
where journal_zdb_id is null;

update tmp_new_pubs
set journal_zdb_id = (select id from tmp_first_journal_to_match
where issn is not null
      and jrnl_print_issn is not null
      and issn = jrnl_print_issn
and journal_zdb_id is null);

update tmp_new_pubs
set journal_zdb_id = (select id from tmp_first_journal_to_match
where issn is not null
      and jrnl_online_issn is not null
      and issn = jrnl_online_issn
and journal_zdb_id is null);


create temp table tmp_new_journals as 
select distinct journaltitle, iso, issn from tmp_new_pubs
where journal_zdb_id is null;

delete from tmp_new_journals where journaltitle = 'Medical research archives' or issn = '2375-1916';

create temp table tmp_ids as
select get_id('JRNL') as id, journaltitle, iso, issn
from tmp_new_journals;

insert into zdb_active_source
  select id from tmp_ids;

\copy (select * from tmp_ids) to '<!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/newJournals.txt' delimiter '|';

insert into journal (jrnl_zdb_id, jrnl_name, jrnl_abbrev, jrnl_is_nice, jrnl_print_issn)
  select id, journaltitle, iso, false, issn
  from tmp_ids;

update tmp_new_pubs
set journal_zdb_id = (select jrnl_zdb_id from journal
where trim(lower(journaltitle)) = jrnl_name_lower)
where journal_zdb_id is null;

update tmp_new_pubs
set journal_zdb_id = (select jrnl_zdb_id from journal
where trim(lower(iso)) = jrnl_abbrev_lower)
where journal_zdb_id is null;

select iso,journaltitle from tmp_new_pubs
where journal_Zdb_id is null;

update tmp_new_pubs
set status = 'active'
where status = 'ppublish' -- no, this is not a typo. it really has two p's.
      or status = 'epublish';

update tmp_new_pubs
set status = 'Epub ahead of print'
where status = 'aheadofprint';

insert into zdb_active_source
  select zdb_id from tmp_new_pubs;

update tmp_new_pubs
set month = '0'||month
where length(month) = 1;

update tmp_new_pubs
set day = '0'||day
where length(day) = 1;

\copy (select * from tmp_new_pubs) to '<!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/newPublicationsAdded.txt' delimiter '|';

\copy (select zdb_id, pmid, title from tmp_new_pubs) to '<!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/newPubSummary.txt' delimiter '|';

insert into publication (
  pub_pmc_id,
  pub_mid,
  zdb_id,
  authors,
  pub_date,
  title,
  keywords,
  accession_no,
  pub_abstract,
  status,
  pub_volume,
  pub_pages,
  pub_jrnl_zdb_id,
  jtype)
  select
    pmcid,
    mid,
    zdb_id,
    authors,
    to_date(month||'/'||day||'/'||year,'MM/DD/YYYY'),
    title,
    keywords,
    pmid,
    abstract,
    status,
    volume,
    pages,
    journal_zdb_id,
    'Journal'
  from tmp_new_pubs
  where year is not null
        and not exists (Select 'x' from publication where accession_no = pmid);


insert into publication (
  pub_pmc_id,
  pub_mid,
  zdb_id,
  authors,
  title,
  keywords,
  accession_no,
  pub_abstract,
  status,
  pub_volume,
  pub_pages,
  pub_jrnl_zdb_id,
  jtype)
  select
    pmcid,
    mid,
    zdb_id,
    authors,
    title,
    keywords,
    pmid,
    abstract,
    status,
    volume,
    pages,
    journal_zdb_id,
    'Journal'
  from tmp_new_pubs
  where year is null
        and not exists (Select 'x' from publication where accession_no = pmid);

insert into pub_tracking_history (pth_pub_zdb_id,
                                  pth_status_id,
                                  pth_status_set_by)
  select zdb_id,
    (select pts_pk_id from pub_tracking_status where pts_status= 'READY_FOR_PROCESSING'),
    (select zdb_id from person where full_name = 'Pub Acquisition Script')
  from tmp_new_pubs
  where exists (Select 'x' from publication
  where tmp_new_pubs.zdb_id = publication.zdb_id and status = 'active');

insert into pub_tracking_history (pth_pub_zdb_id,
                                  pth_status_id,
                                  pth_status_set_by)
  select zdb_id,
    (select pts_pk_id from pub_tracking_status where pts_status= 'WAIT' and pts_status_display = 'Waiting for Activation'),
    (select zdb_id from person where full_name = 'Pub Acquisition Script')
  from tmp_new_pubs
  where exists (Select 'x' from publication
  where tmp_new_pubs.zdb_id = publication.zdb_id and status is distinct from 'active');

create temp table tmp_mesh (
  pmid integer,
  descriptor_id text,
  qualifier_id text,
  is_major boolean
) ;

\copy tmp_mesh from '<!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/parseMesh.log' delimiter '|';

insert into mesh_heading (mh_pub_zdb_id, mh_mesht_mesh_descriptor_id, mh_descriptor_is_major_topic)
  select distinct tmp_new_pubs.zdb_id, tmp_mesh.descriptor_id, tmp_mesh.is_major
  from tmp_new_pubs
    inner join tmp_mesh on tmp_new_pubs.pmid = tmp_mesh.pmid
  where tmp_mesh.qualifier_id is null;

insert into mesh_heading_qualifier (mhq_mesh_heading_id, mhq_mesht_mesh_qualifier_id, mhq_is_major_topic)
  select mesh_heading.mh_pk_id, tmp_mesh.qualifier_id, tmp_mesh.is_major
  from tmp_mesh
    inner join mesh_heading on tmp_mesh.descriptor_id = mesh_heading.mh_mesht_mesh_descriptor_id
    inner join tmp_new_pubs on tmp_mesh.pmid = tmp_new_pubs.pmid and mesh_heading.mh_pub_zdb_id = tmp_new_pubs.zdb_id
  where tmp_mesh.qualifier_id is not null;

commit work;

--rollback work ;

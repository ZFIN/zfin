begin work ;

create temp table tmp_pubs (pmid varchar(30), 
       	    	  	   	 keywords lvarchar(2200), 
				 title lvarchar(400),
				 pages varchar(30), 
				 abstract lvarchar(18000),
       				 authors lvarchar(3000), 
				 numAuthors int,
				 year varchar(4), 
				 month varchar(4), 
				 day varchar(4), 
				 issn varchar(50), 
				 volume varchar(50), 
       				 issue varchar(50), 
				 journaltitle varchar(255), 
				 iso varchar(255),
				 status varchar(20))
with no log;

load from <!--|LOAD_PUBS_DIR|-->/parsePubs.log
insert into tmp_pubs;

delete from tmp_pubs
 where authors = 'none'
 and numAuthors = '0';

create temp table tmp_new_pubs (zdb_id varchar(50),
       	    	  	       	pmid varchar(30), 
				keywords lvarchar(2200),
				title lvarchar(400), 
				pages varchar(30), 
				abstract lvarchar(18000),
       				authors lvarchar(3000), 
				numAuthors int,
				year varchar(4), 
				month varchar(4), 
				day varchar(4), 
				issn varchar(50), 
				volume varchar(50), 
       				issue varchar(50), 
				journaltitle varchar(255),
				iso varchar(255),
				status varchar(40),
				journal_zdb_id varchar(50)
);

insert into tmp_new_pubs
  select get_id('PUB'), 
  	 pmid, 
	 keywords, 
	 title,
	 pages,
	 abstract, 
	 authors, 
	 numAuthors,
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
 where not exists (Select 'x' from publication
       	   	  	  where accession_no = pmid);

select distinct jrnl_zdb_id, jrnl_abbrev_lower, jrnl_name_lower
  from journal, tmp_pubs
  where lower(journaltitle) = jrnl_name_lower
  or lower(iso) = jrnl_abbreV_lower
into temp tmp_journal_matches;

select min(jrnl_zdb_id) as id, jrnl_abbrev_lower, jrnl_name_lower
 from tmp_journal_matches
 group by jrnl_abbrev_lower, jrnl_name_lower
 into temp tmp_first_journal_to_match;

update tmp_new_pubs
 set journal_zdb_id = (Select id from tmp_first_journal_to_match
     		      	      where trim(lower(journaltitle)) = jrnl_name_lower);

update tmp_new_pubs
 set journal_zdb_id = (Select id from tmp_first_journal_to_match
     		      	      where trim(lower(iso)) = jrnl_abbrev_lower)
 where journal_zdb_id is null;

select distinct journaltitle, iso, issn from tmp_new_pubs
 where journal_zdb_id is null
 into temp tmp_new_journals;

select get_id('JRNL') as id, journaltitle, iso, issn
  from tmp_new_journals
into temp tmp_ids;

insert into zdb_active_source
 select id from tmp_ids;

unload to "<!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/newJournals.txt"
  select * from tmp_ids;

insert into journal (jrnl_zdb_id, jrnl_name, jrnl_abbrev, jrnl_is_nice, jrnl_issn
)
 select id, journaltitle, iso, 'f', issn
  from tmp_ids; 

update tmp_new_pubs
  set journal_zdb_id = (Select jrnl_zdb_id from journal
     		      	      where trim(lower(journaltitle)) = jrnl_name_lower)
 where journal_zdb_id is null;

update tmp_new_pubs
  set journal_zdb_id = (Select jrnl_zdb_id from journal
     		      	      where trim(lower(iso)) = jrnl_abbrev_lower)
 where journal_zdb_id is null;


select iso,journaltitle from tmp_new_pubs
 where journal_Zdb_id is null;

update tmp_new_pubs
  set status = 'active'
 where status = 'ppublish';

update tmp_new_pubs
  set status = 'Epub ahead of print'
 where status = 'aheadofprint';

insert into zdb_active_source
  select zdb_id from tmp_new_pubs;



update tmp_new_pubs
 set month = "0"||month
 where length(month) = 1;

update tmp_new_pubs
 set day = "0"||day
 where length(day) = 1;

unload to "<!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/newPublicationsAdded.txt"
 select * from tmp_new_pubs;

unload to "<!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/newPubSummary.txt"
 select zdb_id, pmid, title from tmp_new_pubs;

insert into publication (zdb_id, 
       	    			 authors,
				 num_auths,
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
  select zdb_id,
 	 authors,
	 numAuthors,
  	 month||"/"||day||"/"||year,
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

	insert into publication (zdb_id, 
       	    			 authors,
				 num_auths,
				 title,
				 keywords,
				 accession_no,
				 pub_abstract,
				 status,
				 pub_volume,
				 pub_pages,
				 pub_jrnl_zdb_id,
				 jtype)
  select zdb_id,
 	 authors,
	 numAuthors,
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
			 
commit work;

--rollback work ;
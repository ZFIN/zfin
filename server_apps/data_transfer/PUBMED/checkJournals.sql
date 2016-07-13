begin work;

create temp table tmp_ncbi_journals (
  title varchar(255),
  medAbbr varchar(255),
  issnPrint varchar(100),
  issnOnline varchar(100),
  isoAbbr varchar(255),
  nlmID varchar(100))
with no log;

load from <!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/journalsFromNCBI.txt
  insert into tmp_ncbi_journals;

select count(*) as noIssn from tmp_ncbi_journals where issnPrint is null;
select count(*) as noTitle from tmp_ncbi_journals where title is null;
select count(distinct lower(title)) as numTitle from tmp_ncbi_journals;
select count(distinct issnPrint) as numIssn from tmp_ncbi_journals;
select count(*) as noNlmID from tmp_ncbi_journals where nlmID is null;
select count(*) as numAllJournals from tmp_ncbi_journals;
select count(distinct nlmID) as numNlmID from tmp_ncbi_journals; 

select j1.issnPrint, j1.title, j1.nlmID 
  from tmp_ncbi_journals j1 
 where j1.issnPrint is not null 
   and exists(select "x" from tmp_ncbi_journals j2 
               where j2.issnPrint = j1.issnPrint 
                 and j2.nlmID != j1.nlmID) 
group by j1.issnPrint, j1.title, j1.nlmID
order by j1.issnPrint, j1.title, j1.nlmID
into temp duplJournalsNCBI;

unload to <!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/duplJournalsNCBI.txt select * from duplJournalsNCBI; 

delete from tmp_ncbi_journals allJournal
 where exists(select "x" from duplJournalsNCBI del
               where del.nlmID = allJournal.nlmID);

select t1.title, t1.issnPrint, t1.nlmID
  from tmp_ncbi_journals t1
 where t1.title is not null
   and exists(select "x" from tmp_ncbi_journals t2
               where lower(t2.title) = lower(t1.title)
                 and t2.nlmID != t1.nlmID)        
  group by t1.title, t1.issnPrint, t1.nlmID
  order by t1.title, t1.issnPrint, t1.nlmID
into temp sameTitleNCBIjournals;

unload to <!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/duplTitlesNCBI.txt select * from sameTitleNCBIjournals;

delete from tmp_ncbi_journals allJournal 
 where exists(select "x" from sameTitleNCBIjournals del
               where del.nlmID = allJournal.nlmID);


select count(*) as numAllJournals from tmp_ncbi_journals;

select count(distinct title) as numTitle from tmp_ncbi_journals;

select count(distinct lower(title)) as numLowerTitle from tmp_ncbi_journals;
  
select count(distinct issnPrint) as numPrintIssn from tmp_ncbi_journals;

select count(jrnl_zdb_id) as noIssnZFIN from journal where jrnl_print_issn is null;

update journal
   set jrnl_print_issn = (select issnPrint from tmp_ncbi_journals
                           where issnPrint is not null
                             and lower(jrnl_name) = lower(title))
 where jrnl_print_issn is null;

select count(jrnl_zdb_id) as noIssnZFINafter from journal where jrnl_print_issn is null;


select j1.jrnl_zdb_id as journalZdbID, j1.jrnl_name, j1.jrnl_print_issn, issnPrint, j1.jrnl_abbrev, medAbbr, nlmID
  from journal j1, tmp_ncbi_journals
 where issnPrint is not null
   and j1.jrnl_print_issn is not null
   and title is not null
   and j1.jrnl_name is not null
   and j1.jrnl_name = title
   and j1.jrnl_print_issn != issnPrint
   and not exists(select "x" from journal j2
                   where j2.jrnl_name = j1.jrnl_name
                     and j2.jrnl_zdb_id != j1.jrnl_zdb_id)
 order by j1.jrnl_name
into temp possibleWrongIssnPrint;

unload to <!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/possibleWrongIssnPrint.txt select * from possibleWrongIssnPrint;

update journal
   set jrnl_online_issn = (select issnOnline from tmp_ncbi_journals
                            where issnOnline is not null
                              and issnPrint = jrnl_print_issn)
 where jrnl_print_issn is not null
   and jrnl_online_issn is null
   and not exists(select "x" from possibleWrongIssnPrint
                   where journalZdbID = jrnl_zdb_id);

update journal
   set jrnl_nlmid = (select nlmID from tmp_ncbi_journals
                      where issnOnline is not null
                        and issnPrint = jrnl_print_issn)    
 where jrnl_print_issn is not null 
   and jrnl_nlmid is null 
   and not exists(select "x" from possibleWrongIssnPrint 
                   where journalZdbID = jrnl_zdb_id);

update journal
   set jrnl_medabbrev = (select medAbbr from tmp_ncbi_journals
                          where issnOnline is not null
                            and issnPrint = jrnl_print_issn)    
 where jrnl_print_issn is not null 
   and jrnl_medabbrev is null 
   and not exists(select "x" from possibleWrongIssnPrint 
                   where journalZdbID = jrnl_zdb_id);

update journal
   set jrnl_isoabbrev = (select isoAbbr from tmp_ncbi_journals
                          where issnOnline is not null
                            and issnPrint = jrnl_print_issn)    
 where jrnl_print_issn is not null 
   and jrnl_isoabbrev is null 
   and not exists(select "x" from possibleWrongIssnPrint 
                   where journalZdbID = jrnl_zdb_id);


select j1.jrnl_print_issn, j1.jrnl_zdb_id, j1.jrnl_name, j1.jrnl_online_issn, j1.jrnl_abbrev, j1.jrnl_nlmid
  from journal j1
 where j1.jrnl_print_issn is not null
   and exists(select "x" from journal j2
               where j2.jrnl_print_issn = j1.jrnl_print_issn
                 and j2.jrnl_zdb_id != j1.jrnl_zdb_id)
group by j1.jrnl_print_issn, j1.jrnl_name, j1.jrnl_abbrev, j1.jrnl_zdb_id, j1.jrnl_nlmid, j1.jrnl_online_issn
order by j1.jrnl_print_issn, j1.jrnl_name, j1.jrnl_abbrev
into temp duplJournalsByIssnPrint;    

unload to <!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/duplicateJournals.txt select * from duplJournalsByIssnPrint;

commit work;

--rollback work ;

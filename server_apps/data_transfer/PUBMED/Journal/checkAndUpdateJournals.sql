-- checkAndUpdateJournals.sql
-- populate the parsed NLM journal data into a temp table
-- clean up the NLM journal data in that temp table
-- fill/update print issn with NLM data based on the same journal title/abbrev
-- report the wrong print issn with our data before correcting them
-- update other fields with NLM data based by the same print issn
-- report possible duplicates with the same print issn but different titles
-- report all journals missing issn print

begin work;

create temp table tmp_ncbi_journals (
  title varchar(255),
  medAbbr varchar(255),
  issnPrint varchar(100),
  issnOnline varchar(100),
  isoAbbr varchar(255),
  nlmID varchar(100))
with no log;

load from <!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/Journal/journalsFromNCBI.txt
  insert into tmp_ncbi_journals;

create index tnj_title_index
 on tmp_ncbi_journals(title) using btree in idxdbs3;

create index tnj_issnPrint_index
 on tmp_ncbi_journals(issnPrint) using btree in idxdbs3;

create index tnj_nlmID_index
 on tmp_ncbi_journals(nlmID) using btree in idxdbs3;

update statistics high for table tmp_ncbi_journals;

-- temp table with pre-computed lower case values
select lower(title) as title, lower(medAbbr) as medAbbr, issnPrint, issnOnline, lower(isoAbbr) as isoAbbr, nlmID
 from tmp_ncbi_journals into temp lower_ncbi_journals;

create index lnj_title_index
 on lower_ncbi_journals(title) using btree in idxdbs3;

create index lnj_issnPrint_index
 on lower_ncbi_journals(issnPrint) using btree in idxdbs3;

create index lnj_nlmID_index
 on lower_ncbi_journals(nlmID) using btree in idxdbs3;

update statistics high for table lower_ncbi_journals;

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
into temp duplJournalsNCBI;

-- get rid of the NLM data with duplicated issn print

delete from tmp_ncbi_journals allJournal
 where exists(select "x" from duplJournalsNCBI del
               where del.nlmID = allJournal.nlmID);

delete from lower_ncbi_journals allJournal
 where exists(select "x" from duplJournalsNCBI del
               where del.nlmID = allJournal.nlmID);

select t1.title, t1.issnPrint, t1.nlmID
  from lower_ncbi_journals t1
 where t1.title is not null
   and exists(select "x" from lower_ncbi_journals t2
               where t2.title = t1.title
                 and t2.nlmID != t1.nlmID)        
into temp sameTitleNCBIjournals;

-- get rid of the NLM data with duplicated titles

delete from tmp_ncbi_journals allJournal 
 where exists(select "x" from sameTitleNCBIjournals del
               where del.nlmID = allJournal.nlmID);

delete from lower_ncbi_journals allJournal
 where exists(select "x" from sameTitleNCBIjournals del
               where del.nlmID = allJournal.nlmID);

select count(*) as num_temp from tmp_ncbi_journals;

select count(distinct title) as num_temp_titles from tmp_ncbi_journals;

select count(*) as num_lower from lower_ncbi_journals;

select count(distinct title) as num_lower_titles from lower_ncbi_journals;

select t1.medAbbr, t1.issnPrint, t1.nlmID
  from lower_ncbi_journals t1
 where t1.medAbbr is not null
   and exists(select "x" from lower_ncbi_journals t2
               where t2.medAbbr = t1.medAbbr
                 and t2.nlmID != t1.nlmID)
into temp sameMedAbbrNCBIjournals;

-- get rid of the NLM data with duplicated MedAbbr

delete from tmp_ncbi_journals allJournal
 where exists(select "x" from sameMedAbbrNCBIjournals del
               where del.nlmID = allJournal.nlmID);

delete from lower_ncbi_journals allJournal
 where exists(select "x" from sameMedAbbrNCBIjournals del
               where del.nlmID = allJournal.nlmID);

select t1.isoAbbr, t1.issnPrint, t1.nlmID
  from lower_ncbi_journals t1
 where t1.isoAbbr is not null
   and exists(select "x" from tmp_ncbi_journals t2
               where t2.isoAbbr = t1.isoAbbr
                 and t2.nlmID != t1.nlmID)
into temp sameIsoAbbrNCBIjournals;

-- get rid of the NLM data with duplicated IsoAbbr

delete from tmp_ncbi_journals allJournal
 where exists(select "x" from sameIsoAbbrNCBIjournals del
               where del.nlmID = allJournal.nlmID);

delete from lower_ncbi_journals allJournal
 where exists(select "x" from sameIsoAbbrNCBIjournals del
               where del.nlmID = allJournal.nlmID);

select count(*) as numAllJournals from tmp_ncbi_journals;

select count(distinct title) as numTitle from tmp_ncbi_journals;

select count(distinct lower(title)) as numLowerTitle from tmp_ncbi_journals;
  
select count(distinct issnPrint) as numPrintIssn from tmp_ncbi_journals;

select count(jrnl_zdb_id) as noIssnZFIN from journal where jrnl_print_issn is null;

-- blank journal issn print to the value of NLM based on same title

update journal
   set jrnl_print_issn = (select issnPrint from lower_ncbi_journals
                           where issnPrint is not null
                             and jrnl_name = lower(title))
 where jrnl_print_issn is null
   and exists(select "x" from lower_ncbi_journals
               where title = lower(jrnl_name));

select count(jrnl_zdb_id) as noIssnZFINafter1 from journal where jrnl_print_issn is null;

-- blank journal issn print to the value of NLM based on same Med abbrev

update journal
   set jrnl_print_issn = (select issnPrint from lower_ncbi_journals
                           where issnPrint is not null
                             and jrnl_abbrev_lower = medAbbr)
 where jrnl_print_issn is null
   and exists(select "x" from lower_ncbi_journals 
               where medAbbr = jrnl_abbrev_lower);

select count(jrnl_zdb_id) as noIssnZFINafter2 from journal where jrnl_print_issn is null;

-- blank journal issn print to the value of NLM based on same Iso abbrev

update journal
   set jrnl_print_issn = (select issnPrint from lower_ncbi_journals
                           where issnPrint is not null
                             and jrnl_abbrev_lower = isoAbbr)
 where jrnl_print_issn is null
   and exists(select "x" from lower_ncbi_journals
               where isoAbbr = jrnl_abbrev_lower);

select count(jrnl_zdb_id) as noIssnZFINafter3 from journal where jrnl_print_issn is null;

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
into temp wrongIssnPrint;

-- dump the issn print that disagree with NLM (same title, different issn print)

unload to <!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/Journal/wrongIssnPrintByTitle.txt select * from wrongIssnPrint;

select j1.jrnl_zdb_id as journalZdbID, j1.jrnl_abbrev, j1.jrnl_print_issn, issnPrint, j1.jrnl_name, nlmID
  from journal j1, tmp_ncbi_journals
 where issnPrint is not null
   and j1.jrnl_print_issn is not null
   and medAbbr is not null
   and j1.jrnl_abbrev is not null
   and j1.jrnl_abbrev = medAbbr
   and j1.jrnl_print_issn != issnPrint
   and not exists(select "x" from journal j2
                   where j2.jrnl_abbrev = j1.jrnl_abbrev
                     and j2.jrnl_zdb_id != j1.jrnl_zdb_id)
 order by j1.jrnl_abbrev
into temp wrongIssnPrintByMedAbbr;

-- dump the issn print that disagree with NLM (same abbrev/MedAbbr, different issn print)

! echo "                           from wrongIssnPrintByMedAbbr."
unload to <!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/wrongIssnPrintByMedAbbrWithNoHeader.txt select * from wrongIssnPrintByMedAbbr;

select j1.jrnl_zdb_id as journalZdbID, j1.jrnl_abbrev, j1.jrnl_print_issn, issnPrint, j1.jrnl_name, nlmID           
  from journal j1, tmp_ncbi_journals
 where issnPrint is not null
   and j1.jrnl_print_issn is not null
   and isoAbbr is not null
   and j1.jrnl_abbrev is not null
   and j1.jrnl_abbrev = isoAbbr
   and j1.jrnl_print_issn != issnPrint         
   and not exists(select "x" from journal j2              
                   where j2.jrnl_abbrev = j1.jrnl_abbrev
                     and j2.jrnl_zdb_id != j1.jrnl_zdb_id)
 order by j1.jrnl_abbrev
into temp wrongIssnPrintByIsoAbbr;

-- dump the issn print that disagree with NLM (same abbrev/isoAbbr, different issn print)

unload to <!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/Journal/wrongIssnPrintByIsoAbbr.txt select * from wrongIssnPrintByIsoAbbr;

-- update journal issn print to the value of NLM based on same title

update journal
   set jrnl_print_issn = (select issnPrint from wrongIssnPrint
                           where journalZdbID = jrnl_zdb_id)
 where jrnl_print_issn is not null
   and exists(select "x" from wrongIssnPrint
               where jrnl_zdb_id = journalZdbID);

-- update journal issn print to the value of NLM based on same abbrev/MedAbbr

update journal
   set jrnl_print_issn = (select issnPrint from wrongIssnPrintByMedAbbr
                           where journalZdbID = jrnl_zdb_id)
 where jrnl_print_issn is not null
   and exists(select "x" from wrongIssnPrintByMedAbbr
               where jrnl_zdb_id = journalZdbID);

-- update journal issn print column to the value of NLM based on same abbrev/MedAbbr

update journal
   set jrnl_print_issn = (select issnPrint from wrongIssnPrintByIsoAbbr
                           where journalZdbID = jrnl_zdb_id)
 where jrnl_print_issn is not null
   and exists(select "x" from wrongIssnPrintByIsoAbbr
               where jrnl_zdb_id = journalZdbID);


-- update values of issn online column based on same issn print

update journal
   set jrnl_online_issn = (select issnOnline from tmp_ncbi_journals
                            where issnOnline is not null
                              and issnPrint = jrnl_print_issn)
 where jrnl_print_issn is not null
   and (jrnl_online_issn is null 
        or exists(select "x" from tmp_ncbi_journals
                   where issnOnline is not null
                     and jrnl_online_issn is not null
                     and issnOnline != jrnl_online_issn)
        );

-- update values of nlm id column based on same issn print

update journal
   set jrnl_nlmid = (select nlmID from tmp_ncbi_journals
                      where nlmID is not null
                        and issnPrint = jrnl_print_issn)    
 where jrnl_print_issn is not null
   and (jrnl_nlmid is null
        or exists(select "x" from tmp_ncbi_journals 
                   where nlmID is not null 
                     and jrnl_nlmid is not null           
                     and nlmID != jrnl_nlmid) 
        ); 


-- update values of medAbbr column based on same issn print

update journal
   set jrnl_medabbrev = (select medAbbr from tmp_ncbi_journals
                          where medAbbr is not null
                            and issnPrint = jrnl_print_issn)    
 where jrnl_print_issn is not null
   and (jrnl_medabbrev is null
        or exists(select "x" from tmp_ncbi_journals
                   where medAbbr is not null     
                     and jrnl_medabbrev is not null      
                     and medAbbr != jrnl_medabbrev)           
        ); 

-- update values of isoAbbr column based on same issn print

update journal
   set jrnl_isoabbrev = (select isoAbbr from tmp_ncbi_journals         
                          where isoAbbr is not null   
                            and issnPrint = jrnl_print_issn)
 where jrnl_print_issn is not null
   and (jrnl_isoabbrev is null 
        or exists(select "x" from tmp_ncbi_journals
                   where isoAbbr is not null
                     and jrnl_isoabbrev is not null
                     and isoAbbr != jrnl_isoabbrev)
        ); 


-- records of temp table that have titles which are already in journal table (for avoding breaking unique constraint)
select issnPrint as issnP
  from lower_ncbi_journals
 where issnPrint is not null
   and title is not null
   and exists (select "x"
                 from journal
                where lower(jrnl_name) = title)
into temp titleAlreadyThere with no log;

delete from tmp_ncbi_journals
  where exists(select "x" 
                 from titleAlreadyThere
                where issnP = issnPrint);

delete from lower_ncbi_journals
  where exists(select "x" 
                 from titleAlreadyThere
                where issnP = issnPrint); 

-- update values of journal name/title based on same issn print

select jrnl_print_issn as issn, jrnl_zdb_id as zdbid, jrnl_name as oldname, t.title as newname
  from journal, tmp_ncbi_journals t, lower_ncbi_journals lowercase
 where jrnl_print_issn = t.issnPrint
   and jrnl_print_issn = lowercase.issnPrint
   and lower(jrnl_name) != lowercase.title
into temp titlesToChange;

select t1.issn as issnD
  from titlesToChange t1
 where exists(select "x" from titlesToChange t2
               where t2.oldname != t1.oldname
                 and t2.newname = t1.newname)
 into temp problemRecords;

delete from titlesToChange 
 where exists(select "x" from problemRecords
               where issnD = issn);

update journal
   set jrnl_name = (select newname from titlesToChange
                     where issn = jrnl_print_issn
                       and oldname = jrnl_name
                   )
 where jrnl_print_issn is not null
   and exists (select "x"
                 from titlesToChange
                where issn = jrnl_print_issn);

unload to <!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/Journal/titlesUpdated.txt select * from titlesToChange;

-- report possible duplicated journals (same issn print, different zdb id)

select j1.jrnl_print_issn, j1.jrnl_zdb_id, j1.jrnl_name, j1.jrnl_online_issn, j1.jrnl_abbrev, j1.jrnl_nlmid
  from journal j1
 where j1.jrnl_print_issn is not null
   and exists(select "x" from journal j2
               where j2.jrnl_print_issn = j1.jrnl_print_issn
                 and j2.jrnl_zdb_id != j1.jrnl_zdb_id)
group by j1.jrnl_print_issn, j1.jrnl_name, j1.jrnl_abbrev, j1.jrnl_zdb_id, j1.jrnl_nlmid, j1.jrnl_online_issn
order by j1.jrnl_print_issn, j1.jrnl_name, j1.jrnl_abbrev
into temp duplJournalsByIssnPrint;    

unload to <!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/Journal/duplicateJournals.txt select * from duplJournalsByIssnPrint;

-- report journals missing issn print

unload to <!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/Journal/journalsMissingIssnPrint.txt 
select jrnl_zdb_id, jrnl_name, jrnl_abbrev
  from journal
 where jrnl_print_issn is null                            
group by jrnl_name, jrnl_zdb_id, jrnl_abbrev
order by jrnl_name, jrnl_zdb_id, jrnl_abbrev;

commit work;

--rollback work ;


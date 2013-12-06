unload to <!--|ROOT_PATH|-->/server_apps/DB_maintenance/reportRecords.txt
select title 
      from publication, journal
      where zdb_id not in (
	"ZDB-PUB-021016-117", { "ZDB-PUB-961014-110", }
	"ZDB-PUB-961014-169", { "ZDB-PUB-961014-170", }
	"ZDB-PUB-021016-125", { "ZDB-PUB-961014-1217", }
	"ZDB-PUB-021016-60",  { "ZDB-PUB-991014-9", }
	"ZDB-PUB-000125-1",   { "ZDB-PUB-990525-2", }
	"ZDB-PUB-961014-288", { "ZDB-PUB-961014-289", }
	"ZDB-PUB-010718-37",  { "ZDB-PUB-010912-30", }
	"ZDB-PUB-981110-12",  { "ZDB-PUB-990218-4", }
	"ZDB-PUB-021016-112", "ZDB-PUB-961014-758", { "ZDB-PUB-961014-759", }
	"ZDB-PUB-000125-3",   { "ZDB-PUB-010131-19", }
	"ZDB-PUB-021015-13",  { "ZDB-PUB-030211-13", }
	"ZDB-PUB-961014-1233",{ "ZDB-PUB-961014-1234",}
	"ZDB-PUB-961014-106", { "ZDB-PUB-961014-107",}
	"ZDB-PUB-010417-9",   { "ZDB-PUB-990414-35",}
	"ZDB-PUB-010711-2",   { "ZDB-PUB-010814-8", }
	"ZDB-PUB-000824-10",  { "ZDB-PUB-990824-40",}
	"ZDB-PUB-010912-1",   { "ZDB-PUB-021017-13",}
        "ZDB-PUB-980420-9",   { "ZDB-PUB-030425-13",}
        "ZDB-PUB-010718-13",  { "ZDB-PUB-020913-1", }
        "ZDB-PUB-990414-54",  { "ZDB-PUB-021017-3", }
        "ZDB-PUB-010718-27",  { "ZDB-PUB-010821-1", }
        "ZDB-PUB-021017-74"   {, "ZDB-PUB-041012-5" }
        ,"ZDB-PUB-010918-3"   {, "ZDB-PUB-040216-6" }
        , "ZDB-PUB-050127-1" {, "ZDB-PUB-030408-12"}
        )
        and jrnl_abbrev <> "ZFIN Direct Data Submission"
        and jrnl_zdb_id = pub_jrnl_zdb_id
      group by title 
      having count(*) > 1 
     into temp dup_titles with no log;

select p.title, p.accession_no, p.zdb_id, p.authors, p.pub_date, jrnl_abbrev, p.pub_volume, p.pub_pages 
        from publication p, dup_titles d, journal
        where p.title = d.title 
        and jrnl_zdb_id = pub_jrnl_zdb_id
        order by p.title, p.zdb_id;

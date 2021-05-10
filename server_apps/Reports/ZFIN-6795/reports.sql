begin work;

\copy (select dblink_linked_recid from db_link where dblink_fdbcont_zdb_id='ZDB-FDBCONT-131021-1' and dblink_acc_num like 'ENSDARG%' and dblink_linked_recid not in (select dblink_linked_recid from db_link where dblink_fdbcont_zdb_id='ZDB-FDBCONT-061018-1')) to 'geneswithonlyensembl.txt';

\copy (select distinct recattrib_source_zdb_id from record_attribution, db_link where dblink_zdb_id = recattrib_data_zdb_id and dblink_fdbcont_zdb_id='ZDB-FDBCONT-061018-1') to 'listofGRCz11refs.txt' ;
\copy (select distinct recattrib_source_zdb_id,count(dblink_zdb_id) from record_attribution, db_link where dblink_zdb_id = recattrib_data_zdb_id and dblink_fdbcont_zdb_id='ZDB-FDBCONT-061018-1' group by recattrib_source_zdb_id) to 'GRCZ11refsandcounts.txt';
select dblink_linked_recid as geneid into tmpdb from db_link where dblink_fdbcont_zdb_id='ZDB-FDBCONT-061018-1' group by dblink_linked_recid having count(*) > 1;
--select distinct geneid,recattrib_source_zdb_id  from tmpdb,db_link,record_attribution where geneid=dblink_linked_recid and dblink_fdbcont_zdb_id='ZDB-FDBCONT-061018-1' and dblink_zdb_id=recattrib_data_zdb_id and  recattrib_source_zdb_id in ('ZDB-PUB-061101-1', 'ZDB-PUB-190221-12') group by geneid,recattrib_source_zdb_id  order by geneid;
\copy (select distinct geneid,recattrib_source_zdb_id  from tmpdb,db_link,record_attribution where geneid=dblink_linked_recid and dblink_fdbcont_zdb_id='ZDB-FDBCONT-061018-1' and dblink_zdb_id=recattrib_data_zdb_id and recattrib_source_zdb_id in ('ZDB-PUB-061101-1', 'ZDB-PUB-190221-12') group by geneid,recattrib_source_zdb_id having count(recattrib_source_zdb_id) =2 order by geneid) to 'multiplegrc11withcorrectattr.txt';

\copy (select distinct geneid,recattrib_source_zdb_id  from tmpdb,db_link,record_attribution where geneid=dblink_linked_recid and dblink_fdbcont_zdb_id='ZDB-FDBCONT-061018-1' and dblink_zdb_id=recattrib_data_zdb_id and recattrib_source_zdb_id not in ('ZDB-PUB-061101-1', 'ZDB-PUB-190221-12') group by geneid,recattrib_source_zdb_id having count(recattrib_source_zdb_id) =2 order by geneid) to 'multiplegrc11withincorrectattr.txt';

rollback work;

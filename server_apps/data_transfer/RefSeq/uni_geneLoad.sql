--RefSeq files are downloaded via ftp then parsed into informix loadable
--.unl files. RefSeq file formats are expected to be well formed per there
--documentation on 05/31/02. Script will load nothing if RefSeq files are 
--not well formatted.

--Static Variables
--db_name     - RefSeq


--MAKE INFORMIX LOAD FILES


--LOAD INTO TEMP TABLES

--ZEBRAFISH locus_link
create temp table LL_ZDB
  (
    llzdb_ll_id		varchar (50) not null,
    llzdb_zdb_id	varchar (50) not null
  )
with no log;

!echo 'LOAD LL_ID.UNL'
load from 'll_id.unl' insert into ll_zdb;

create index llzdb_ll_id_index on ll_zdb
    (llzdb_ll_id) using btree;
create index llzdb_zdb_id_index on ll_zdb
    (llzdb_zdb_id) using btree in idxdbs3;

--UNI_GENE
create temp table uni_gene
  (
    uni_ll_id		varchar (50) not null,
    uni_cluster_id	varchar (50) not null
  )
with no log;

!echo 'load loc2UG.unl'
load from 'loc2UG.unl' insert into uni_gene;

create index uni_ll_id_index on uni_gene
    (uni_ll_id) using btree in idxdbs3;
create index uni_cluster_id_index on uni_gene
    (uni_cluster_id) using btree in idxdbs3;


--TMP_DB_LINK
create temp table tmp_db_link
  (
    linked_recid 	varchar(50),
    db_name 		varchar(50),
    acc_num 		varchar(50),
    info 		varchar(80),
    dblink_zdb_id    	varchar(50)
  )
with no log;

!echo 'insert into temp_db_link'
insert into tmp_db_link
  select
    llzdb_zdb_id,
    'UniGENE',
    uni_cluster_id,
    '',
    get_id('DBLINK')
  from uni_gene, ll_zdb, zdb_active_data
  where uni_ll_id = llzdb_ll_id
    and llzdb_zdb_id = zactvd_zdb_id
;


begin work;

insert into foreign_db 
  values 
    ( 
      'UniGENE',
      'http://www.ncbi.nlm.nih.gov/UniGene/query.cgi?ORG=Dr&TEXT=',
      '',
      'sequence',
      3
    )
;

--INSERT NEW DATA
!echo 'remove UniGENE records'
delete from zdb_active_data where zactvd_zdb_id in (
 select dblink_zdb_id from db_link 
 where db_name = 'UniGENE'
  and acc_num in (
          select b.acc_num 
          from tmp_db_link b
          where b.db_name = 'UniGENE'
      )
  and dblink_zdb_id in (
          select recattrib_data_zdb_id 
          from record_attribution
          where recattrib_source_zdb_id = "ZDB-PUB-020723-3"
      )
  and dblink_zdb_id not in (
          select a.recattrib_data_zdb_id 
          from record_attribution a, record_attribution b
          where a.recattrib_source_zdb_id = "ZDB-PUB-020723-3"
            and b.recattrib_source_zdb_id != "ZDB-PUB-020723-3"
            and a.recattrib_data_zdb_id = b.recattrib_data_zdb_id
      )
);

!echo 'get all UniGENE db_links that remain'
  select * 
  from db_link 
  where db_name = "UniGENE"
  into temp unigene_link
  with no log;


!echo 'add active source and active data'
insert into zdb_active_data select dblink_zdb_id from tmp_db_link
       where acc_num not in (select acc_num from unigene_link);


!echo 'insert new db_links'
insert into db_link select * from tmp_db_link;


!echo 'Attribute RefSeq links to an artificial pub record.'
insert into record_attribution
      (recattrib_data_zdb_id, recattrib_source_zdb_id)
    select dblink_zdb_id, 'ZDB-PUB-020723-3'
    from a.db_link, tmp_db_link tmp
    where a.db_name = "UniGENE"
      and a.acc_num = tmp.acc_num
;

commit work;


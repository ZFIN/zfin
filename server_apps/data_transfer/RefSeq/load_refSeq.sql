--RefSeq files are downloaded via ftp then parsed into informix loadable
--.unl files. RefSeq file formats are expected to be well formed per there
--documentation on 05/31/02. Script will load nothing if RefSeq files are 
--not well formatted.
--
--Download
--ftp.ncbi.nih.gov/refseq/LocusLink/
--
--Static Variables
--db_name     - RefSeq


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
    (llzdb_zdb_id) using btree;

--HUMAN locus_link
create temp table LL_GDB
  (
    llgdb_ll_id		varchar (50) not null,
    llgdb_omim_id	varchar (50),
    llgdb_gdb_id	varchar (50) not null
  )
with no log;

!echo 'load ll_hs_id.unl'
load from 'll_hs_id.unl' insert into ll_gdb;

create index llgdb_ll_id_index on ll_gdb
    (llgdb_ll_id) using btree;
create index llgdb_gdb_id_index on ll_gdb
    (llgdb_gdb_id) using btree;

--MOUSE locus_link
create temp table LL_MGI
  (
    llmgi_ll_id		varchar (50) not null,
    llmgi_mgi_id	varchar (50) not null
  )
with no log;

!echo 'load ll_mm_id.unl'
load from 'll_mm_id.unl' insert into ll_mgi;

create index llmgi_ll_id_index on ll_mgi
    (llmgi_ll_id) using btree;
create index llmgi_gdb_id_index on ll_mgi
    (llmgi_mgi_id) using btree;
   

--REFSEQ ACCESSION NUM--
create temp table REF_SEQ_ACC
  (
    refseq_ll	varchar (50) not null,
    refseq_acc	varchar (50) not null
  )
with no log;

!echo 'load loc2ref.unl'
load from 'loc2ref.unl' insert into ref_seq_acc;

create index refseq_ll_index on ref_seq_acc
    (refseq_ll) using btree;
create index refseq_acc_index on ref_seq_acc
    (refseq_acc) using btree;
   

--GENBANK ACCESSION NUM--
create temp table GENBANK_ACC
  (
    gbacc_ll	varchar (50) not null,
    gbacc_acc	varchar (50) not null
  )
with no log;

!echo 'load loc2acc.unl'
load from 'loc2acc.unl' insert into genbank_acc;

create index genbank_ll_index on genbank_acc
    (gbacc_ll) using btree;
create index genbank_acc_index on genbank_acc
    (gbacc_acc) using btree;

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
    (uni_ll_id) using btree;
create index uni_cluster_id_index on uni_gene
    (uni_cluster_id) using btree;


--TMP_DB_LINK
create temp table tmp_db_link
  (
    linked_recid 	varchar(50),
    db_name 		varchar(50),
    acc_num 		varchar(50),
    info 		varchar(80),
    dblink_zdb_id	varchar(50)
  )
with no log;

!echo 'insert RefSeq into temp_db_link'
insert into tmp_db_link
  select
    zactvd_zdb_id,
    'RefSeq',
    refseq_acc,
    '',
    get_id('DBLINK')
  from ref_seq_acc, ll_zdb, zdb_active_data
  where refseq_ll = llzdb_ll_id
    and llzdb_zdb_id = zactvd_zdb_id
;

!echo 'insert GenBank into temp_db_link'
insert into tmp_db_link
  select
    zactvd_zdb_id,
    'Genbank',
    gbacc_acc,
    '',
    get_id('DBLINK')
  from genbank_acc, ll_zdb, zdb_active_data
  where gbacc_ll = llzdb_ll_id
    and llzdb_zdb_id = zactvd_zdb_id
;


!echo 'insert ZF_LL into temp_db_link'
insert into tmp_db_link
  select
    zactvd_zdb_id,
    'LocusLink',
    llzdb_ll_id,
    'uncurrated ' || TODAY || ' LocusLink load',
    get_id('DBLINK')
  from ll_zdb, zdb_active_data
  where llzdb_zdb_id = zactvd_zdb_id
;

create index tmp_linked_recid_index on tmp_db_link
    (linked_recid) using btree;
create index tmp_acc_num_index on tmp_db_link
    (acc_num) using btree;


select linked_recid, count(linked_recid) 
from tmp_db_link
where db_name = "RefSeq"
group by linked_recid
having count(linked_recid) > 1
order by 1;



--GDB  GDB  GDB  GDB  GDB--
--Map genes with GDB orthologues to LocusLink. 

--Store necessary info from db_link/orthologue 
--to make GENE-LocusLink connection.
!echo 'create temp table GDB_ortho_link'
create temp table gdb_omim_tmp
  (
    lnkortho_zdb_id varchar(50),
    lnkortho_c_gene_id varchar(50),
    lnkortho_ortho_abbrev varchar(15),
    lnkortho_ortho_name varchar(120),
    lnkortho_acc_num varchar(50),
    lnkortho_link_zdb_id varchar(50)
  )
with no log;

!echo 'create temp table GDB_ortho_link'
create temp table gdb_ortho_link_tmp
  (
    lnkortho_zdb_id varchar(50),
    lnkortho_c_gene_id varchar(50),
    lnkortho_ortho_abbrev varchar(15),
    lnkortho_ortho_name varchar(120),
    lnkortho_acc_num varchar(50)
  )
with no log;

--select a distinct set of gdb/zfin ids
!echo 'insert into gdb_ortho_link_tmp'
insert into gdb_ortho_link_tmp
    select 
      distinct zdb_id,
      c_gene_id,
      ortho_abbrev,
      ortho_name,
      llgdb_ll_id
    from db_link, orthologue, ll_gdb
    where db_name = "GDB"
      and zdb_id = linked_recid
      and acc_num = llgdb_gdb_id;

!echo 'create temp table GDB_ortho_link'
create temp table gdb_ortho_link
  (
    lnkortho_zdb_id varchar(50),
    lnkortho_c_gene_id varchar(50),
    lnkortho_ortho_abbrev varchar(15),
    lnkortho_ortho_name varchar(120),
    lnkortho_acc_num varchar(50),
    dblink_zdb_id varchar(50)
  )
with no log;

--add ZDB ids
insert into gdb_ortho_link
    select *, get_id('DBLINK')
    from gdb_ortho_link_tmp;

--insert into gdb_omim_tmp
insert into gdb_omim_tmp
    select 
      distinct zdb_id,
      c_gene_id,
      ortho_abbrev,
      ortho_name,
      llgdb_omim_id,
      get_id('DBLINK')
    from db_link, orthologue, ll_gdb
    where db_name = "GDB"
      and zdb_id = linked_recid
      and acc_num = llgdb_gdb_id
      and llgdb_omim_id is not null;


create index gdb_lnkortho_zdb_id_index on gdb_ortho_link
    (lnkortho_zdb_id) using btree;


-- Ban OMIM disease links (Hard code for now 06-02-03)
delete from gdb_omim_tmp 
where lnkortho_acc_num in ('193500','106210','168461','300401','601868');


begin work;

create temp table dblinkid
  (
    link_id 	varchar(80)
  )
with no log;

!echo 'remove automated links'
insert into dblinkid
select dblink_zdb_id
       from db_link
       where dblink_zdb_id in (
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
;
create index link_id_index on dblinkid 
    (link_id) using btree in tempdbs1 ;

delete from zdb_active_data where zactvd_zdb_id in (select link_id from dblinkid);

!echo 'get all LocusLink and OMIM db_links that remain'
select * 
from db_link 
where db_name = "OMIM" 
   or db_name = "LocusLink"
into temp omim_and_ll
with no log;

!echo 'add LocusLink active data'
insert into zdb_active_data select dblink_zdb_id from gdb_ortho_link
       where lnkortho_acc_num not in (select acc_num from omim_and_ll where db_name = 'LocusLink');

!echo 'add OMIM active data'
insert into zdb_active_data select lnkortho_link_zdb_id from gdb_omim_tmp
       where lnkortho_acc_num not in (select acc_num from omim_and_ll where db_name = 'OMIM');


!echo 'insert LocusLink db_links for GDB orthos'
insert into db_link
        (linked_recid,
        db_name,
        acc_num,
        info,
        dblink_zdb_id,
        dblink_acc_num_display)
    select distinct
        lnkortho_zdb_id,
        "LocusLink",
        lnkortho_acc_num,
        '',
        dblink_zdb_id,
        'lnkortho_acc_num'
    from gdb_ortho_link
    where lnkortho_acc_num not in (select acc_num from omim_and_ll where db_name = 'LocusLink');

!echo 'insert OMIM db_links for GDB orthos'
insert into db_link
        (linked_recid,
        db_name,
        acc_num,
        info,
        dblink_zdb_id,
        dblink_acc_num_display)
    select distinct
        lnkortho_zdb_id,
        "OMIM",
        lnkortho_acc_num,
        '',
        lnkortho_link_zdb_id,
        lnkortho_acc_num
    from gdb_omim_tmp
    where lnkortho_acc_num not in (select acc_num from omim_and_ll where db_name = 'OMIM');

!echo 'Attribute human LL links to source LocusLink curation pub.'
insert into record_attribution
    select dblink_zdb_id, 'ZDB-PUB-020723-3'
    from db_link
    where db_name = "LocusLink"
      and acc_num not in (select acc_num from omim_and_ll);

!echo 'Attribute OMIM links to source LocusLink curation pub.'
insert into record_attribution
    select dblink_zdb_id, 'ZDB-PUB-020723-3'
    from db_link
    where db_name = "OMIM"
      and acc_num not in (select acc_num from omim_and_ll);
      


-- ------------------- END MGD ------------------- --



--MGI  MGI  MGI  MGI  MGI--
--Map genes with MGI orthologues to LocusLink. 

--Store necessary info from db_link/orthologue 
--to make GENE-LocusLink connection.
!echo 'create temp table MGI_ortho_link'
create temp table mgi_ortho_link
  (
    lnkortho_zdb_id varchar(50),
    lnkortho_c_gene_id varchar(50),
    lnkortho_ortho_abbrev varchar(15),
    lnkortho_ortho_name varchar(120),
    lnkortho_acc_num varchar(50),
    dblink_zdb_id varchar(50)
  )
with no log;

!echo 'insert into mgi_ortho_link'
insert into mgi_ortho_link
    select 
      zdb_id,
      c_gene_id,
      ortho_abbrev,
      ortho_name,
      llmgi_ll_id,
      get_id('DBLINK')
    from db_link, orthologue, ll_mgi
    where db_name = "MGI"
      and linked_recid = zdb_id
      and acc_num = llmgi_mgi_id;


create index mgi_lnkortho_zdb_id_index on mgi_ortho_link
    (lnkortho_zdb_id) using btree;


!echo 'get all LocusLink db_links that remain'
  select * 
  from db_link 
  where db_name = "LocusLink"
  into temp locuslink_link
  with no log;


!echo 'add active source and active data'
insert into zdb_active_data select dblink_zdb_id from mgi_ortho_link
       where lnkortho_acc_num not in (select acc_num from locuslink_link);


!echo 'insert LocusLink db_links for MGI orthos'
insert into db_link
        (linked_recid,
        db_name,
        acc_num,
        info,
        dblink_zdb_id,
        dblink_acc_num_display)
    select distinct
        lnkortho_zdb_id,
        "LocusLink",
        lnkortho_acc_num,
        'uncurrated ' || TODAY || ' LocusLink load',
        dblink_zdb_id,
        lnkortho_acc_num
    from mgi_ortho_link
    where lnkortho_acc_num not in (select acc_num from locuslink_link);

!echo 'Attribute mouse LL links to source LocusLink curation pub.'
insert into record_attribution
    select dblink_zdb_id, 'ZDB-PUB-020723-3'
    from db_link
    where db_name = "LocusLink"
      and acc_num not in (select acc_num from locuslink_link);

-- ----------------------  END MGI  ----------------------- --



-- ----------------------  DB_LINK  ------------------------ --

-- ------------------  add new links  ---------------------- --
!echo 'add active data'
insert into zdb_active_data select dblink_zdb_id from tmp_db_link where db_name = "RefSeq";
insert into zdb_active_data select dblink_zdb_id from tmp_db_link where db_name = "LocusLink" and acc_num not in (select acc_num from db_link where db_name = "LocusLink");
insert into zdb_active_data select dblink_zdb_id from tmp_db_link where db_name = "Genbank" and acc_num not in (select acc_num from db_link where db_name = "Genbank");
 
!echo 'insert new db_links'
insert into db_link
        (linked_recid,
        db_name,
        acc_num,
        info,
        dblink_zdb_id,
        dblink_acc_num_display) select *, 'acc_#' from tmp_db_link where db_name = "RefSeq";
insert into db_link
        (linked_recid,
        db_name,
        acc_num,
        info,
        dblink_zdb_id,
        dblink_acc_num_display) select *, 'acc_#' from tmp_db_link where db_name = "LocusLink" and acc_num not in (select acc_num from db_link where db_name = "LocusLink");
insert into db_link
        (linked_recid,
        db_name,
        acc_num,
        info,
        dblink_zdb_id,
        dblink_acc_num_display) select *, 'acc_#' from tmp_db_link where db_name = "Genbank" and acc_num not in (select acc_num from db_link where db_name = "Genbank");


!echo 'Attribute ZFIN_LL links to an artificial pub record.'
insert into record_attribution
    select a.dblink_zdb_id, 'ZDB-PUB-020723-3'
    from db_link a, tmp_db_link tmp
    where a.db_name in ("Genbank","LocusLink","RefSeq")
      and a.dblink_zdb_id = tmp.dblink_zdb_id
;


-- ------------------  UNI_GENE  ------------------- --
!echo 'remove existing temp_db_link records'
delete from tmp_db_link;

!echo 'insert into temp_db_link'
insert into tmp_db_link
  select
    llzdb_zdb_id,
    'UniGene',
    uni_cluster_id,
    'uncurrated ' || TODAY || ' LocusLink load',
    get_id('DBLINK')
  from uni_gene, ll_zdb, zdb_active_data
  where uni_ll_id = llzdb_ll_id
    and llzdb_zdb_id = zactvd_zdb_id
;

-- ------------------ add new records ------------------ --
!echo 'get all UniGene db_links that remain'
  select * 
  from db_link 
  where db_name = "UniGene"
  into temp unigene_link
  with no log;


!echo 'add active source and active data'
insert into zdb_active_data select dblink_zdb_id from tmp_db_link
       where acc_num not in (select acc_num from unigene_link);


!echo 'insert new db_links'
insert into db_link
        (linked_recid,
        db_name,
        acc_num,
        info,
        dblink_zdb_id,
        dblink_acc_num_display)
    select *, 'acc_$'
    from tmp_db_link
    where acc_num not in (select acc_num from unigene_link);


!echo 'Attribute RefSeq links to an artificial pub record.'
insert into record_attribution
    select a.dblink_zdb_id, 'ZDB-PUB-020723-3'
    from db_link a
    where a.db_name = "UniGene"
      and a.acc_num not in (select acc_num from unigene_link)
;

commit work;



-- load ensdarT as link to other transcript page
-- need a mapping from ensdarT to dbacc fetched from Ensembl (done)

begin work;

create table ensdarT_dbacc (
	ensacc_ensdarT varchar(20),
	ensacc_dbacc varchar(20)  primary key
)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 789585 next size 789585
; -- SWAG  extents needs adjusting, want tool

load from 'ensdarT_dbacc.unl' insert into ensdarT_dbacc;

-- note adding miRNA data makes both columns non-unique

create unique index ensdarT_dbacc_ensacc_ensdarT_idx
 on ensdarT_dbacc(ensacc_ensdarT) in idxdbs3;

update statistics medium for table ensdarT_dbacc;

! echo "drop existing ensdarT that are not continued"
delete from zdb_active_data where exists (
	select 't' from db_link
	 where dblink_fdbcont_zdb_id == 'ZDB-FDBCONT-110301-1'
	   and dblink_zdb_id == zactvd_zdb_id
	   and not exists (
	    	select 't' from ensdarT_dbacc
	    	 where dblink_acc_num == ensacc_ensdarT
	   )
);

! echo "check if any moved?"
select count(*) moved
 from db_link ens, db_link ott, ensdarT_dbacc
 where ens.dblink_fdbcont_zdb_id == 'ZDB-FDBCONT-110301-1'
   and ott.dblink_acc_num == ensacc_dbacc
   and ott.dblink_linked_recid == ens.dblink_linked_recid
   and ens.dblink_acc_num != ensacc_ensdart
;

! echo "drop new ensdarT that already exist"
delete from ensdarT_dbacc where exists (
	select 't' from db_link
	 where  dblink_fdbcont_zdb_id == 'ZDB-FDBCONT-110301-1'
	   and dblink_acc_num == ensacc_ensdarT
);


! echo "isolate transcript marker ZDB_ids  linked to ottdarTs"
select dblink_linked_recid tscript, ensacc_ensdarT acc, get_id('DBLINK') zad, dblink_length len
 from db_link , ensdarT_dbacc
 where dblink_acc_num == ensacc_dbacc
into temp tmp_dblink with no log;


insert into zdb_active_data select zad from tmp_dblink;

! echo "make db links to Ensembl with ensdarTs which are mapped to ottdarTs in ZFIN"
insert into db_link (
    dblink_linked_recid,
    dblink_acc_num,
    dblink_zdb_id,
    dblink_fdbcont_zdb_id,
    dblink_info,
    dblink_length
)
select  tscript, acc, zad, 'ZDB-FDBCONT-110301-1','uncurated ' || TODAY, len
 from tmp_dblink
;

! echo "attribute db links"

insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
) select  zad, 'ZDB-PUB-061101-1' from tmp_dblink -- ensembl pub
;

drop table tmp_dblink;
drop table ensdarT_dbacc;

-- applied externaly
--rollback work;
--commit work;


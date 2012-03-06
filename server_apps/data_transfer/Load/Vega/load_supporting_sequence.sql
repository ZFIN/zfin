
-- transcript supporting sequence from evidence file

begin work;

create table support_seq (ss_ottdarT varchar(20), ss_gbacc varchar(9))
fragment by round robin in tbldbs1,tbldbs2,tbldbs3
;

! echo "load file evidence.unl"
load from 'evidence.unl' insert into support_seq;

create index support_seq_ss_ottdarT_idx on support_seq(ss_ottdarT) in idxdbs2;
create index support_seq_ss_gbacc_idx on support_seq(ss_gbacc) in idxdbs3;

update statistics for table support_seq;

alter table support_seq add ss_tscript varchar(25);

-- data cleaning
select dblink_linked_recid,ss_gbacc, count(*) howmany
 from db_link join support_seq on dblink_acc_num == ss_ottdarT
 group by 1,2
 having count(*) >1
;

update support_seq set ss_tscript = (
	select distinct dblink_linked_recid from db_link 
	 where dblink_acc_num == ss_ottdarT
)where exists(
	select 't' from  db_link where dblink_acc_num == ss_ottdarT
);


! echo "do any ottdarT not have a tscript?"
select * from support_seq where ss_tscript is NULL;

! echo "find existing SS as well as we can"
select dblink_zdb_id, dblink_linked_recid, dblink_acc_num
 from db_link, record_attribution
 where dblink_linked_recid[1,12] == 'ZDB-TSCRIPT-'
   and dblink_acc_num[1,6] != 'OTTDAR'
   and dblink_acc_num[1,4] != 'ZFIN'
   and recattrib_data_zdb_id == dblink_zdb_id
   and recattrib_source_zdb_id == 'ZDB-PUB-030703-1'
  into temp tmp_ss with no log
;
! echo "filter all existing SS that are not known to be dropped"
delete from tmp_ss where exists(
	select 't' from support_seq
	 where ss_gbacc == dblink_acc_num
       and ss_tscript == dblink_linked_recid
);
! echo "drop SS that are not continued in the evidence file from Kerstin"
delete from zdb_active_data where exists (
    select 't' from tmp_ss where dblink_zdb_id == zactvd_zdb_id
);
drop table tmp_ss;

! echo "filter incomming SS that already exist"
delete from support_seq where exists (
    select 't' from db_link, record_attribution
 where dblink_linked_recid[1,12] == 'ZDB-TSCRIPT-'
   and dblink_acc_num[1,6] != 'OTTDAR'
   and dblink_acc_num[1,4] != 'ZFIN'
   and recattrib_data_zdb_id == dblink_zdb_id
   and recattrib_source_zdb_id == 'ZDB-PUB-030703-1'
   and ss_tscript == dblink_linked_recid
   and ss_gbacc == dblink_acc_num
);
update statistics for table support_seq;

! echo "drop the wz_est support"
delete from support_seq where ss_gbacc[1,2] == 'wz';

! echo "find gb accession length"
alter table support_seq add ss_len varchar(50);
update support_seq set ss_len = (
    select accbk_length from accession_bank
     where accbk_fdbcont_zdb_id == 'ZDB-FDBCONT-040412-37'
       and ss_gbacc == accbk_acc_num
);

! echo "did any not get a length?"
select count(*) from support_seq where ss_len is NULL;

! echo "they are most likely not zebrafish "
! echo " some are not at any rate so I am going to delete them"
delete from support_seq where ss_len is NULL;
update statistics for table support_seq;

! echo "new SS to be added"
select count(*) howmany from support_seq;
unload to 'novel_ss.unl' select * from support_seq;

! echo " make dblink zdbid"
alter table support_seq add ss_zad varchar(50);
update support_seq set ss_zad = get_id('DBLINK');

! echo "prime zdb active data with DBLINK"
insert into zdb_active_data select ss_zad from support_seq
 where ss_tscript is not NULL
;

! echo "load db_link"
insert into db_link(
	dblink_linked_recid,
	dblink_acc_num,
	dblink_info,
	dblink_zdb_id,
--	dblink_acc_num_display,
	dblink_length,
	dblink_fdbcont_zdb_id
) select
    ss_tscript,
    ss_gbacc,
    'uncurated supporting sequence ' || TODAY,
    ss_zad,
    --,
    ss_len,
    'ZDB-FDBCONT-040412-37'
from support_seq where ss_tscript is not NULL
;

! echo "attribute"
insert into record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id)
select ss_zad,'ZDB-PUB-030703-1' from support_seq where ss_tscript is not NULL
;

--
drop table support_seq;

! echo "transaction terminated externaly"

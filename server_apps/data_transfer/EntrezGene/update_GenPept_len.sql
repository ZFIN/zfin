-- update_GenPept_len.sql

begin work;

create table gp_len(
        gpl_acc varchar(16) not NULL PRIMARY KEY, -- great. multiple lengths for same accession
        gpl_len integer not NULL
) in tbldbs3;

load from 'genpept_len.unl' insert into gp_len;

{ the reduced set seems okey ...
select gpl_acc, max(gpl_len) ln from gp_len
 group by 1 into temp tmp_gpl with no log;
delete from gp_len;
insert into gp_len select * from tmp_gpl;
drop table tmp_gpl;
create unique index gp_len_gpl_acc_idx
 on gp_len(gpl_acc) using btree in idxdbs3;
}

update statistics high for table gp_len;

! echo "Update NULL GenPept length"
update db_link set dblink_length = (
        select  gpl_len  from gp_len where gpl_acc == dblink_acc_num
)
where dblink_length is NULL
 and dblink_fdbcont_zdb_id == "ZDB-FDBCONT-040412-42"
  and exists (
        select 't' from gp_len
         where gpl_acc == dblink_acc_num

);

! echo "Update changed GenPept length"
update db_link set dblink_length = (
        select  gpl_len  from gp_len where gpl_acc == dblink_acc_num
)
where dblink_length is not NULL
 and dblink_fdbcont_zdb_id == "ZDB-FDBCONT-040412-42" -- GenPept
  and exists (
        select 't' from gp_len
         where gpl_acc == dblink_acc_num
           and dblink_length != gpl_len
);


drop table gp_len;


-- transaction terminated externally

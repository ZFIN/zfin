begin work;

create table ens_zdb( ez_zdb varchar(50), ez_ens varchar(20));
load from 'ensdarg.unl' insert into ens_zdb;
create index ez_zdb_idx on ens_zdb( ez_zdb );
update statistics for table ens_zdb;

! echo "if a gene has been merged, fix it's zdbid"
update ens_zdb set ez_zdb = (
	select zrepld_new_zdb_id
	 from zdb_replaced_data
	 where ez_zdb = zrepld_old_zdb_id
)where ez_zdb in (
	select zrepld_old_zdb_id from zdb_replaced_data
);

! echo "confirm all zdbids still exists"
select * from ens_zdb where ez_zdb not in (select * from zdb_active_data);
delete from ens_zdb where ez_zdb not in (select * from zdb_active_data);

! echo "confirm zdb-ens pairs are unique"
select distinct * from  ens_zdb into temp tmp_ens_zdb;
delete from ens_zdb;
insert into ens_zdb select * from tmp_ens_zdb;
drop table tmp_ens_zdb;

update statistics for table ens_zdb;


! echo "delete existing links that are no longer represented by Ensembl 1:1"
delete from zdb_active_data where zactvd_zdb_id in (
	select dblink_zdb_id from db_link
	 where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-061018-1'
	   and not exists(
	 	select 1 from ens_zdb
	 	 where dblink_linked_recid = ez_zdb
	 	   and dblink_acc_num = ez_ens
	)
);

! echo "remove incomming Ensembl links that already exist in ZFIN"
delete from ens_zdb
 where exists(
 	select 1 from db_link
 	 where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-061018-1'
 	   and dblink_linked_recid = ez_zdb
 	   and dblink_acc_num = ez_ens
);

-- are there other conditions to test?
-- the round about senerios I come up with should be covered.

! echo "make new records for incomming Ensembl links"
alter table ens_zdb add ez_zad varchar(50);
update ens_zdb set ez_zad = get_id('DBLINK');

insert into zdb_active_data select ez_zad from ens_zdb;
insert into db_link(
	dblink_linked_recid,
	dblink_acc_num,
	dblink_info,
	dblink_zdb_id,
	dblink_acc_num_display,
	dblink_length,
	dblink_fdbcont_zdb_id
)
select
	ez_zdb,
	ez_ens,
	'uncurrated ' || TODAY,
	ez_zad,
	ez_ens,
	0,
	'ZDB-FDBCONT-061018-1'
 from ens_zdb
;

! echo "attribute links to fake pub"
insert into record_attribution (
	recattrib_data_zdb_id,
	recattrib_source_zdb_id,
	recattrib_source_type
) select ez_zad, 'ZDB-PUB-061101-1', 'standard' from ens_zdb
;

drop table ens_zdb;

--rollback work;

--
commit work;


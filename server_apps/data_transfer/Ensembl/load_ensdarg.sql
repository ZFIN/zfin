begin work;
-- TODO
-- ensdargs to more than one zdb to RENO (merge candidates)
-- zdb with *many* ensdarg not reliable
-- attribution
-- new ENSEMBL link
--

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

--! echo "if a gene has a Vega link do NOT add a Ensembl link"
--delete from ens_zdb where ez_zdb in
--	(select dblink_linked_recid from db_link
--	 where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-14'
--);


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

--
rollback work;

--commit work;


begin work;


create temp table ens_zdb( ez_zdb text, ez_ens varchar(20)) ;

--! echo "ensdarG.unl -> load_ensdarG.sql"

\copy from /opt/zfin/www_homes/swirl/server_apps/data_transfer/Ensembl/ensdarG.unl insert into ens_zdb;

create index ez_zdb_idx on ens_zdb( ez_zdb );

--delete the dups from the file itself.  The rest will be merges and will be ok to leave 2 ENSDARGs on.
create temp table tmp_ens_zdb (ez_zdb text, ez_ens varchar(20));

insert into tmp_ens_zdb
select distinct * from ens_zdb;

delete from ens_zdb;

insert into ens_zdb select * from tmp_ens_zdb;
drop table tmp_ens_zdb;


--! echo "if a gene has been merged, fix its zdbid"
update ens_zdb set ez_zdb = (
	select zrepld_new_zdb_id
	 from zdb_replaced_data
	 where ez_zdb = zrepld_old_zdb_id
)where ez_zdb in (
	select zrepld_old_zdb_id from zdb_replaced_data
);

--! echo "unload rows to confirm all zdbids still exists:  >0 is bad."
\copy (select * from ens_zdb where not exists (select 'x' from zdb_active_data where zactvd_zdb_id = ez_zdb)) to zdb_ids_not_in_zdb_active_data.txt


delete from ens_zdb where not exists (select 'x' from zdb_active_data where zactvd_zdb_id = ez_zdb);

create temp table tmp_ens_zdbDups (ez_zdb text, ez_ens varchar(20));

insert into tmp_ens_zdbDups (ez_zdb, ez_ens)
select ez_zdb, ez_ens
 from ens_zdb
union 
select dblink_linked_recid as ez_zdb, dblink_acc_num as ez_ens
  from db_link
 where dblink_Acc_num like 'ENSDARG%';


--! echo "unload rows to confirm zdb-ens pairs are unique within and between ENSDARG data sources."

\copy (select count(*), ez_zdb, ez_ens from tmp_ens_zdbDups group by ez_zdb, ez_ens having count(*) > 1 ) to zdb_ens_ids_not_unique.txt

create temp table tmp_dup_enz_ids (counter int, ens_id varchar(20));

insert into tmp_dup_enz_ids (counter, ens_id)
select count(*) as counter, ez_ens as ens_id
  from tmp_ens_zdbDups
group by ez_ens
having count(*) > 1;

create index dupEns_index on tmp_dup_enz_ids(ens_id);

create temp table tmp_reportDupEnsdarg (ez_ens varchar(20), ez_zdb text);

insert into tmp_reportDupEnsdarg (ez_ens, ez_zdb)
select ez_ens, ez_zdb
  from tmp_ens_zdbDups, tmp_dup_enz_ids
  where ez_ens = ens_id;

--! echo "unload rows to show ENSDARGs on more than one gene."

\copy (select * from tmp_reportDupEnsdarg order by ez_ens) to ensdargs_on_more_than_one_gene.txt

create temp table tmp_ens_zdb (ez_ens varchar(20), ez_zdb text);

insert into tmp_ens_zdb
select distinct * from ens_zdb;

insert into ens_zdb select * from tmp_ens_zdb;
drop table tmp_ens_zdb;

--! echo "confirm a zdb_id still only exists once. (we do merges ...)"
create temp table tmp_dup_zdb (id text);

insert into tmp_dup_zdb (id)
select ez_zdb as id from ens_zdb
 group by 1 having count(*) > 1;

\copy (select id, ez_ens from tmp_dup_zdb, ens_zdb where id = ez_zdb) to zdb_id_exists_more_than_once.txt


delete from ens_zdb
 where exists (Select 'x' from tmp_dup_zdb where id = ens_zdb.ez_zdb)
;
drop table tmp_dup_zdb;

--!echo "SIERRA" ;

--!echo "delete existing links that are no longer represented by Ensembl 1:1" ;


create temp table tmp_drop as
select dblink_zdb_id as id
 from db_link
 where not exists(
	 	select 'x' from ens_zdb
	 	 where dblink_linked_recid = ez_zdb
	 	   and dblink_acc_num = ez_ens
)
and dblink_acc_num like 'ENSDARG%'
 and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-061018-1'
 and dblink_linked_recid like 'ZDB-GENE%'
;

--!echo "unloading ENSDARGs that have changed but can not be updated because they are associated with Sanger Load Pub";

\copy (select id, dblink_acc_num, dblink_linked_recid from tmp_drop, record_Attribution, db_link where recattrib_data_zdb_id = id and recattrib_source_zdb_id in ('ZDB-PUB-120207-1','ZDB-PUB-130213-1','ZDB-PUB-130211-1','ZDB-PUB-130725-1','ZDB-PUB-130425-4', 'ZDB-PUB-100504-26', 'ZDB-PUB-100504-23', 'ZDB-PUB-100504-24','ZDB-PUB-070315-1','ZDB-PUB-130211-1','ZDB-PUB-120207-1','ZDB-PUB-130213-1') and dblink_zdb_id = id and not exists (Select 'x' from ensdar_mapping where ensm_ensdarg_id = dblink_acc_num) to changedSangerEnsdargs.txt;

--!echo "delete from tmp_drop"

delete from tmp_drop
 where exists (select 'x' from record_attribution
       	       where recattrib_data_zdb_id = id
		 and recattrib_source_zdb_id in ('ZDB-PUB-120207-1', 'ZDB-PUB-130213-1', 'ZDB-PUB-130211-1',
                                                 'ZDB-PUB-130725-1', 'ZDB-PUB-130425-4', 'ZDB-PUB-100504-26', 
                                                 'ZDB-PUB-100504-23', 'ZDB-PUB-100504-24','ZDB-PUB-070315-1',
                                                 'ZDB-PUB-130211-1','ZDB-PUB-120207-1','ZDB-PUB-130213-1'));

--!echo "select from tmp_drop"

select * From tmp_drop
 where id = 'ZDB-DBLINK-120813-30916';

delete from db_link
 where exists (Select 'x' from tmp_drop where dblink_zdb_id = id);


delete from zdb_active_data 
  where exists (select 'x' from tmp_drop where zactvd_zdb_id = id);

select * from db_link
 where dblink_zdb_id = 'ZDB-DBLINK-120813-30916';

select * from ens_zdb
 where ez_ens = 'ENSDARG00000094606';

select * from db_link
 where dblink_acc_num = 'ENSDARG00000094606';

--! echo "remove incomming Ensembl links that already exist in ZFIN"
delete from ens_zdb
 where exists(
 	select 1 from db_link
 	 where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-061018-1'
 	   and dblink_linked_recid = ez_zdb
 	   and dblink_acc_num = ez_ens
);

-- are there other conditions to test?
-- the round about senerios I come up with should be covered.

--! echo "make new records for incomming Ensembl links"

create temp table ens_zdb1 ( ez_zdb varchar(50), ez_ens varchar(20), ez_zad varchar(50));

insert into ens_zdb1 (ez_zdb, ez_ens, ez_zad)
 select ez_zdb,ez_ens,get_id('DBLINK')
   from ens_zdb;

create index ez_zdb1_idx on ens_zdb1( ez_zdb );

insert into zdb_active_data 
       select ez_zad from ens_zdb1;

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
	'uncurrated ' || NOW(),
	ez_zad,
	ez_ens,
	0,
	'ZDB-FDBCONT-061018-1'
 from ens_zdb1
;

--! echo "attribute links to fake pub"
insert into record_attribution (
	recattrib_data_zdb_id,
	recattrib_source_zdb_id,
	recattrib_source_type
) select ez_zad, 'ZDB-PUB-061101-1', 'standard' from ens_zdb1
;

--select count(*) from ens_zdb;

-- commit or rollback is appended externally
--rollback work;commit work;


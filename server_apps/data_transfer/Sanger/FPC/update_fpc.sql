set pdqpriority high;

! echo "load marker - name"
create table fpc_marker(fpm_mrkr varchar(25),fpm_acc varchar(25))
fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3
;
load from 'fpc_zdb.unl' insert into fpc_marker;
update fpc_marker set fpm_mrkr = lower(fpm_mrkr) where fpm_mrkr[1,4] <> 'ZDB-';
create index fpm_acc_ndx on fpc_marker(fpm_acc) in idxdbs1;
create index fpm_mrkr_ndx on fpc_marker(fpm_mrkr)in idxdbs3;
update statistics medium for table fpc_marker;
---------------------------------------------------
! echo "load clone - marker"
create table fpc_clone (fpcl_cln varchar(20), fpcl_acc varchar(20), fpcl_junk integer)
fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3
;
load from 'fpc_clone.unl' insert into fpc_clone;
create index fpcl_cln_ndx on fpc_clone(fpcl_cln) in idxdbs1;
create index fpcl_acc_ndx on fpc_clone(fpcl_acc)in idxdbs2;
alter table fpc_clone drop fpcl_junk;
update statistics medium for table fpc_clone;
---------------------------------------------------
! echo "load contig - clone"
create table fpc_contig(fpct_ctg varchar(20), fpct_cln varchar(20), fpct_junk varchar(20))
fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3
extent size 4096 next size 4096
;
load from 'fpc_contig.unl' insert into fpc_contig;
create index fpct_ctg_ndx on fpc_contig(fpct_ctg)in idxdbs3;
create index fpct_cln_ndx on fpc_contig(fpct_cln)in idxdbs2;
alter table fpc_contig drop fpct_junk;
update statistics medium for table fpc_contig;
---------------------------------------------------
! echo "translate mrkr_abbrevs to zdbids"
update fpc_marker set fpm_mrkr = (
	select mrkr_zdb_id
	from marker
	where fpm_mrkr = mrkr_abbrev
)where fpm_mrkr in (select mrkr_abbrev from marker)
;
! echo "update any replaced zdb_ids"
update fpc_marker set fpm_mrkr = (
	select zrepld_new_zdb_id
	from zdb_replaced_data
	where fpm_mrkr = zrepld_old_zdb_id
)where fpm_mrkr in (select zrepld_old_zdb_id from zdb_replaced_data)
;
---------------------------------------------------
! echo "associate etids with zdbs via gb_acc"
select distinct fpm_mrkr gb, fpm_acc acc, dblink_linked_recid zdb
from fpc_marker, db_link
where dblink_fdbcont_zdb_id ==  'ZDB-FDBCONT-040412-37' --'GenBank'
and upper(fpm_mrkr)  ==  dblink_acc_num
and fpm_mrkr[1] not in ('Z','1','2','4','5','6','7','8','9','z','s')
order by 2,3,1
into temp tmp_maz with no log;
---------------------------------------------------
! echo "cull out the redundant gb_acc"
delete from tmp_maz where exists (
	select 1 from fpc_marker where fpm_mrkr = zdb and fpm_acc = acc
);
! echo "turn tables and cull back"
delete from fpc_marker where exists (
	select 1 from  tmp_maz where fpm_mrkr = gb and fpm_acc = acc
);

! echo "add the rows back in with interest"
insert into fpc_marker select zdb,acc from tmp_maz;

drop table tmp_maz;
--------------------------------------------------
! echo "translate any zdb which have been replaced"
update fpc_marker set fpm_mrkr = (
	select zrepld_new_zdb_id
	from zdb_replaced_data
	where fpm_mrkr = zrepld_old_zdb_id
)where fpm_mrkr in (
	select zrepld_old_zdb_id from zdb_replaced_data
);

---------------------------------------------------
! echo "Find the unique set"
select distinct * from fpc_marker into temp tmp_fpm_mrkr with no log;
delete from fpc_marker;
insert into fpc_marker select * from  tmp_fpm_mrkr;
drop table tmp_fpm_mrkr;

---------------------------------------------------
---------------------------------------------------
! echo "***********************************************************"
! echo ""
begin work;

---------------------------------------------------
---------------------------------------------------

!echo "one time clean up after splitting BAC/PAC_ENDs"
delete from zdb_active_data where zactvd_zdb_id in (
    select dalias_zdb_id
     from data_alias,record_attribution
     where recattrib_data_zdb_id = dalias_zdb_id
     and  dalias_data_zdb_id[1,12] in ('ZDB-BAC_END-','ZDB-PAC_END-')
     and  recattrib_source_zdb_id =  'ZDB-PUB-030703-3'  -- clone
);

delete from zdb_active_data where zactvd_zdb_id in (
    select dblink_zdb_id from db_link
     where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-10'
     and dblink_linked_recid[1,12] in ('ZDB-BAC_END-','ZDB-PAC_END-')
);

!echo "clean out existing data alias if they have not been renewed"

select dalias_data_zdb_id zad, recattrib_data_zdb_id[1,11]
from record_attribution, data_alias
where --recattrib_data_zdb_id[1,11] = 'ZDB-DALIAS-' and
       recattrib_source_zdb_id == 'ZDB-PUB-030703-2' -- fpc
and    recattrib_data_zdb_id   == dalias_data_zdb_id
and not exists (
	select 1 from fpc_marker
	where dalias_data_zdb_id = fpm_mrkr
	and   dalias_alias = fpm_acc
)
into temp tmp_dropped_fpc with no log
;
-- should cascade out to record_attribution & data_alias
delete from zdb_active_data
where zactvd_zdb_id in (select zad from tmp_dropped_fpc)
;
drop table tmp_dropped_fpc;

! echo "create data alias from sanger_name to zfin_name"
select  get_id('DALIAS') zad ,fpm_mrkr, fpm_acc, 'alias' datype, lower(fpm_mrkr) lower
	from fpc_marker
	where fpm_mrkr[1,4] == 'ZDB-'
	and not exists (
		select 1 from data_alias
		where dalias_data_zdb_id = fpm_mrkr
		and   dalias_alias = fpm_acc
	)
	group by fpm_acc,fpm_mrkr
	into temp tmp_dalias with no log;

! echo "make sure all marker zdb_ids are currently valid"
select * from tmp_dalias where fpm_mrkr not in (select * from zdb_active_data);
--delete   from tmp_dalias where fpm_mrkr not in (select * from zdb_active_data);


--select count(*)fpc_marker from tmp_dalias;
insert into zdb_active_data select zad from tmp_dalias;
insert into data_alias      select *   from tmp_dalias;

! echo "create record attribution for alias"
insert into record_attribution(recattrib_data_zdb_id,recattrib_source_zdb_id)
	select zad,'ZDB-PUB-030703-2'  from tmp_dalias; -- fpc

-----------------------------------------------------------------------
!echo "---------------------------------------------------------------"

! echo "build up all potential FPC links in this load"
select distinct
	fpm_mrkr,
	'ZDB-FDBCONT-040412-10' db_name,
	fpm_acc acc,
	"Sanger " || TODAY comments,
	'12345678901234567890123456789012345'::varchar(30) zad

	from fpc_marker, fpc_clone, fpc_contig
	where fpct_cln = fpcl_cln
	and   fpm_acc  = fpcl_acc
	and   fpm_mrkr[1,4] = 'ZDB-'
	into temp tmp_dblk with no log
;
create index tdblkaidx on tmp_dblk(acc);
create index tdblkmidx on tmp_dblk(fpm_mrkr);
--- this could use to be sped up
! echo "delete db-links that have not been renewed"
delete from zdb_active_data -- should cascade out to db_link and... ???
where zactvd_zdb_id in (
	select dblink_zdb_id
	from db_link o
	where o.dblink_fdbcont_zdb_id  = 'ZDB-FDBCONT-040412-10'
	and not exists (
		select 1 from tmp_dblk tdl
		where tdl.fpm_mrkr = o.dblink_linked_recid
		and   tdl.acc  = dblink_acc_num
	)
);

! echo "delete incomming links that are allready exist"
delete from tmp_dblk
where exists (
	select 1
	from db_link o
	where fpm_mrkr = o.dblink_linked_recid
	and  acc  = o.dblink_acc_num
	and o.dblink_fdbcont_zdb_id  = 'ZDB-FDBCONT-040412-10'
)
;

select distinct * from tmp_dblk into temp tmp_dblk_u with no log;
drop table tmp_dblk;

select  count(*) all_rows from tmp_dblk_u;
--select distinct fpm_mrkr,acc from tmp_dblk_u;


! echo "create db link to Sanger contig viewer"

update tmp_dblk_u set zad = get_id('DBLINK');

insert into zdb_active_data select zad from tmp_dblk_u;


insert into db_link(dblink_linked_recid, dblink_fdbcont_zdb_id, dblink_acc_num,dblink_info, dblink_zdb_id)
    select * from tmp_dblk_u
    ;

! echo "create record attribution for link"
insert into record_attribution(recattrib_data_zdb_id,recattrib_source_zdb_id)
	select zad,'ZDB-PUB-030703-2' from tmp_dblk_u; -- fpc
-------------------------------------------------------------------------------

-- should we also associate a marker with a Sanger BAC (clone) if the FPC indicates it?
-- what about when associated with a clone we do not have in ZFIN?
-- should we also find the FPC contigs a marker is in?
{

select distinct fpct_ctg,linked_recid
from db_link, fpc_contig, fpc_clone
where db_name = 'Sanger_FPC'
and fpct_cln = fpcl_cln
and acc_num = fpcl_acc

==>  5537 rows linking marker to sanger FPC contig
but same info is 1 click away by clicking on marker's Sanger_FPS link

}
drop table tmp_dblk_u;


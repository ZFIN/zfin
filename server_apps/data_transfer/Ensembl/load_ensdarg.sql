begin work;

create table ensdarg (darg_zdb varchar(50), darg_ens varchar(50));
load from 'ensdarg.unl' insert into ensdarg;

! echo "find & remove any zdb_ids from ensemble which have been SPLIT in ZFIN"
select * from ensdarg where exists (
    select 1
    from zdb_replaced_data a,zdb_replaced_data b
    where darg_zdb = a.zrepld_old_zdb_id
    and   darg_zdb = b.zrepld_old_zdb_id
    and  a.zrepld_new_zdb_id <> b.zrepld_new_zdb_id
);

delete from ensdarg where exists (
    select 1
    from zdb_replaced_data a,zdb_replaced_data b
    where darg_zdb = a.zrepld_old_zdb_id
    and   darg_zdb = b.zrepld_old_zdb_id
    and  a.zrepld_new_zdb_id <> b.zrepld_new_zdb_id
);

! echo "update any zdb_ids from ensemble which have been REPLACED in ZFIN"
update ensdarg set darg_zdb = replaced_zdb(darg_zdb)
where darg_zdb in(select zrepld_old_zdb_id from zdb_replaced_data)
;

! echo "sanity check - do the remaining zdb's still exist in zfin"
select * from ensdarg where darg_zdb not in (
    select mrkr_zdb_id from marker where mrkr_type = 'GENE'
);

delete from ensdarg where darg_zdb not in (
    select mrkr_zdb_id from marker where mrkr_type = 'GENE'
);

! echo "drop OLD ENSEMBL regord with automated attribution"
delete from zdb_active_data where zactvd_zdb_id in (
	select dblink_zdb_id from db_link, record_attribution  
	where db_name = 'ENSEMBL'
	and   dblink_zdb_id = recattrib_data_zdb_id
	and   recattrib_source_zdb_id = 'ZDB-PUB-030703-1' -- Curation of VEGA Database Links
);
! echo "drop NEW ENSEMBL records with non-automated attribution."
delete from ensdarg where exists (
	select 1 from db_link 
	where db_name = 'ENSEMBL'
	and linked_recid = darg_zdb
	and acc_num = darg_ens
);

! echo "Add current ENSEMBL links and automated attribution"
select distinct *, '123456789012345678901234567890'::varchar(50) zad 
from ensdarg into temp tmp_dblink with no log;

update tmp_dblink set zad = get_id('DBLINK');
insert into zdb_active_data select zad from tmp_dblink;
insert into record_attribution select zad, 'ZDB-PUB-030703-1' from tmp_dblink;
insert into db_link (linked_recid,db_name,acc_num,info,dblink_zdb_id,dblink_acc_num_display)
select darg_zdb,'ENSEMBL',darg_ens,'uncurated ' || TODAY, zad, darg_ens
from tmp_dblink;
drop table tmp_dblink;
drop table ensdarg;


--rollback work;
commit work;

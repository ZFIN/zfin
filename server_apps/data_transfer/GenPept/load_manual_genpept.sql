
! echo "First make sure all the manualy curated GenPepts are in."

begin work;
create temp table manual_genpept (gene varchar(50), genpept varchar(20)) with no log;
load from 'manual_curation.genpept' insert into manual_genpept;

update manual_genpept set gene = (
	select mrkr_zdb_id from marker where mrkr_abbrev = gene
)
where gene in (select mrkr_abbrev from marker where mrkr_type = 'GENE')
;
!echo "drop db_links without manual curation that will conflict with these"
delete from zdb_active_data
where zactvd_zdb_id in (
    select dblink_zdb_id
    from db_link, manual_genpept,record_attribution
    where db_name = 'GenPept'
    and   gene = linked_recid
    and recattrib_source_zdb_id <> 'ZDB-PUB-020723-5'
    and recattrib_data_zdb_id = dblink_zdb_id
    and   genpept = acc_num
) and zactvd_zdb_id in (
    select dblink_zdb_id
    from db_link,record_attribution
    where db_name = 'GenPept'
    and recattrib_source_zdb_id <> 'ZDB-PUB-020723-5'
    and recattrib_data_zdb_id = dblink_zdb_id
);

! echo "drop from incomming the ones that are already in with manual attribution"
delete from manual_genpept
where exists (
	select 1
	from db_link ,record_attribution
	where db_name = 'GenPept'
	and   gene = linked_recid
	and recattrib_source_zdb_id = 'ZDB-PUB-020723-5' --Scientific Curation
	and recattrib_data_zdb_id = dblink_zdb_id
	and   genpept = acc_num
) and gene in (
	select linked_recid
	from db_link
	where db_name = 'GenPept'
);

! echo "did any symbols fail to translate to ZDB IDs? indicating merge or nomenclature activity"
select * from manual_genpept where gene[1,9] <> 'ZDB-GENE-';

! echo "insert new manualy curated GenPept links"
select distinct gene, genpept, '123456789012345678901234567890' zad
from manual_genpept
where gene[1,9] == 'ZDB-GENE-'
into temp tmp_db_link with no log;

update tmp_db_link set zad = get_id('DBLINK');

!echo "sanity check"
select * from tmp_db_link where zad[1,11] <> 'ZDB-DBLINK-';
select zad,count(*) from tmp_db_link group by 1 having count(*) > 1;

insert into zdb_active_data select zad from tmp_db_link;

insert into db_link(
	linked_recid,
	db_name,
	acc_num,
	info,
	dblink_zdb_id,
	dblink_acc_num_display,
	dblink_organism,
	dblink_data_type,
	dblink_length
) select  distinct
	gene,
	'GenPept',
	genpept,'curated ' ||TODAY info,
	zad,
	genpept,
	'Zebrafish',
	'protein sequence',
	0
	from tmp_db_link
;

insert into record_attribution
       (recattrib_data_zdb_id, recattrib_source_zdb_id)
select zad, 'ZDB-PUB-020723-5'
from tmp_db_link;

drop table tmp_db_link;
drop table manual_genpept;

-- rollback work;

--
commit work;

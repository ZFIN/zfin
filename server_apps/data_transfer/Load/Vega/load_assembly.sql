-- load assembly
-- expects assembly for tom
-- and annotated clone files
! echo "$HOST $DBNAME `date +%Y%m%d`  `whoami`"

begin work;

create table assembly (
	asmb_lg varchar(3),
	five integer,
	three integer,
	asmb_name varchar(40),
	asmb_accession varchar(50),
	asmb_acc_vers integer,
	junk2 integer,
	asmb_length integer,
	polarity integer
) fragment by round robin in tbldbs1, tbldbs2, tbldbs3;

create table tmp_acc_clone(
	tac_accession varchar(20),
	tac_name varchar(40)
) fragment by round robin in tbldbs1, tbldbs2, tbldbs3
;
-- separate the clone acc from it's version # first (with tab)
--!  tr \. '\11' < assembly_for_tom.txt >! assembly_for_tom.tab

! echo "load file 'assembly_for_tom.tab' into a table"
load from 'assembly_for_tom.tab' delimiter '	'
 insert into assembly;

-- separate the clone acc from it's version # first
-- tr \. '\11' < clonelist_for_tom.txt > ! clonelist_for_tom.tab
-- cut -f 1-3 clonelist_for_tom.tab | nawk '{print $2"|"$3"|"$1"|"}' >! annotated_clones.unl

load from 'annotated_clones.unl'
 insert into tmp_acc_clone
;

-- clones in ZFIN are uppercase in name and abbrev
update assembly set asmb_name = upper(asmb_name);
update tmp_acc_clone set tac_name = upper(tac_name);

create index asmb_accession_idx on assembly(asmb_accession);
create index asmb_name_idx      on assembly(asmb_name);
create index tac_accession_idx  on tmp_acc_clone(tac_accession);
create index tac_name_idx  on tmp_acc_clone(tac_name);

! echo "Are annotated clones not in assembly?"
select tac_name,tac_accession from tmp_acc_clone
 where tac_accession not in (
	select asmb_accession from assembly
);

! echo "Are clones in assembly missing names?"
select count(asmb_accession) from assembly
 where asmb_name is NULL OR asmb_name = ""
;

drop table tmp_acc_clone;

! echo "Have existing clones have changed accessions?"
select --count(*) diff_acc
 asmb_name[1,20], dblink_acc_num[1,12] old ,asmb_accession[1,12] new--,asmb_accession[9,12]
 from assembly, db_link, marker
 where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-36' --gb genomic
 and mrkr_type in ('BAC','PAC','FOSMID')
 and mrkr_zdb_id = dblink_linked_recid
 and mrkr_abbrev = asmb_name
 and dblink_acc_num != asmb_accession
;
! echo "Are existing clone missing accession?"
select --count(*) miss_acc
       asmb_name[1,20], mrkr_zdb_id[1,25],asmb_accession[1,8]
 from assembly,marker
 where mrkr_type in ('BAC','PAC','FOSMID')
 and mrkr_abbrev = asmb_name
 and not exists(
    select 't' from db_link
     where mrkr_zdb_id = dblink_linked_recid
     and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-36'
     and dblink_acc_num = asmb_accession
 )
 order by 2,1
;

! echo "_ALL_ loaded Vega clones not in current Assembly"
{this currently is an ever increasing number
 tho being tied to Ensembl has slowed it down
 the idea is we keep the attribution because we did get it from them
 and it keep whatever linkage info it had when they dropped it.

 to know if a clone is part of the current assembly look in db_link
 for links to vega 
}
select count(*) all_dropped_clones
      --mrkr_abbrev[1,20], mrkr_zdb_id[1,25]
 from marker, record_attribution
 where mrkr_type in ('BAC','PAC','FOSMID')
   and mrkr_zdb_id = recattrib_data_zdb_id
   and recattrib_source_zdb_id == 'ZDB-PUB-030703-1'
   and not exists(
    select 't' from assembly where mrkr_abbrev = asmb_name
);


! echo "Which existing vega clones JUST left the Assembly?"
select mrkr_abbrev[1,20], mrkr_zdb_id[1,25]
 from marker, db_link
 where mrkr_type in ('BAC','PAC','FOSMID')
   and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040826-2'
   and mrkr_zdb_id = dblink_linked_recid
 and not exists(
    select 1 from assembly where mrkr_abbrev = asmb_name
);


! echo "Drop Vega_Clone Links that are not in this Assembly"
delete from zdb_active_data where zactvd_zdb_id in (
	select dblink_zdb_id
	 from marker, db_link
	 where mrkr_type in ('BAC','PAC','FOSMID')
	   and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040826-2'
	   and mrkr_zdb_id = dblink_linked_recid
	   and not exists(
		select 1 from assembly where mrkr_abbrev = asmb_name
	)
);

--############################################################################
-- this section may not need to be repeated

! echo "Restore vega_clone links for existing bacs in both zfin and assembly"
! echo "They were removed for a while when Sanger decided they were haplotype"

create table  tmp_vc_lnk ( tvl_lnk_recid varchar(50), tvl_acc_num varchar(50));

insert into tmp_vc_lnk
select distinct gb.dblink_linked_recid,gb.dblink_acc_num
 from  db_link gb,assembly
 where gb.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-36'
   and gb.dblink_linked_recid[1,8] in ('ZDB-BAC-','ZDB-PAC-','ZDB-FOSM')
   and gb.dblink_acc_num = asmb_accession
   and  not exists(
    select 1
     from db_link v
     where gb.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040826-2'
       and gb.dblink_acc_num = v.dblink_acc_num
 )
 and gb.dblink_acc_num not in ('BX548249','CR933767') -- bad data?
;


delete from  tmp_vc_lnk
 where exists (
    select 1 from db_link
     where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040826-2'
       and dblink_linked_recid = tvl_lnk_recid
       and dblink_acc_num = tvl_acc_num
);

--select tvl_lnk_recid,count(*) howmany from tmp_vc_lnk group by 1 having count(*) > 1;
--select tvl_acc_num,  count(*) howmany from tmp_vc_lnk group by 1 having count(*) > 1;


alter table tmp_vc_lnk add tvl_zad varchar(50);
update tmp_vc_lnk set tvl_zad = get_id('DBLINK');


insert into zdb_active_data
 select tvl_zad from tmp_vc_lnk
;


--set triggers for db_link disabled;
insert into db_link (
    dblink_linked_recid,
    dblink_acc_num,
    dblink_fdbcont_zdb_id,
    dblink_info,
    --dblink_length,
    --dblink_acc_num_display,
    dblink_zdb_id
) select
    tvl_lnk_recid,
    tvl_acc_num,
    'ZDB-FDBCONT-040826-2',
    'uncurated ' || TODAY,
    --asmb_length,
    --asmb_accession[1,8],
    tvl_zad
 from tmp_vc_lnk
;


--set triggers for db_link enabled;
! echo "Attribute new bac/pac Vega_Clone links to Vega."

insert into record_attribution(recattrib_data_zdb_id,recattrib_source_zdb_id)
 select tvl_zad, 'ZDB-PUB-030703-1' from tmp_vc_lnk
 ;

drop table tmp_vc_lnk;

--############################################################################

! echo "Do existing vega clones that are not in current assembly have genes?"
select count(*) drp_cln_gene
	--gene.mrkr_abbrev[1,20], gene.mrkr_zdb_id[1,25]
 from marker clone, record_attribution , marker gene, marker_relationship
 where clone.mrkr_type in ('BAC','PAC','FOSMID')
   and mrel_type = 'clone contains gene'
   and clone.mrkr_zdb_id = recattrib_data_zdb_id
   and recattrib_source_zdb_id = 'ZDB-PUB-030703-1'
   and clone.mrkr_zdb_id = mrel_mrkr_1_zdb_id
   and gene.mrkr_zdb_id = mrel_mrkr_2_zdb_id
 and not exists(
    select 1 from assembly where clone.mrkr_abbrev = asmb_name
)
;

-- need to add check for existing clones using same accession but with diferent name

! echo "Do existing clones have NEW NAMES in current assembly?"
select mrkr_abbrev[1,20]old_name, mrkr_zdb_id[1,25], asmb_name[1,20] new_name,asmb_accession acc
 from marker,db_link,assembly
 where mrkr_type in ('BAC','PAC','FOSMID')
 and mrkr_zdb_id = dblink_linked_recid
 and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040826-2'
 and dblink_acc_num == asmb_accession
 and mrkr_abbrev != asmb_name
;


---------------------------------------------------------------------
---##################################################################
---------------------------------------------------------------------
-- embl attributions are historical artifacts and will hopefully go away
! echo "Any Embl attribution with Vega attribution ...?"
select a.recattrib_source_zdb_id dup_src, a.recattrib_data_zdb_id dup_data
 from record_attribution a, record_attribution b
 where a.recattrib_source_zdb_id = 'ZDB-PUB-020822-1'
 and   b.recattrib_source_zdb_id = 'ZDB-PUB-030703-1'
 and   a.recattrib_data_zdb_id = b.recattrib_data_zdb_id
 into temp tmp_dup_recatt with no log
;
! echo "Delete Embl coexisting with Vega"
delete from record_attribution where exists(
    select 1 from tmp_dup_recatt
     where  dup_src  = recattrib_source_zdb_id
     and    dup_data = recattrib_data_zdb_id
);
drop table tmp_dup_recatt;

! echo "... update in db_link via clones in assembly"
update record_attribution set recattrib_source_zdb_id = 'ZDB-PUB-030703-1'
where recattrib_source_zdb_id = 'ZDB-PUB-020822-1'
and exists (
    select 1 from assembly,marker,db_link
     where mrkr_type in ('BAC','PAC','FOSMID')
     and mrkr_abbrev = asmb_name
     and mrkr_zdb_id = dblink_linked_recid
     and recattrib_data_zdb_id = dblink_zdb_id
);


! echo "... update in marker via ottdargs in db_link"
update record_attribution set recattrib_source_zdb_id = 'ZDB-PUB-030703-1'
where recattrib_source_zdb_id = 'ZDB-PUB-020822-1'
and exists (
    select 1 from db_link
      where dblink_acc_num[1,8] = 'OTTDARG0'
      and recattrib_data_zdb_id = dblink_linked_recid
);

! echo " ... update in marker via clones in assembly"
update record_attribution set recattrib_source_zdb_id = 'ZDB-PUB-030703-1'
where recattrib_source_zdb_id = 'ZDB-PUB-020822-1'
and exists (
    select 1 from assembly,marker
       where mrkr_type in ('BAC','PAC','FOSMID')
        and mrkr_abbrev = asmb_name
        and recattrib_data_zdb_id = mrkr_zdb_id
);

! echo "... update in linkage via clones in assembly"
update record_attribution set recattrib_source_zdb_id = 'ZDB-PUB-030703-1'
where recattrib_source_zdb_id = 'ZDB-PUB-020822-1'
and exists (
    select 1 from assembly,linkage_member,marker
       where mrkr_type in ('BAC','PAC','FOSMID')
        and mrkr_abbrev = asmb_name
        and mrkr_zdb_id = lnkgmem_member_zdb_id
        and recattrib_data_zdb_id = lnkgmem_linkage_zdb_id
);

-------------------------------------------------------------------------------
! echo "Which clones have changed LG's? (skip to & from  AB & unknown)"

-- select lnkg_or_lg oldlg, asmb_lg newlg, asmb_name[1,20] clone ,----------------------------------

drop table assembly;


! echo "transaction terminated externaly"
--rollback work;
--commit work;

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
 and it keepw whatever linkage info it had when they dropped it.

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

{ haploid clones are now back in vega and can be linked to again
! echo "Drop Vega_Clone links that are Haploid in this Assembly"
delete from zdb_active_data where zactvd_zdb_id in (
	select dblink_zdb_id
	 from marker, db_link
	 where mrkr_type in ('BAC','PAC')
	   and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040826-2'
	   and mrkr_zdb_id = dblink_linked_recid
	   and exists(
		select 1 from assembly
		 where mrkr_abbrev = asmb_name
		 and asmb_lg = 'H'
	)
);
}
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

{
select distinct recattrib_data_zdb_id
 from record_attribution,db_link
 where recattrib_source_zdb_id = 'ZDB-PUB-020822-1'
 and   recattrib_data_zdb_id = dblink_linked_recid
 and   dblink_acc_num[1,8] = 'OTTDARG0'
;
}
-------------------------------------------------------------------------------
! echo "Which clones have changed LG's? (skip to & from  AB & unknown)"

select lnkg_or_lg oldlg, asmb_lg newlg, asmb_name[1,20] clone ,asmb_accession[1,20] acc
 from assembly,linkage,linkage_member,marker,record_attribution
 where mrkr_type in ('BAC','PAC','FOSMID')
 and mrkr_zdb_id = lnkgmem_member_zdb_id
 and lnkgmem_linkage_zdb_id = lnkg_zdb_id
 and recattrib_data_zdb_id = lnkg_zdb_id
 and recattrib_source_zdb_id in ('ZDB-PUB-030703-1','ZDB-PUB-020822-1') -- vega,embl
 and mrkr_abbrev = asmb_name
 and asmb_lg not in ('AB')
 and lnkg_or_lg != case when asmb_lg = 'U' then '0' else asmb_lg end
;

! echo "Change LG to match current assembly."
update linkage set lnkg_or_lg = (
    select case when asmb_lg = 'U' then '0' else asmb_lg end
     from assembly,marker,linkage_member,record_attribution
     where mrkr_type in ('BAC','PAC','FOSMID')
     and mrkr_zdb_id = lnkgmem_member_zdb_id
     and lnkgmem_linkage_zdb_id = lnkg_zdb_id
     and recattrib_data_zdb_id = lnkg_zdb_id
     and recattrib_source_zdb_id in ('ZDB-PUB-030703-1','ZDB-PUB-020822-1') -- vega,embl
     and mrkr_abbrev = asmb_name
     and asmb_lg not in ('AB')
     and lnkg_or_lg != asmb_lg
)where lnkg_zdb_id in (
    select lnkgmem_linkage_zdb_id
     from assembly,marker,linkage_member,record_attribution
     where mrkr_type in ('BAC','PAC','FOSMID')
     and mrkr_zdb_id = lnkgmem_member_zdb_id
     and lnkgmem_linkage_zdb_id = lnkg_zdb_id
     and recattrib_data_zdb_id = lnkg_zdb_id
     and recattrib_source_zdb_id in ('ZDB-PUB-030703-1','ZDB-PUB-020822-1') -- vega,embl
     and mrkr_abbrev = asmb_name
     and asmb_lg not in ('AB')
     and lnkg_or_lg != asmb_lg
)
;

--------------------
! echo "Drop bac/pac in current assembly that already exist in ZFIN."
delete from assembly where exists (
    select 1 from marker
     where mrkr_type in ('BAC','PAC','FOSMID')
     and asmb_name = mrkr_name
 )
;

! echo "Are we sure?"
select mrkr_abbrev , mrkr_type --count(*)
 from marker,assembly
 where asmb_name = mrkr_abbrev
;
! echo "Are there any dup names in incomming assembly?"
select asmb_name, count(*)
 from assembly
 group by 1 having count(*) > 1
;

! echo "Which remaining clones do not know type lib etc for?"
select asmb_name[1,20],asmb_accession[1,12]
 from assembly where asmb_name[1,5] not in (
 'BUSM1',
 'CH211',
 'CH73-',
 'DKEY-',
 'DKEYP',
 'RP71-',
 'XX-BA',
 'XX-PA',
 'XX-DZ',
 'XX-BY',
 'ZFOS-',
 'XX-ZF',
 'CH107'--(3)
 );

! echo "Delete remaining clones we do not know type lib etc for."

 delete from assembly where asmb_name[1,5] not in (
 'BUSM1',
 'CH211',
 'CH73-',
 'DKEY-',
 'DKEYP',
 'RP71-',
 'XX-BA',
 'XX-PA',
 'XX-DZ',
 'XX-BY',
 'ZFOS-',
 'XX-ZF',
 'CH107'--(3)
 );

! echo "How many clones are left to add?"
select count(*) howmany from assembly;

! echo "Create markers&clones for the remaining bac/pac"

alter table assembly add mrkr_zad varchar(50);
alter table assembly add mrkr_typ varchar(50);
update  assembly set mrkr_typ = case
    when asmb_name[1,5] = 'BUSM1' then 'PAC'
    when asmb_name[1,5] = 'CH211' then 'BAC'
    when asmb_name[1,5] = 'CH73-' then 'BAC'
    when asmb_name[1,5] = 'DKEY-' then 'BAC'
    when asmb_name[1,5] = 'DKEYP' then 'BAC'
    when asmb_name[1,5] = 'RP71-' then 'BAC'
    when asmb_name[1,5] = 'XX-BA' then 'BAC'
    when asmb_name[1,5] = 'XX-PA' then 'PAC'
    when asmb_name[1,5] = 'XX-DZ' then 'PAC'
    when asmb_name[1,5] = 'XX-BY' then 'BAC'
    when asmb_name[1,5] = 'ZFOS-' then 'FOSMID'
    when asmb_name[1,5] = 'XX-ZF' then 'BAC'
    when asmb_name[1,6]= 'CH1073' then 'FOSMID'
    else 'XAC' -- expect to break
    end
;

update assembly set mrkr_typ  = trim(mrkr_typ)
 where octet_length(mrkr_typ) != length(mrkr_typ)
;

update  assembly set mrkr_zad = get_id(mrkr_typ);

insert into zdb_active_data select mrkr_zad from assembly;

insert into marker (
    mrkr_zdb_id,
    mrkr_name,
    mrkr_abbrev,
    mrkr_type,
    mrkr_owner
)
select
	mrkr_zad,
	asmb_name,
	asmb_name,
	mrkr_typ,
	'ZDB-PERS-001130-2'
 from assembly
;

insert into clone (
    clone_mrkr_zdb_id,
    clone_vector_name,
    clone_probelib_zdb_id,
    clone_sequence_type
) select mrkr_zad,
    case -- the casts are attempting to stop the case from adding trailing spaces ... not working
    when asmb_name[1,5] = 'BUSM1' then 'pCYPAC-6'
    when asmb_name[1,5] = 'CH211' then 'pTARBAC2.1'
    when asmb_name[1,5] = 'CH73-' then 'pTARBAC2.1'
    when asmb_name[1,5] = 'DKEY-' then 'pIndigoBAC-536'
    when asmb_name[1,5] = 'DKEYP' then 'pIndigoBAC-536'
    when asmb_name[1,5] = 'RP71-' then 'pTARBAC2'
    when asmb_name[1,5] = 'XX-BA' then 'pBeloBAC11'
    when asmb_name[1,5] = 'XX-PA' then 'pCYPAC-6'
    when asmb_name[1,5] = 'XX-DZ' then 'pCYPAC-6'
    when asmb_name[1,5] = 'XX-BY' then 'pBeloBAC11'
    when asmb_name[1,5] = 'ZFOS-' then 'pFOS-1'
    when asmb_name[1,5] = 'XX-ZF' then 'pFOS-1'
    when asmb_name[1,6]= 'CH1073' then 'pCC1FOS-CHA_PmII'
    when asmb_name[1,5] = 'IZAB-' then 'pBeloBAC11'
    else null
    end,
    case
    when asmb_name[1,5] = 'BUSM1' then 'ZDB-PROBELIB-020423-2'
    when asmb_name[1,5] = 'CH211' then 'ZDB-PROBELIB-020423-3'
    when asmb_name[1,5] = 'CH73-' then 'ZDB-PROBELIB-050214-1'
    when asmb_name[1,5] = 'DKEY-' then 'ZDB-PROBELIB-020423-4'
    when asmb_name[1,5] = 'DKEYP' then 'ZDB-PROBELIB-020423-5'
    when asmb_name[1,5] = 'RP71-' then 'ZDB-PROBELIB-020423-1'
    when asmb_name[1,5] = 'XX-BA' then 'ZDB-PROBELIB-040512-1' -- unknown
    when asmb_name[1,5] = 'XX-PA' then 'ZDB-PROBELIB-040512-1' -- unknown
    when asmb_name[1,5] = 'XX-DZ' then 'ZDB-PROBELIB-040512-1' -- unknown
    when asmb_name[1,5] = 'XX-BY' then 'ZDB-PROBELIB-040512-1' -- unknown
    when asmb_name[1,5] = 'ZFOS-' then 'ZDB-PROBELIB-100106-1'
    when asmb_name[1,5] = 'XX-ZF' then 'ZDB-PROBELIB-040512-1' -- unknown
    when asmb_name[1,6]= 'CH1073' then 'ZDB-PROBELIB-070723-1'
    when asmb_name[1,5] = 'IZAB-' then 'ZDB-PROBELIB-050523-2' -- Incyte
    else null
    end,
    'Genomic'
 from assembly
;
! echo "Get rid of trailing spaces in clone_vector_name."
update clone  set clone_vector_name = trim(clone_vector_name)
 where octet_length(clone_vector_name) != length(clone_vector_name)
;

! echo "Attribute new bac/pac to vega."
insert into record_attribution(recattrib_data_zdb_id,recattrib_source_zdb_id)
 select mrkr_zad, 'ZDB-PUB-030703-1' from assembly;
------------------------------------------------
! echo "Add GenBank accessions for the novel bac/pacs."
alter table assembly add dblink_zad varchar(50);
update assembly set dblink_zad = get_id('DBLINK');

insert into zdb_active_data select dblink_zad from assembly;
insert into db_link (
    dblink_linked_recid,
    dblink_acc_num,
    dblink_fdbcont_zdb_id,
    dblink_length,
    dblink_zdb_id
) select
    mrkr_zad,
    asmb_accession,
    'ZDB-FDBCONT-040412-36',
    asmb_length,
    dblink_zad
 from assembly;

! echo "Attribute new bac/pac accession links to Vega."
insert into record_attribution(recattrib_data_zdb_id,recattrib_source_zdb_id)
 select dblink_zad, 'ZDB-PUB-030703-1' from assembly;

------------------------------------------------
! echo "Add Vega_Clone links for the novel bac/pacs."
update assembly set dblink_zad = get_id('DBLINK');

insert into zdb_active_data select dblink_zad from assembly;
--set triggers for db_link disabled;
insert into db_link (
    dblink_linked_recid,
    dblink_acc_num,
    dblink_fdbcont_zdb_id,
    dblink_length,
    --dblink_acc_num_display,
    dblink_zdb_id
) select
    mrkr_zad,
    asmb_accession,
    'ZDB-FDBCONT-040826-2',
    asmb_length,
    --asmb_accession[1,8],
    dblink_zad
 from assembly

;


--set triggers for db_link enabled;
! echo "Attribute new bac/pac Vega_Clone links to Vega."

insert into record_attribution(recattrib_data_zdb_id,recattrib_source_zdb_id)
 select dblink_zad, 'ZDB-PUB-030703-1' from assembly
  --where asmb_lg != 'H'
 ;

--------------------------------
--###############################################################################
! echo "Adding LG linkage info for novel bac/pacs..."

! echo "Don't make linkages for NULL Linkage group. (should be 0)"
delete from assembly where asmb_lg is NULL;

! echo "Don't make linkages for Sangers 'AB' Linkage group "
delete from assembly where asmb_lg in('AB');

! echo "Convert Sanger U chromosome to zfin LG 0."
-----
update assembly set dblink_zad = get_id('LINK');
insert into zdb_active_data select dblink_zad from assembly;

insert into linkage (
    lnkg_zdb_id,
   lnkg_or_lg,
    lnkg_comments,
    lnkg_submitter_zdb_id
  )
  select
   dblink_zad,
    case
    	when asmb_lg = 'U' then '0'
    	else asmb_lg
    end,
   "Clones chromosome assignment by the Sanger Institute's Vega assembly as of " || TODAY,
   'ZDB-PERS-001130-2'
  from assembly
;

insert into linkage_member (lnkgmem_linkage_zdb_id, lnkgmem_member_zdb_id)
  select dblink_zad, mrkr_zad
   from assembly
;

! echo "Attribute LG linkage to Vega"
insert into record_attribution(recattrib_data_zdb_id, recattrib_source_zdb_id)
  select dblink_zad,'ZDB-PUB-030703-1'
   from assembly
;

---------------------------------

! echo "Include a '(order this)' link on new clones."

select distinct mrkr_zdb_id zdb,'ZDB-LAB-040701-1' lab ,mrkr_abbrev acc
 from marker
 where mrkr_type = 'BAC'
 and mrkr_abbrev[1,5] in ('CH211','CH73-','RP71-')
union
select mrkr_zdb_id,'ZDB-COMPANY-051101-1',mrkr_abbrev
 from marker
 where mrkr_type = 'BAC'
 and mrkr_abbrev[1,5] = 'DKEY-'
union
select mrkr_zdb_id,'ZDB-LAB-970513-1',mrkr_abbrev
 from marker
 where mrkr_type = 'PAC'
 and mrkr_abbrev[1,6] = 'BUSM1-'
union
select mrkr_zdb_id,'ZDB-LAB-040701-1',mrkr_abbrev
 from marker
 where mrkr_type = 'FOSMID'
 and mrkr_abbrev[1,6] = 'CH1073'
--union
--select distinct mrkr_zdb_id zdb,'???' lab ,mrkr_abbrev acc
-- from marker
-- where mrkr_type = 'BAC'
-- and mrkr_abbrev[1,5] = 'IZAB-'

 into temp tmp_ids with no log
;

-- trim trailing space from case sttatement
update tmp_ids set lab = trim(lab);
-- incase any others got through
--update int_data_supplier set idsup_supplier_zdb_id = trim(idsup_supplier_zdb_id);

 -- DKEYP- is on hold  IZAB- is new



! echo "drop incomming supplier info that already exists"
delete from tmp_ids where exists (
    select 't' from int_data_supplier
     where  zdb = idsup_data_zdb_id
     and    lab = idsup_supplier_zdb_id
     --and    acc = idsup_acc_num
);

! echo "find supplier info that may change"
select zdb,acc,lab,idsup_supplier_zdb_id[1,25],idsup_acc_num[1,20]
 from int_data_supplier,tmp_ids
 where zdb = idsup_data_zdb_id
   --and acc = idsup_acc_num
   and lab != idsup_supplier_zdb_id
;

! echo "find internal duplicates"
select zdb,lab,count(*) howmany
 from tmp_ids
 group by 1,2 having count(*) > 1
;


! echo "add supplier"
insert into int_data_supplier(
    idsup_data_zdb_id, idsup_supplier_zdb_id, idsup_acc_num
 ) select distinct * from tmp_ids;

drop table tmp_ids;



---------------------------------

! echo "How many clones w no link to Vega/Ensembl?"
select count(distinct mrkr_zdb_id) ens_cln_lnk
 from marker
 where mrkr_type in( 'BAC','PAC','FOSMID')
   and not exists (
    	select 1 from db_link
    	 where dblink_fdbcont_zdb_id in ('ZDB-FDBCONT-040826-2','ZDB-FDBCONT-061004-1')
    	   and dblink_linked_recid = mrkr_zdb_id
 )
;

! echo "How many clones w/genes-tscripts and no link to Vega/Ensembl?"
select count(distinct mrkr_zdb_id) ens_cln_lnk
 from marker, marker_relationship ct, marker_relationship gt
 where mrkr_type in( 'BAC','PAC','FOSMID')
   and ct.mrel_type = 'clone contains transcript'
   and ct.mrel_mrkr_2_zdb_id == gt.mrel_mrkr_2_zdb_id
   and gt.mrel_type = 'gene produces transcript'
   and mrkr_zdb_id = ct.mrel_mrkr_1_zdb_id
   and not exists (
    	select 1 from db_link
    	 where dblink_fdbcont_zdb_id in ('ZDB-FDBCONT-040826-2','ZDB-FDBCONT-061004-1')
    	   and dblink_linked_recid = mrkr_zdb_id
);

{

! echo "Make Ensembl_Clone links for Bacs on the Haploid LG for clones in current assenbly"

insert into zdb_active_data select dblink_zad from assembly where asmb_lg == 'H';
insert into db_link (
    dblink_linked_recid,
    dblink_acc_num,
    dblink_fdbcont_zdb_id,
    dblink_length,
    --dblink_acc_num_display,
    dblink_zdb_id
) select
    mrkr_zad,
    asmb_accession,
    'ZDB-FDBCONT-061004-1', --Ensembl_Clone
    asmb_length,
    --asmb_accession,
    dblink_zad
 from assembly
 where asmb_lg == 'H'
;

! echo "Attribute new bac/pac Ensembl_Clone links to Vega."

insert into record_attribution(recattrib_data_zdb_id,recattrib_source_zdb_id)
 select dblink_zad, 'ZDB-PUB-030703-1' from assembly
  where asmb_lg == 'H'
 ;


! echo "Clones that fell off the assembly before this load might also be candidates for Ensembl_Clone links"
! echo "but I am going to leave them for now since this link helps me identify known haploid  clones in the databsae"

}


----------------------------------
drop table assembly;


! echo "transaction terminated externaly"
--rollback work;
--commit work;

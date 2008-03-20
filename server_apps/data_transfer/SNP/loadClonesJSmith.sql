-- loadClonesJSmith.sql
-- one time use script
-- load the clone associated with Smith SNPs and that ZFIN doesn't have

begin work;

create table clones_Smith (
        cloneName varchar(50),
        accession varchar(50),
        vector varchar(80),
        probe varchar(50),
        type varchar(10),
        lg varchar(2)
);

load from newClonesJSmith.unl
 insert into clones_Smith;

delete from  clones_Smith
 where exists (
    select 1 from marker
     where cloneName = mrkr_name
);

alter table clones_Smith add mrkr_id varchar(50);

update clones_Smith set mrkr_id = get_id(type);
insert into zdb_active_data select mrkr_id from clones_Smith;
! echo "         into zdb_active_data table."

insert into marker (
    mrkr_zdb_id,
    mrkr_name,
    mrkr_abbrev,
    mrkr_type,
    mrkr_owner
)
select
        mrkr_id,
        cloneName,
        cloneName,
        type,
        'ZDB-PERS-050706-1'
 from clones_Smith
;

! echo "         into marker table."

insert into clone (
    clone_mrkr_zdb_id,
    clone_vector_name,
    clone_probelib_zdb_id,
    clone_sequence_type
) select 
     mrkr_id,
     vector,
     probe,
    'Genomic'
 from clones_Smith
;

! echo "         into clone table."


-- ! echo "Attribute new bac/pac to Jeff Smith."
-- insert into record_attribution(recattrib_data_zdb_id,recattrib_source_zdb_id)
-- select mrkr_id, 'ZDB-PUB-070427-10' from clones_Smith;
-- ! echo "         into record_attribution table." 
 
 
! echo "Add GenBank accessions for the novel bac/pacs."
alter table clones_Smith add dblink_id varchar(50);
update clones_Smith set dblink_id = get_id('DBLINK');

insert into zdb_active_data select dblink_id from clones_Smith;
! echo "         into zdb_active_data table."

insert into db_link (
    dblink_linked_recid,
    dblink_acc_num,
    dblink_fdbcont_zdb_id,
    dblink_zdb_id
) select
    mrkr_id,
    accession,
    'ZDB-FDBCONT-040412-36',
    dblink_id
 from clones_Smith;
! echo "         into db_link table."

-- ! echo "Attribute new bac/pac accession links to Jeff Smith."
-- insert into record_attribution(recattrib_data_zdb_id,recattrib_source_zdb_id)
--  select dblink_id, 'ZDB-PUB-070427-10' from clones_Smith;
-- ! echo "         into record_attribution table." 


create temp table pre_lg (    
        dblink_id varchar(50),
        mrkr_id varchar(50),
        cloneName varchar(50),
        accession varchar(50),
        vector varchar(80),
        probe varchar(50),
        type varchar(10),
        lg varchar(2)
    ) with no log;

insert into pre_lg (dblink_id,mrkr_id,cloneName,accession,vector,probe,type,lg)
  select dblink_id,mrkr_id,cloneName,accession,vector,probe,type,lg
    from clones_Smith
    where lg != 'un';
! echo "         into pre_lg table."

! echo "Adding LG linkage info for novel bac/pacs if there is LG info"
! echo "Don't make linkages for unknown linkage group."

update pre_lg set dblink_id = get_id('LINK');

insert into zdb_active_data select dblink_id from pre_lg;
! echo "         into zdb_active_data table."

insert into linkage (
    lnkg_zdb_id,
    lnkg_or_lg,
    lnkg_comments,
    lnkg_submitter_zdb_id
  )
  select
    dblink_id,
    lg,
   "Clones chromosone assignment by the association of Jeff Smith SNP as of " ||
TODAY,
   'ZDB-PERS-050706-1'
  from pre_lg
;
! echo "         into linkage table."


insert into linkage_member (lnkgmem_linkage_zdb_id, lnkgmem_member_zdb_id)
  select dblink_id, mrkr_id
   from pre_lg
;
! echo "         into linkage_member table."

-- ! echo "Attribute LG linkage to Jeff Smith"
-- insert into record_attribution(recattrib_data_zdb_id, recattrib_source_zdb_id)
--  select dblink_id,'ZDB-PUB-070427-10'
--   from pre_lg
-- ;
-- ! echo "         into record_attribution table."


drop table pre_lg;

drop table clones_Smith;

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
 into temp tmp_ids with no log
;

-- trim trailing space from case sttatement
update tmp_ids set lab = trim(lab);
-- incase any others got through
update int_data_supplier set idsup_supplier_zdb_id = trim(idsup_supplier_zdb_id);

 -- DKEYP- is on hold

delete from tmp_ids where exists (
    select 1 from int_data_supplier
     where  zdb = idsup_data_zdb_id
     and    lab = idsup_supplier_zdb_id
     and    acc = idsup_acc_num
);

insert into int_data_supplier(
    idsup_data_zdb_id,idsup_supplier_zdb_id,idsup_acc_num
 ) select * from tmp_ids;


drop table tmp_ids;

--rollback work;

commit work;
begin work;
create table prot_len_acc (pla_prot varchar (10), pla_len integer, pla_acc varchar(10));
load from 'prot_len_acc.unl' insert into prot_len_acc;

create index pla_prot_idx on prot_len_acc(pla_prot);
create index pla_acc_idx on prot_len_acc(pla_acc);
update statistics for table prot_len_acc;

!echo "make input unique"
select distinct * from prot_len_acc into temp tmp_pla with no log;
delete from prot_len_acc;
insert into prot_len_acc select * from tmp_pla;
drop table tmp_pla;

{
-- first time only -------------------------------------------
update db_link set dblink_length = (
    select distinct pla_len 
    from prot_len_acc
    where pla_prot = acc_num
) 
where db_name = 'GenPept'
and acc_num in (
    select pla_prot from prot_len_acc
);

-- adopt any unattributed Genpept links
! echo "Attribute existing Genpept links to ZFIN citation -onetime"

insert into record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id)
select dblink_zdb_id ,(
            select zdb_id from publication 
            where authors = 'ZFIN Staff'
            and title = 'Curation of NCBI Protein Sequence Database Links'   
            )
from db_link where db_name = 'GenPept' and dblink_zdb_id not in (
    select recattrib_data_zdb_id 
    from record_attribution
    where recattrib_source_zdb_id = (
        select zdb_id from publication 
        where authors = 'ZFIN Staff'
        and title = 'Curation of NCBI Protein Sequence Database Links'   
    )
)
;           
}

---------------------------------------------------------------
{
! echo "drop incomming links already in ZFIN which are unchanged"
delete from prot_len_acc where exists ( 
    select 1 from db_link, record_attribution -- 
    where db_name = 'GenPept' 
    and  pla_prot = acc_num
    and  pla_len = dblink_length
    and  recattrib_data_zdb_id = dblink_zdb_id
    and  recattrib_source_zdb_id in ( 
        select zdb_id from publication 
        where authors = 'ZFIN Staff'
        and title = 'Curation of NCBI Protein Sequence Database Links'   
    )
);
}

! echo "drop GenPepts existing ZFIN attributed to NCBI Protein"
delete from zdb_active_data where zactvd_zdb_id in (
    select dblink_zdb_id 
    from db_link,record_attribution
    where db_name = 'GenPept' 
    and  recattrib_data_zdb_id = dblink_zdb_id
    and  recattrib_source_zdb_id in ( 
        select zdb_id from publication 
        where authors = 'ZFIN Staff'
        and title in ('Curation of EMBL records' ,'Curation of NCBI Protein Sequence Database Links')  
    )
);

! echo "find the new Genpept links to add"
select 
    linked_recid, 
    'GenPept' dbname,
     pla_prot acc,
    'uncurrated ' || TODAY info, 
    '1234567890123456789012345' zad, 
     pla_prot acc_display,
    'Zebrafish' organism,
    'protein sequence' type,
     max(pla_len) len
from  db_link, prot_len_acc
where db_name in ('Genbank','SwissProt', 'RefSeq', 'LocusLink')
and   acc_num = pla_acc
group by 1,3,6 
into temp tmp_dblk with no log;

update tmp_dblk set zad = get_id('DBLINK');  

insert into zdb_active_data select zad from tmp_dblk;

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
) select * from tmp_dblk
;


! echo "Attribute Genpept links to ZFIN citation"
insert into record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id)
select zad ,(select zdb_id from publication 
            where authors = 'ZFIN Staff'
            and title = 'Curation of NCBI Protein Sequence Database Links'   
            )
from tmp_dblk
;           

unload to 'unused_proteins.unl' 
select * from prot_len_acc
where pla_prot not in (
    select acc from tmp_dblk
);   


drop table tmp_dblk;
drop table prot_len_acc;

commit work;


begin work;
create table prot_len_acc (pla_prot varchar (10), pla_len integer, pla_acc varchar(10));
load from 'prot_len_acc.unl' insert into prot_len_acc;

create index pla_prot_idx on prot_len_acc(pla_prot);
create index pla_acc_idx on prot_len_acc(pla_acc);
update statistics for table prot_len_acc;

!echo "make input unique"
select distinct * from prot_len_acc into temp tmp_pla with no log;
delete from prot_len_acc;
insert into prot_len_acc select * from tmp_pla
where pla_prot not in 
(
'NP_059341',--  78                
'NP_059339',--  78                
'NP_059335',--  78                
'NP_059338',--  78                
'NP_059342',--  78                
'NP_059337',--  78                
'NP_059332',--  78                
'NP_059331',--  78                
'NP_059340',--  78                
'NP_059334',--  78                
'NP_059343',--  78                
'NP_059333',--  78                
'NP_059336',--  78                
'AAF27271',--   276               
'AAF27263',--   276               
'AAF27274',--   276               
'AAF27265',--   276               
'AAF27269',--   276               
'AAF27275',--   276               
'AAF27268',--   276               
'AAF27259',--   276               
'AAF27264',--   276               
'AAF27266',--   276               
'AAF27258',--   276               
'AAF27260',--   276               
'AAF27267',--   276               
'AAF27272',--   276               
'AAF27262',--   276               
'AAF27261',--   276               
'AAF27276',--   276               
'AAF27277',--   276               
'AAF27273',--   276               
'AAF27270',--   276               
'AAF74303',--   741               
'AAF74299',--   741               
'AAF74302',--   741               
'AAF74301',--   741               
'AAF74300',--   741               
'AAF74305',--   741               
'AAF74298',--   741               
'AAF74308',--   741               
'AAF74309',--   741               
'AAF74297',--   741               
'AAF74306',--   741               
'AAF74304',--   741               
'AAF74307' --   741 
)
;
drop table tmp_pla;


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

! echo "adopt existing Genpept links to with NO citation"

insert into record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id)
select dblink_zdb_id ,(
	select zdb_id from publication 
        where authors = 'ZFIN Staff'
        and title = 'Curation of NCBI Protein Sequence Database Links'   
)
from db_link 
where db_name = 'GenPept' 
and dblink_zdb_id not in (select recattrib_data_zdb_id from record_attribution)
;

---------------------------------------------------------------

! echo "drop GenPepts with existing ZFIN attribution to NCBI Protein or EMBL"
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
from  db_link, prot_len_acc, marker
where db_name in ('Genbank','SwissProt', 'RefSeq', 'LocusLink')
and   acc_num = pla_acc
and   mrkr_type = 'GENE'
and mrkr_zdb_id = linked_recid
group by 1,3,6 
into temp tmp_dblk with no log;

! echo "drop NP_ GenPepts that are already in as RefSeq"
delete from tmp_dblk where acc in (
    select acc_num from db_link 
    where db_name = 'Ref_seq' and acc_num[1,3] = 'NP_'
);

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

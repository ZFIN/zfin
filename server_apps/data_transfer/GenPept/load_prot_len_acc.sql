begin work;

create table prot_len_acc (pla_prot varchar (10), pla_len integer,pla_gene varchar(100), pla_acc varchar(10));
load from 'prot_len_acc.unl' insert into prot_len_acc;

create index pla_prot_idx on prot_len_acc(pla_prot);
create index pla_acc_idx on prot_len_acc(pla_acc);
create index pla_gene_idx on prot_len_acc(pla_gene);
update statistics for table prot_len_acc;


!echo "make input unique"
select distinct * from prot_len_acc into temp tmp_pla with no log;
delete from prot_len_acc;
insert into prot_len_acc select * from tmp_pla;

drop table tmp_pla;

! echo "Check that (manual curation) GenPept has sequence length"
update db_link set dblink_length = (
    select pla_len
    from prot_len_acc
    where dblink_acc_num = pla_acc
) 
where dblink_length in (0,'',null)
and dblink_acc_num in (select pla_acc from prot_len_acc)
and dblink_fdbcont_zdb_id in (
    select fdbcont_zdb_id
    from foreign_db_contains
    where fdbcont_fdb_db_name = 'GenPept'
    and fdbcont_fdbdt_data_type = 'Polypeptide'
)
;


! echo "Drop from consideration GenPept with manual curation"
delete from prot_len_acc
where exists (
    select 1 from db_link, record_attribution, foreign_db_contains
    where dblink_zdb_id = recattrib_data_zdb_id
    and recattrib_source_zdb_id = 'ZDB-PUB-020723-5' --Manually curated data (Curation)
    and dblink_fdbcont_zdb_id = fdbcont_zdb_id
    and fdbcont_fdb_db_name = 'GenPept'
    and dblink_acc_num = pla_prot
);   
-- 
-- may need to also filter on nt-accessions and symbols for these GENES
-- but we have no connection between a GenPept and the nt-acc that coded it ...
{
-- first time only -------------------------------------------
update db_link set dblink_length = (
    select distinct pla_len 
    from prot_len_acc
    where pla_prot = dblink_acc_num
) 
where dblink_fdbcont_zdb_id in (select fdbcont_zdb_id where fdbcont_fdb_db_name = 'GenPept')
and dblink_acc_num in (
    select pla_prot from prot_len_acc
);
}
! echo "split off nt-acc with more than one protein"
select pla_acc from prot_len_acc group by 1 having count(*) > 1
into temp tmp_Genomic_acc with no log;

select * from prot_len_acc 
where pla_acc in (select * from tmp_Genomic_acc)
into temp tmp_Genomic_pla with no log; 

delete from prot_len_acc 
where pla_acc in (select * from tmp_Genomic_acc);

drop table tmp_Genomic_acc;
update statistics high for table prot_len_acc;

! echo "adopt existing Genpept links to with NO citation"

insert into record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id)
select dblink_zdb_id ,(
	select zdb_id from publication 
        where authors = 'ZFIN Staff'
        and title = 'Curation of NCBI Protein Sequence Database Links'   
)
from db_link, foreign_db_contains 
where dblink_fdbcont_zdb_id = fdbcont_zdb_id
and fdbcont_fdb_db_name = 'GenPept' 
and dblink_zdb_id not in (select recattrib_data_zdb_id from record_attribution)
;

---------------------------------------------------------------

! echo "drop GenPepts with existing ZFIN attribution to NCBI Protein or EMBL"
delete from zdb_active_data where zactvd_zdb_id in (
    select dblink_zdb_id 
    from db_link,record_attribution, foreign_db_contains
    where dblink_fdbcont_zdb_id = fdbcont_zdb_id
    and  fdbcont_fdb_db_name = 'GenPept' 
    and  recattrib_data_zdb_id = dblink_zdb_id
    and  recattrib_source_zdb_id in ( 
        select zdb_id from publication 
        where authors = 'ZFIN Staff'
        and title in ('Curation of EMBL records' ,'Curation of NCBI Protein Sequence Database Links')  
    )
);

! echo "find the simple(protein & nt_accession unique) Genpept links to add"

create temp table tmp_dblk
  (
    dblink_linked_recid varchar(50),
    acc varchar(10),
    zad varchar(50),
    len integer,
    fdbcont_zdb_id varchar(50)  
  ) with no log;
  
insert into tmp_dblk
select 
    dblink_linked_recid, 
    pla_prot, 
    'x', 
    max(pla_len),
    pept.fdbcont_zdb_id 
from  db_link, prot_len_acc, marker, foreign_db_contains nonpept, foreign_db_contains pept
where dblink_fdbcont_zdb_id = nonpept.fdbcont_zdb_id
and   nonpept.fdbcont_fdb_db_name in ('Genbank','SwissProt', 'RefSeq', 'LocusLink')
and   dblink_acc_num = pla_acc
and   mrkr_type in ('GENE','EST')
and   mrkr_zdb_id = dblink_linked_recid
and   pept.fdbcont_fdb_db_name = "GenPept"
and   pept.fdbcont_fdbdt_data_type = "Polypeptide"
group by 1,2,5; 
--having count(*) = 1 -- just the distinct ones first
--into temp tmp_dblk with no log;

! echo "drop NP_ GenPepts that are already in as RefSeq"
delete from tmp_dblk where acc in (
    select dblink_acc_num 
    from db_link 
    where dblink_acc_num[1,3] = 'NP_'
    and dblink_fdbcont_zdb_id in (
        select fdbcont_zdb_id 
        from foreign_db_contains
        where fdbcont_fdb_db_name = 'Ref_seq' 
    )
);

update tmp_dblk set zad = get_id('DBLINK');  

insert into zdb_active_data select zad from tmp_dblk;

--set explain on;
--drop trigger dblink_insert_trigger;

insert into db_link(
    dblink_linked_recid,
    dblink_fdbcont_zdb_id,
    dblink_acc_num,
    dblink_info,
    dblink_zdb_id,
    dblink_acc_num_display,
    dblink_length
) select 
    dblink_linked_recid,
    fdbcont_zdb_id,
    acc,
    'uncurrated ' || TODAY, 
    zad, 
    acc,
    len 
from tmp_dblk
;


! echo "Attribute Genpept links to ZFIN citation"
insert into record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id)
select zad ,(select zdb_id from publication 
            where authors = 'ZFIN Staff'
            and title = 'Curation of NCBI Protein Sequence Database Links'   
            )
from tmp_dblk
;   
drop table tmp_dblk; 

delete from prot_len_acc where pla_prot in (
    select dblink_acc_num from db_link 
    where dblink_fdbcont_zdb_id in (
        select fdbcont_zdb_id
        from foreign_db_contains
        where fdbcont_fdb_db_name in ('GenPept','RefSeq')
    )
);
{
! echo "These are the non unique candidates"
select
    dblink_linked_recid[1,25], 
    mrkr_abbrev[1,20] symbol,
    pla_prot[1,10] genpept,
    pla_gene gb_name,
    count(*) howmany
from  db_link, prot_len_acc, marker, foreign_db_contains
where dblink_fdbcont_zdb_id = fdbcont_zdb_id
and   fdbcont_fdb_db_name in ('Genbank','SwissProt', 'RefSeq', 'LocusLink')
and   dblink_acc_num = pla_acc
and   mrkr_type in ('GENE','EST')
and   mrkr_zdb_id = dblink_linked_recid
group by 1,2,3,4
;
should be closed comment here
}



-------------------------------------------------------------------------------
! echo "these are the proteins that did not find a GENE, or EST" 
! echo "check v.s. everything  else cept big clones BAC,PAC,YAC," 
! echo "we would have to pull the GenPept link forward to the Gene"

select 
     est.dblink_linked_recid, 
     pla_prot acc,
    '1234567890123456789012345' zad, 
     max(pla_len) len,
     fdbcont_zdb_id
from  db_link est, prot_len_acc, marker, foreign_db_contains
where est.dblink_fdbcont_zdb_id = fdbcont_zdb_id
and   fdbcont_fdb_db_name = 'Genbank'
and   fdbcont_fdbdt_data_type = 'Polypeptide'
and   est.dblink_acc_num = pla_acc
and   mrkr_type not in ('GENE','BAC','PAC','YAC','EST')
and   mrkr_zdb_id = est.dblink_linked_recid
group by 1,2,5
into temp tmp_dblk
 with no log;
 


! echo "drop NP_ GenPepts that are already in as RefSeq"
delete from tmp_dblk where acc in (
    select dblink_acc_num from db_link 
    where dblink_acc_num[1,3] = 'NP_'
    and dblink_fdbcont_zdb_id in (
        select fdbcont_zdb_id 
        from foreign_db_contains
        where fdbcont_fdb_db_name = 'Ref_seq'
    )
);


update tmp_dblk set zad = get_id('DBLINK');
! echo "ODDITIES"  
select * from tmp_dblk;

insert into zdb_active_data select zad from tmp_dblk;

insert into db_link(
    dblink_linked_recid,
    dblink_fdbcont_zdb_id,
    dblink_acc_num,
    dblink_info,
    dblink_zdb_id,
    dblink_acc_num_display,
    dblink_length
) select 
    dblink_linked_recid, 
    fdbcont_zdb_id,
    acc,
    'uncurrated ' || TODAY, 
    zad, 
    acc,
    len 
from tmp_dblk
;

! echo "Attribute Genpept links to ZFIN citation"
insert into record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id)
select zad ,(select zdb_id from publication 
            where authors = 'ZFIN Staff'
            and title = 'Curation of NCBI Protein Sequence Database Links'   
            )
from tmp_dblk
;   
drop table tmp_dblk; 


-------------------------------------------------------------------------------
-- Protein derived from Genomic DNS or alt transcripts and weirdness
------------------------------------------------------------------------------- 

! echo "now try the NON-UNIQUE proteins"
! echo "**********************************************************************"
! echo ""
delete from tmp_Genomic_pla where pla_prot in ( -- just in case
    select dblink_acc_num from db_link 
    where dblink_fdbcont_zdb_id in (
        select fdbcont_zdb_id 
        from foreign_db_contains
        where fdbcont_fdb_db_name in ('GenPept','RefSeq')
    )
);     

! echo "find the new Genpept links to add where there is an exact symbol match"
select
    dblink_linked_recid, 
     pla_prot acc,
    '1234567890123456789012345' zad, 
     max(pla_len) len,
     fdbcont_zdb_id
from  db_link, tmp_Genomic_pla, marker, foreign_db_contains
where dblink_fdbcont_zdb_id = fdbcont_zdb_id
and   fdbcont_fdb_db_name in ('Genbank','SwissProt', 'RefSeq', 'LocusLink')
and   fdbcont_fdbdt_data_type = 'Polypeptide'
and   dblink_acc_num = pla_acc
and   pla_gene = mrkr_abbrev --- the ones with an exact name match 
and   mrkr_type in ('GENE','EST')
and mrkr_zdb_id = dblink_linked_recid
group by 1,2,5
--having count(*) < 2
into temp tmp_dblk with no log;

! echo "drop NP_ GenPepts that are already in as RefSeq"
delete from tmp_dblk where acc in (
    select dblink_acc_num from db_link 
    where dblink_acc_num[1,3] = 'NP_'
    and dblink_fdbcont_zdb_id in (
        select fdbcont_zdb_id
        from foreign_db_contains
        where fdbcont_fdb_db_name = 'Ref_seq'
    )
);

! echo "second cut"
select
    mrkr_abbrev sym, 
    pla_prot gp,
    pla_acc coded_by
from  db_link, tmp_Genomic_pla, marker, foreign_db_contains
where dblink_fdbcont_zdb_id = fdbcont_zdb_id
and   fdbcont_fdb_db_name in ('Genbank','SwissProt', 'RefSeq', 'LocusLink')
and   fdbcont_fdbdt_data_type = 'Polypeptide'
and   dblink_acc_num = pla_acc
and   pla_gene = mrkr_abbrev --- the ones with an exact name match 
and   mrkr_type in ('GENE','EST')
and mrkr_zdb_id = dblink_linked_recid
group by 1,2,3 
order by 1,2,3;

update tmp_dblk set zad = get_id('DBLINK');  

insert into zdb_active_data select zad from tmp_dblk;

insert into db_link(
    dblink_linked_recid,
    dblink_fdbcont_zdb_id,
    dblink_acc_num,
    dblink_info,
    dblink_zdb_id,
    dblink_acc_num_display,
    dblink_length
) select 
    dblink_linked_recid, 
    fdbcont_zdb_id,
    acc,
    'uncurrated ' || TODAY, 
    zad, 
    acc,
    len 
from tmp_dblk
;

! echo "Attribute Genpept links to ZFIN citation"
insert into record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id)
select zad ,(select zdb_id from publication 
            where authors = 'ZFIN Staff'
            and title = 'Curation of NCBI Protein Sequence Database Links'   
            )
from tmp_dblk
;  
drop table tmp_dblk;       

! echo "delete the second cut"

delete from tmp_Genomic_pla where pla_prot in ( -- just in case
    select dblink_acc_num from db_link 
    where dblink_fdbcont_zdb_id in (
        select fdbcont_zdb_id
        from foreign_db_contains
        where fdbcont_fdb_db_name ='GenPept'
    )
);

---------------------------------------
! echo "2.5 cut -- where there is an exact symbol match but more than one gb_names to match to"
select
    dblink_linked_recid, 
    pla_prot acc,
    pla_gene
from  db_link, tmp_Genomic_pla, marker, foreign_db_contains
where dblink_fdbcont_zdb_id = fdbcont_zdb_id
and   fdbcont_fdb_db_name in ('Genbank','SwissProt', 'RefSeq', 'LocusLink')
and   dblink_acc_num = pla_acc
--and   mrkr_abbrev in pla_acc::COLLECTION -- make a spl to convert varchar to collection
and   pla_acc like conc(conc("%",mrkr_abbrev),"%")
and   mrkr_type in ('GENE','EST')
and mrkr_zdb_id = dblink_linked_recid
group by  1,2,3
;

! echo "look for a single exact match on previous names"
! echo "find the new Genpept links to add where there is an exact match with a previous name"
select 
    dblink_linked_recid, 
     pla_prot acc,
    '1234567890123456789012345' zad, 
     max(pla_len) len, --,pla_gene,
    fdbcont_zdb_id
from  db_link, tmp_Genomic_pla, marker, data_alias, foreign_db_contains
where dblink_fdbcont_zdb_id = fdbcont_zdb_id
and   fdbcont_fdb_db_name in ('Genbank','SwissProt', 'RefSeq', 'LocusLink')
and   fdbcont_fdbdt_data_type = 'Polypeptide'
and   dblink_acc_num = pla_acc
and   pla_gene = lower(dalias_alias) --- the ones with an exact name match 
and   dalias_data_zdb_id = mrkr_zdb_id
and   mrkr_type in ('GENE','EST')
and   mrkr_zdb_id = dblink_linked_recid
group by 1,2,5 
into temp tmp_dblk with no log;

! echo "drop NP_ GenPepts that are already in as RefSeq"
delete from tmp_dblk where acc in (
    select dblink_acc_num from db_link 
    where dblink_acc_num[1,3] = 'NP_'
    and dblink_fdbcont_zdb_id in (
        select fdbcont_zdb_id
        from foreign_db_contains
        where fdbcont_fdb_db_name = 'Ref_seq' 
    )
);

! echo "third cut"
select 
    mrkr_abbrev sym, 
    pla_prot gp,
    pla_acc coded_by
from  db_link, tmp_Genomic_pla, marker, data_alias, foreign_db_contains
where dblink_fdbcont_zdb_id = fdbcont_zdb_id
and   fdbcont_fdb_db_name in ('Genbank','SwissProt', 'RefSeq', 'LocusLink')
and   fdbcont_fdbdt_data_type = 'Polypeptide'
and   dblink_acc_num = pla_acc
and   pla_gene = lower(dalias_alias) --- the ones with an exact name match 
and   dalias_data_zdb_id = mrkr_zdb_id
and   mrkr_type in ('GENE','EST')
and   mrkr_zdb_id = dblink_linked_recid
group by 1,2,3 
order by 1,2,3
;

update tmp_dblk set zad = get_id('DBLINK');  

insert into zdb_active_data select zad from tmp_dblk;

insert into db_link(
    dblink_linked_recid,
    dblink_fdbcont_zdb_id,
    dblink_acc_num,
    dblink_info,
    dblink_zdb_id,
    dblink_acc_num_display,
    dblink_length
) select 
    dblink_linked_recid, 
    fdbcont_zdb_id,
    acc,
    'uncurrated ' || TODAY, 
    zad, 
    acc,
    len 
from tmp_dblk
;

! echo "Attribute Genpept links to ZFIN citation"
insert into record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id)
select zad ,(select zdb_id from publication 
            where authors = 'ZFIN Staff'
            and title = 'Curation of NCBI Protein Sequence Database Links'   
            )
from tmp_dblk
; 

! echo "delete the third cut"

delete from tmp_Genomic_pla where pla_prot in ( -- just in case
    select dblink_acc_num from db_link 
    where dblink_fdbcont_zdb_id in (
        select fdbcont_zdb_id 
        from foreign_db_contains
        where fdbcont_fdb_db_name ='GenPept'
    )
);

---
! echo "see what is left, will run diff against last weeks version"
unload to 'potential_problems.unl'
select 
    mrkr_abbrev[1,20] symbol,
    pla_prot[1,10] genpept,
    pla_gene[1,20] gb_name,
    dblink_acc_num[1,10] coded_by
from  db_link, tmp_Genomic_pla, marker, foreign_db_contains
where dblink_fdbcont_zdb_id = fdbcont_zdb_id
and   fdbcont_fdb_db_name in ('Genbank','SwissProt', 'RefSeq', 'LocusLink')
and   fdbcont_fdbdt_data_type = 'Polypeptide'
and   dblink_acc_num = pla_acc
and   mrkr_type = 'GENE'
and   mrkr_zdb_id = dblink_linked_recid
group by 1,2,3,4
order by 1,2,3,4
;

drop table tmp_Genomic_pla;

unload to 'unused_proteins.unl' 

select * from prot_len_acc
where pla_prot not in (
    select dblink_acc_num from db_link
    where dblink_fdbcont_zdb_id in (
        select fdbcont_zdb_id
        from foreign_db_contains
        where fdbcont_fdb_db_name in ('GenPept','RefSeq')
    )
);   

drop table prot_len_acc;
-- rollback work;
--
commit work;

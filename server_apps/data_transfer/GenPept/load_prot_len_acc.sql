begin work;

create table prot_len_acc (pla_prot varchar (10), pla_len integer,pla_gene varchar(100), pla_acc varchar(10));
load from 'prot_len_acc.unl' insert into prot_len_acc;

create index pla_prot_idx on prot_len_acc(pla_prot);
create index pla_acc_idx on prot_len_acc(pla_acc);
create index pla_gene_idx on prot_len_acc(pla_gene);
update statistics for table prot_len_acc;


!echo "make incomming_GenPept unique"
select distinct * from prot_len_acc into temp tmp_pla with no log;
delete from prot_len_acc;
insert into prot_len_acc select * from tmp_pla;

drop table tmp_pla;

! echo "Check that (manual curation?)ALL existing_GenPept has sequence length"
update db_link set dblink_length = (
    select pla_len
    from prot_len_acc
    where dblink_acc_num = pla_acc
) 
where dblink_length in ('0',0,'',NULL)
and dblink_acc_num in (select pla_acc from prot_len_acc)
and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-42' -- GenPept-pp
;

-- delete all GenPept attributed links, only for purge.
-- this should normally be commented out unless there is a specific reason to 
-- clean out all GenPept links 
--! echo "delete ALL existing_GenPept-attributed links"                         
--delete from zdb_active_data
--where zactvd_zdb_id in(
--    select distinct a.dblink_zdb_id
--    from db_link a, record_attribution           
--    where a.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-42' -- GenPept-pp
--    and recattrib_data_zdb_id = a.dblink_zdb_id
--    and recattrib_source_zdb_id = 'ZDB-PUB-030924-6' -- GenPept-Pub
--);
 
! echo "Drop from consideration ALL incomming_GenPept with accessions associated with chimeric clones"
delete from prot_len_acc
where exists (
    select 1 
    from db_link, clone
    where clone_is_chimeric -- == 't'
    and clone_mrkr_zdb_id == dblink_linked_recid
    and dblink_acc_num == pla_acc
);

! echo "delete existing_GenPept-attributed links to more than one ZFIN object"
delete from zdb_active_data 
where zactvd_zdb_id in(
    select distinct a.dblink_zdb_id
    from db_link a, db_link b, record_attribution
    where a.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-42' -- GenPept-pp
    and   b.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-42' -- GenPept-pp
    and   a.dblink_acc_num = b.dblink_acc_num
    and   a.dblink_zdb_id <> b.dblink_zdb_id
    and recattrib_data_zdb_id = a.dblink_zdb_id
    and recattrib_source_zdb_id = 'ZDB-PUB-030924-6' -- GenPept-Pub
);

! echo "adopt existing_Genpept links to with NO citation (should be 0)"
insert into record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id)
select dblink_zdb_id , 'ZDB-PUB-030924-6' -- GenPept-Pub
from db_link
where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-42' -- GenPept 
and dblink_zdb_id not in (select recattrib_data_zdb_id from record_attribution)
;

! echo "Drop from consideration ALL incomming_GenPept with non-automatic curation"
delete from prot_len_acc
where exists (
    select 1 from db_link, record_attribution
    where dblink_zdb_id = recattrib_data_zdb_id
    --and recattrib_source_zdb_id ==  'ZDB-PUB-020723-5' --Manually curated data (Curation)
    and recattrib_source_zdb_id not in ('ZDB-PUB-020723-3','ZDB-PUB-030924-6') -- LL & GP
    and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-42' -- GenPept
    and dblink_acc_num = pla_prot
);   

! echo "Drop from consideration ALL incomming_GenPept with Locus Link curation"
delete from prot_len_acc
where exists (
    select 1 from db_link, record_attribution
    where dblink_zdb_id = recattrib_data_zdb_id
    and recattrib_source_zdb_id = 'ZDB-PUB-020723-3'    -- Locus Link curated data (Curation)
    and dblink_fdbcont_zdb_id =   'ZDB-FDBCONT-040412-42' -- GenPept-pp
    and dblink_acc_num = pla_prot
);

! echo "Drop from consideration ALL incomming_GenPept with matching existing_RefPept"
delete from prot_len_acc
where exists (
    select 1 from db_link gp
    where gp.dblink_fdbcont_zdb_id  = 'ZDB-FDBCONT-040412-39' -- RefSeq-pp
    and gp.dblink_acc_num = pla_prot 
);


! echo "Drop from consideration ALL incomming_GenPept with matching existing_GenPept"
delete from prot_len_acc
where exists (
    select 1 from db_link gp, db_link gb, record_attribution, foreign_db_contains
    where gp.dblink_zdb_id = recattrib_data_zdb_id
    and  recattrib_source_zdb_id = 'ZDB-PUB-030924-6'     -- GenPept Pub
    and  gp.dblink_fdbcont_zdb_id  ='ZDB-FDBCONT-040412-42' -- GenPept-pp
    and  gp.dblink_acc_num = pla_prot
    
    and  gb.dblink_acc_num = pla_acc
    and  gb.dblink_fdbcont_zdb_id = fdbcont_zdb_id
    and  fdbcont_fdbdt_data_type = 'cDNA'
);


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


select count(*) cDNA_to_consider from prot_len_acc; 
select count(*) genomic_to_consider from tmp_Genomic_pla;
---------------------------------------------------------------
{
! echo "drop incomming GenPepts with existing ZFIN attribution to Genpept"
delete from zdb_active_data where zactvd_zdb_id in (
    select dblink_zdb_id 
    from db_link,record_attribution
    where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-42'
    and  recattrib_data_zdb_id = dblink_zdb_id
    and  recattrib_source_zdb_id = 'ZDB-PUB-030924-6' -- no more EMBL curation
);
}
! echo "-------------------------------------------------------------------"
! echo "-------------------------------------------------------------------"
! echo "-------------------------------------------------------------------"
! echo "find the simple(protein & nt_accession unique) Genpept links to add"

create temp table tmp_dblk (
    dblink_linked_recid varchar(50),
    acc varchar(10),
    zad varchar(50),
    len integer,
    fdbcont_zdb_id varchar(50)
) with no log
;

insert into tmp_dblk select
    mrkr_zdb_id,
    pla_prot acc,
    '123456789012345678901234567890'::varchar(50) zad,
    max(pla_len) len,
    'ZDB-FDBCONT-040412-42' fdbcont_zdb_id 
from  db_link, prot_len_acc, marker, foreign_db_contains
where dblink_fdbcont_zdb_id = fdbcont_zdb_id
and   fdbcont_fdbdt_data_type = 'cDNA'
and   dblink_acc_num = pla_acc
and   mrkr_zdb_id = dblink_linked_recid
group by 1,2,5 
having count(*) = 1
;

! echo "redundancy check on incomming  v.s existing db_links"
delete from tmp_dblk 
where exists ( 
    select 1 from db_link 
    where tmp_dblk.dblink_linked_recid   = db_link.dblink_linked_recid
    and   tmp_dblk.acc = db_link.dblink_acc_num
    and   tmp_dblk.fdbcont_zdb_id = db_link.dblink_fdbcont_zdb_id
);

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
    'uncurrated ' || TODAY || ' GenPept', 
    zad, 
    acc,
    len 
from tmp_dblk
;

! echo "Attribute Genpept links to ZFIN's  automated NCBI Protein citation"
insert into record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id)
select zad ,'ZDB-PUB-030924-6' --GenPept-Pub
from tmp_dblk
;  

delete from prot_len_acc where pla_prot in (select acc from tmp_dblk);     

drop table tmp_dblk;


! echo "These are the non-unique candidates based on cDNA accession match"
! echo "Maybe the cDNA accession should only be associated with one symbol"
! echo "or the GenPept manually associated with one symbol ... or flagged as chimeric "
select
    pla_prot[1,10] genpept,
    dblink_linked_recid[1,25] zad, 
    mrkr_abbrev[1,20] symbol,
    pla_acc nt_acc
from  db_link, prot_len_acc,foreign_db_contains, marker
where dblink_fdbcont_zdb_id = fdbcont_zdb_id
and   fdbcont_fdbdt_data_type = 'cDNA'
and   dblink_acc_num = pla_acc
and   mrkr_zdb_id = dblink_linked_recid
and   mrkr_type in ('GENE','EST','CDNA')
group by 1,2,3,4
order by 3
;

-------------------------------------------------------------------------------
! echo "these are the proteins that did not find a GENE,cDNA or EST" 
! echo "check v.s. everything  else cept big clones BAC,PAC,YAC," 
! echo "we would have to pull the GenPept link forward to the Gene"

select 
    pla_prot[1,10] genpept,
    dblink_linked_recid[1,25] zad, 
    mrkr_abbrev[1,20] symbol
from  db_link est, prot_len_acc, foreign_db_contains, marker
where est.dblink_fdbcont_zdb_id = fdbcont_zdb_id
and   fdbcont_fdbdt_data_type = 'cDNA'
and   est.dblink_acc_num = pla_acc
and   mrkr_type not in ('GENE','BAC','PAC','YAC','EST','CDNA')
and   mrkr_zdb_id = est.dblink_linked_recid
group by 1,2,3
into temp tmp_dblk
 with no log;

update tmp_dblk set zad = get_id('DBLINK');
! echo "ODDITIES -- not loaded"
select * from tmp_dblk order by 1,3;
{
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
};   
drop table tmp_dblk; 


-------------------------------------------------------------------------------
-- Protein derived from Genomic DNA or alt transcripts and weirdness
------------------------------------------------------------------------------- 
! echo "**********************************************************************"
! echo "now try the NON-UNIQUE proteins"
! echo "**********************************************************************"
! echo ""

select count(*) remaining_genomic from tmp_Genomic_pla;

! echo "find the new Genpept links to add where there is a SINGLE EXACT SYMBOL match"
select
     dblink_linked_recid,
     pla_prot acc,
     '123456789012345678901234567890'::varchar(50) zad, 
     max(pla_len) len,
     'ZDB-FDBCONT-040412-42' dblink_fdbcont_zdb_id
from  db_link, tmp_Genomic_pla, foreign_db_contains, marker
where dblink_fdbcont_zdb_id  = fdbcont_zdb_id
and   fdbcont_fdbdt_data_type in ('cDNA', 'Genomic')
and   dblink_acc_num = pla_acc
and   dblink_linked_recid = mrkr_zdb_id
and   pla_gene = mrkr_abbrev --- the ones with an exact name match 
and   mrkr_type in ('GENE','EST','CDNA')

group by 1,2,5
having count(*) == 1
into temp tmp_dblk with no log;


! echo "second cut"
select
    mrkr_abbrev sym, 
    pla_prot gp,
    pla_acc coded_by
from  db_link, tmp_Genomic_pla, marker, foreign_db_contains
where dblink_fdbcont_zdb_id = fdbcont_zdb_id
and   fdbcont_fdb_db_name in ('GenBank','SwissProt', 'RefSeq', 'LocusLink')
and   fdbcont_fdbdt_data_type = 'Polypeptide'
and   dblink_acc_num = pla_acc
and   pla_gene = mrkr_abbrev --- the ones with an exact name match 
and   mrkr_type in ('GENE','EST','CDNA')
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
    'ZDB-FDBCONT-040412-42' dblink_fdbcont_zdb_id,
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
        where fdbcont_fdb_db_name = 'GenPept'
    )
);

! echo "**********************************************************************"
! echo "2.5 cut -- where there is an exact symbol match but more than one gb_names to match to"

select
     dblink_linked_recid,
     pla_prot acc,
     '123456789012345678901234567890'::varchar(50) zad, 
     max(pla_len) len,
     'ZDB-FDBCONT-040412-42' dblink_fdbcont_zdb_id
from  db_link, tmp_Genomic_pla, foreign_db_contains, marker
where dblink_fdbcont_zdb_id  = fdbcont_zdb_id
and   fdbcont_fdbdt_data_type in ('cDNA', 'Genomic')
and   dblink_acc_num = pla_acc
and   dblink_linked_recid = mrkr_zdb_id
and   pla_gene = mrkr_abbrev --- the ones with an exact name match 
and   mrkr_type in ('GENE','EST','CDNA')
group by 1,2,5
having count(*) > 1
;
! echo "**********************************************************************"
! echo "look for a single exact match on previous names"
! echo "find the new Genpept links to add where there is an exact match with a previous name"
select 
     dblink_linked_recid, 
     pla_prot acc,
     '123456789012345678901234567890'::varchar(50) zad, 
     max(pla_len) len, --,pla_gene,
     'ZDB-FDBCONT-040412-42' fdbcont_zdb_id
from  db_link, tmp_Genomic_pla, marker, data_alias, foreign_db_contains
where dblink_fdbcont_zdb_id = fdbcont_zdb_id
--and   fdbcont_fdb_db_name in ('GenBank','SwissProt', 'RefSeq', 'LocusLink')
and   fdbcont_fdbdt_data_type in  ('cDNA','Genomic') -- Polypeptide'
and   dblink_acc_num = pla_acc
and   pla_gene = dalias_alias_lower --- the ones with an exact previous name match 
and   dalias_data_zdb_id = mrkr_zdb_id
and   mrkr_type in ('GENE','EST','CDNA')
and   mrkr_zdb_id = dblink_linked_recid
group by 1,2,5 
having count(*) == 1
into temp tmp_dblk with no log;

! echo "third cut"
select 
    mrkr_abbrev sym, 
    pla_prot gp,
    pla_acc coded_by
from  db_link, tmp_Genomic_pla, marker, data_alias, foreign_db_contains
where dblink_fdbcont_zdb_id = fdbcont_zdb_id
and   fdbcont_fdb_db_name in ('GenBank','SwissProt', 'RefSeq', 'LocusLink')
and   fdbcont_fdbdt_data_type = 'Polypeptide'
and   dblink_acc_num = pla_acc
and   pla_gene = dalias_alias_lower --- the ones with an exact name match 
and   dalias_data_zdb_id = mrkr_zdb_id
and   mrkr_type in ('GENE','EST','CDNA')
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
and   fdbcont_fdb_db_name in ('GenBank','SwissProt', 'RefSeq', 'LocusLink')
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
-- 
rollback work;
--commit work;

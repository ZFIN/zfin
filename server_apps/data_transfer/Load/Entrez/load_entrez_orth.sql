! echo "begin sql load `date`"
begin work;
{
drop table tmp_entrez_orth_prot;
drop table tmp_entrez_orth_name;
drop table tmp_entrez_orth_xref;
}
---------------------------------------------------------------------
-- entrez_to_protein
create table tmp_entrez_orth_prot(
	teop_taxid varchar(30),
	teop_entrez_id integer,
	teop_protein_acc varchar(50)
)
fragment by round robin in tbldbs1,tbldbs2,tbldbs3
;

! echo "import protein table"
load from 'entrez_orth_prot.unl' insert into  tmp_entrez_orth_prot ;

update tmp_entrez_orth_prot set teop_taxid = "Human" where teop_taxid = "9606";
update tmp_entrez_orth_prot set teop_taxid = "Mouse" where teop_taxid = "10090";

create index  tmp_entrez_orth_prot_eop_entrez_idx
    on  tmp_entrez_orth_prot (teop_entrez_id)
    using btree in idxdbs3
;
create index tmp_entrez_orth_prot_eop_protein_acc_idx
    on  tmp_entrez_orth_prot (teop_protein_acc)
    using btree in idxdbs2
;

update statistics high for table tmp_entrez_orth_prot;

! echo "entrez_orth_prot loaded `date`"
---------------------------------------------------------------------
-- entrez_gene

create table tmp_entrez_orth_name(
    teon_entrez_id integer, --eg_acc_num
    teon_symbol varchar(60),
    teon_name varchar(255)
)
fragment by round robin in tbldbs1,tbldbs2,tbldbs3
;

! echo "import name table"
load from 'entrez_orth_name.unl' insert into tmp_entrez_orth_name;

create unique index tmp_entrez_orth_name_eon_entrez_idx
 on tmp_entrez_orth_name(teon_entrez_id)
 using btree in idxdbs3 ;

create unique index tmp_entrez_orth_name_eon_sym_name_idx
 on tmp_entrez_orth_name(teon_entrez_id,teon_symbol,teon_name)
 using btree in idxdbs2 ;

update statistics high for table tmp_entrez_orth_name ;

! echo "need to stuff name table with NULL name records ..."
select distinct teop_entrez_id
 from tmp_entrez_orth_prot
 where teop_entrez_id not in(
    select teon_entrez_id
     from  tmp_entrez_orth_name
 )into temp tmp_eg_id with no log
;
select 2 * count(*) AK_nulls from tmp_eg_id;
insert into tmp_entrez_orth_name(teon_entrez_id)
select distinct teop_entrez_id from tmp_eg_id
;
drop table tmp_eg_id;

! echo "forget incomming names not associated with incomming proteins"
delete from tmp_entrez_orth_name
 where teon_entrez_id not in (
    select teop_entrez_id from tmp_entrez_orth_prot
);
select count(*) remaining_tname from tmp_entrez_orth_name;


update statistics high for table tmp_entrez_orth_name ;

! echo "entrez_orth_name loaded `date`"

---------------------------------------------------------------------
-- entrez_to_xref

create table tmp_entrez_orth_xref(
    teox_entrez_id integer,
    teox_xref varchar(30)
)
fragment by round robin in tbldbs1,tbldbs2,tbldbs3
;

! echo "import xref table"
load from 'entrez_orth_xref.unl' insert into tmp_entrez_orth_xref;

create index tmp_entrez_orth_xref_eox_entrez_idx
 on tmp_entrez_orth_xref(teox_entrez_id)
 using btree in idxdbs3 ;

update statistics high for table tmp_entrez_orth_xref ;

! echo "forget incomming xrefs not associated with incomming proteins"
delete from tmp_entrez_orth_xref
 where teox_entrez_id not in (
    select teop_entrez_id from tmp_entrez_orth_prot
);
select count(*) remaining_txref from tmp_entrez_orth_xref;

create index tmp_entrez_orth_xref_eox_xref_idx
 on tmp_entrez_orth_xref(teox_xref)
 using btree in idxdbs2;

update statistics high for table tmp_entrez_orth_xref ;

! echo "entrez_orth_xref loaded `date`"
! echo ""
-------------------------------------------------------------------
-------------------------------------------------------------------
-------------------------------------------------------------------

! echo "drop existing Entrez orthology that is not in current load"
! echo "expect this to cascade to protein and xref tables"

delete from entrez_gene where not exists (
   select 1 from tmp_entrez_orth_prot
    where teop_entrez_id = eg_acc_num
);
select count(*) remaining_name from entrez_gene;

! echo "orphaned orth in ZFIN dropped `date`"
! echo ""
! echo "existing symbol may have changed"
update entrez_gene set eg_symbol = (
    select teon_symbol
     from tmp_entrez_orth_name
     where teon_entrez_id = eg_acc_num
)where exists (
    select 1 from tmp_entrez_orth_name
     where teon_entrez_id = eg_acc_num
     and (teon_symbol != eg_symbol or( eg_symbol is NULL and teon_symbol is not NULL))
);

! echo "existing name may have changed"
update entrez_gene set eg_name = (
    select teon_name
     from tmp_entrez_orth_name
     where teon_entrez_id = eg_acc_num
)where exists (
    select 1 from tmp_entrez_orth_name
     where teon_entrez_id = eg_acc_num
     and (teon_name != eg_name  or( eg_name is NULL and teon_name is not NULL))
);


! echo "updated any changed entrez gene orth symbols & names in zfin `date`"
! echo ""
! echo "existing xrefs may have changed"
update entrez_to_xref set ex_xref = (
    select teox_xref
     from tmp_entrez_orth_xref
     where teox_entrez_id = ex_entrez_acc_num
)where exists (
    select 1 from tmp_entrez_orth_xref
     where teox_entrez_id = ex_entrez_acc_num
     and teox_xref != ex_xref
);
! echo "updated any changed entrez gene orth xrefs in zfin  `date`"
! echo ""

--------------------------------------------------------------------------

! echo "delete the incomming protein associations that already exist"
delete from tmp_entrez_orth_prot where exists (
   select 1 from entrez_to_protein
    where teop_entrez_id = ep_entrez_acc_num
      and teop_protein_acc = ep_protein_acc_num
      and teop_taxid = ep_organism_common_name
);
select count(*) remaining_tprot from tmp_entrez_orth_prot;

! echo "delete the incomming names that no longer have an incomming protein"
delete from tmp_entrez_orth_name
 where teon_entrez_id not in (
    select teop_entrez_id from tmp_entrez_orth_prot
);
select count(*) remaining_tname from tmp_entrez_orth_name;

! echo "delete the incomming xrefs that no longer have an incomming protein"
delete from tmp_entrez_orth_xref
 where teox_entrez_id not in (
    select teop_entrez_id from tmp_entrez_orth_prot
);
select count(*) remaining_txref from tmp_entrez_orth_xref;

! echo "existing entrez gene/prot/xref in update dropped `date`"
! echo ""

-------------------------------------------------------------------------
! echo "delete the incomming name/symbol associations that already exist WATCH FOR NULLs"

delete from tmp_entrez_orth_name where exists (
   select 1 from entrez_gene
    where teon_entrez_id = eg_acc_num
      and teon_symbol = eg_symbol
      and teon_name = eg_name
);
select count(*) remaining_tprot from tmp_entrez_orth_name;

! echo "delete the incomming  name/symbol associations that are also NULL in ZFIN"
! echo "note: this might fail if only name OR symbol are NULL"
delete from tmp_entrez_orth_name where exists (
   select 1 from entrez_gene
    where teon_entrez_id = eg_acc_num
      and teon_symbol is NULL
      and   eg_symbol is NULL
      and teon_name is NULL
      and   eg_name is NULL
);
select count(*) remaining_tname from tmp_entrez_orth_name;

! echo "delete the incomming xref associations that already exist"
delete from tmp_entrez_orth_xref where exists (
   select 1 from entrez_to_xref
    where teox_entrez_id = ex_entrez_acc_num
      and teox_xref = ex_xref
);
select count(*) remaining_txref from tmp_entrez_orth_xref;

! echo "bulk load filtered `date`"
--------------------------------------------------------------------

! echo "double check the incomming is self unique"
select teon_entrez_id from tmp_entrez_orth_name
 group by 1 having count(*) > 1
into temp tmp_dyp_name with no log
;
select * from tmp_entrez_orth_name
 where teon_entrez_id in (
 	select * from  tmp_dyp_name
 )
 order by teon_entrez_id
;

drop table tmp_dyp_name;

-- whatever is left is new
! echo "double check the incomming vs the existing"
select count(*) dup_check
 from tmp_entrez_orth_name
 where exists (
 	select 1 from entrez_gene
 	 where eg_acc_num = teon_entrez_id
 	   and eg_symbol = teon_symbol
 	   and eg_name = teon_name
 );
delete from  tmp_entrez_orth_name   
 where exists (
        select 1 from entrez_gene
         where eg_acc_num = teon_entrez_id
           and eg_symbol = teon_symbol
           and eg_name = teon_name
 );

! echo "triple check the incomming vs the existing"
select  * --count(*) dup_check
 from tmp_entrez_orth_name
 where exists (
 	select 1 from entrez_gene
 	 where eg_acc_num = teon_entrez_id
);
delete from tmp_entrez_orth_name
 where exists (
        select 1 from entrez_gene
         where eg_acc_num = teon_entrez_id
);




--select count(*) new_names from tmp_entrez_orth_name;
--select first 40 * from tmp_entrez_orth_name order by 1;
-------------

insert into entrez_gene (
	eg_acc_num,
	eg_symbol,
	eg_name
)
select distinct * from  tmp_entrez_orth_name
;
! echo "entrez gene name  updated `date`"

insert into entrez_to_protein (
	ep_organism_common_name,
	ep_entrez_acc_num,
	ep_protein_acc_num
)
select distinct * from  tmp_entrez_orth_prot
;
! echo "entrez gene prot  updated `date`"

insert into entrez_to_xref (
	ex_entrez_acc_num,
	ex_xref
)
select distinct * from  tmp_entrez_orth_xref
;
! echo "entrez gene xref updated `date`"

--------------------------------------------------------------------------
drop table tmp_entrez_orth_prot;
drop table tmp_entrez_orth_name;
drop table tmp_entrez_orth_xref;



--rollback work;
--
commit work;

! echo "sql update finished `date`"

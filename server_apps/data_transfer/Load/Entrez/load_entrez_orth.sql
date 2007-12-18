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
insert into tmp_entrez_orth_name(teon_entrez_id)
select distinct teop_entrez_id from tmp_eg_id
;
drop table tmp_eg_id;

! echo "forget names not associated with proteins"
delete from tmp_entrez_orth_name
 where teon_entrez_id not in (
    select teop_entrez_id from tmp_entrez_orth_prot
);


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

delete from tmp_entrez_orth_xref
 where teox_entrez_id not in (
    select teop_entrez_id from tmp_entrez_orth_prot
);

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
! echo "orphaned orth in ZFIN dropped `date`"


! echo "existing symbol may have changed"
update entrez_gene set eg_symbol = (
    select teon_symbol
     from tmp_entrez_orth_name
     where teon_entrez_id = eg_acc_num
)where exists (
    select 1 from tmp_entrez_orth_name
     where teon_entrez_id = eg_acc_num
     and teon_symbol != eg_symbol
);

! echo "existing name may have changed"
update entrez_gene set eg_name = (
    select teon_name
     from tmp_entrez_orth_name
     where teon_entrez_id = eg_acc_num
)where exists (
    select 1 from tmp_entrez_orth_name
     where teon_entrez_id = eg_acc_num
     and teon_name != eg_name
);

! echo "fixed entrez gene orth symbols & names in zfin updated `date`"

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
! echo "fixed entrez gene orth xrefs in zfin updated `date`"

--------------------------------------------------------------------------

! echo "delete the entrez_protein associations that already exist"
delete from tmp_entrez_orth_prot where exists (
   select 1 from entrez_to_protein
    where teop_entrez_id = ep_entrez_acc_num
      and teop_protein_acc = ep_protein_acc_num
      and teop_taxid = ep_organism_common_name
);
! echo "existing entrez gene in update dropped `date`"

! echo "delete the entrez_name associations that already exist"
delete from tmp_entrez_orth_name where exists (
   select 1 from entrez_gene
    where teon_entrez_id = eg_acc_num
      and teon_symbol = eg_symbol
      and teon_name = eg_name
);

! echo "delete the entrez_name associations that are all NULL"
delete from tmp_entrez_orth_name where exists (
   select 1 from entrez_gene
    where teon_entrez_id = eg_acc_num
      and teon_symbol is NULL
      and   eg_symbol is NULL
      and teon_name is NULL
      and   eg_name is NULL
);


! echo "delete the entrez_xref associations that already exist"
delete from tmp_entrez_orth_xref where exists (
   select 1 from entrez_to_xref
    where teox_entrez_id = ex_entrez_acc_num
      and teox_xref = ex_xref
);

! echo "bulk load filtered `date`"
--------------------------------------------------------------------
-- whatever is left is new
{
select count(*) dup_check
 from tmp_entrez_orth_name
 where exists (
 	select 1 from entrez_gene
 	 where eg_acc_num = teon_entrez_id
 	   and eg_symbol = teon_symbol
 	   and eg_name = teon_name
 );

select count(*) from tmp_entrez_orth_name;

select first 40 * from tmp_entrez_orth_name;
}

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

! echo "sql update finished `date`"

--rollback work;
--
commit work;

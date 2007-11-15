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
	teop_entrez_id varchar(50),
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
create index  tmp_entrez_orth_prot_eop_protein_acc_idx
    on  tmp_entrez_orth_prot (teop_protein_acc)
    using btree in idxdbs2
;

update statistics high for table tmp_entrez_orth_prot;


---------------------------------------------------------------------
-- entrez_gene

create table tmp_entrez_orth_name(
    teon_entrez_id varchar(50), --eg_acc_num
    teon_symbol varchar(60),
    teon_name varchar(255)
)
fragment by round robin in tbldbs1,tbldbs2,tbldbs3
;


! echo "import name table"
load from 'entrez_orth_name.unl' insert into tmp_entrez_orth_name;

! echo "forget names not associated with proteins"
delete from tmp_entrez_orth_name
 where teon_entrez_id not in (
    select teop_entrez_id from tmp_entrez_orth_prot
);

! echo "need to stuff name table with NULL name records ..."
select distinct teop_entrez_id
 from tmp_entrez_orth_prot
 where teop_entrez_id not in(
    select teon_entrez_id
     from  tmp_entrez_orth_name
 )into temp tmp_eg_id with no log
;

insert into tmp_entrez_orth_name(teon_entrez_id)
select teop_entrez_id from tmp_eg_id
;
drop table tmp_eg_id;

create unique index tmp_entrez_orth_name_eon_entrez_idx
 on tmp_entrez_orth_name(teon_entrez_id)
 using btree in idxdbs3 ;

update statistics high for table tmp_entrez_orth_name ;

---------------------------------------------------------------------
-- entrez_to_xref

create table tmp_entrez_orth_xref(
    teox_entrez_id varchar(50),
    teox_xref varchar(30)
)
fragment by round robin in tbldbs1,tbldbs2,tbldbs3
;

! echo "import xref table"
load from 'entrez_orth_xref.unl' insert into tmp_entrez_orth_xref;
create index tmp_entrez_orth_xref_eox_entrez_idx
 on tmp_entrez_orth_xref(teox_entrez_id)
 using btree in idxdbs3 ;

delete from tmp_entrez_orth_xref
 where teox_entrez_id not in (
    select teop_entrez_id from tmp_entrez_orth_prot
);
update statistics high for table tmp_entrez_orth_xref ;

-------------------------------------------------------------------
-------------------------------------------------------------------
-------------------------------------------------------------------

! echo "drop existing Entrez orthology that is not in current load"
! echo "expect this to cascade to protein and xref tables"
delete from entrez_gene where not exists (
   select 1 from tmp_entrez_orth_prot
    where teop_protein_acc = eg_acc_num
);



! echo " existing symbols may have changed"
update entrez_gene set eg_symbol = (
    select teon_symbol
     from tmp_entrez_orth_name
     where teon_entrez_id = eg_acc_num
)where exists (
    select 1 from tmp_entrez_orth_name
     where teon_entrez_id = eg_acc_num
     and teon_symbol != eg_symbol
);

! echo " existing names may have changed"
update entrez_gene set eg_name = (
    select teon_name
     from tmp_entrez_orth_name
     where teon_entrez_id = eg_acc_num
)where exists (
    select 1 from tmp_entrez_orth_name
     where teon_entrez_id = eg_acc_num
     and teon_name != eg_name
);

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


--------------------------------------------------------------------------

! echo "delete the entrez_protein associations that already exist"
delete from tmp_entrez_orth_prot where exists (
   select 1 from entrez_to_protein
    where teop_entrez_id = ep_entrez_acc_num
      and teop_protein_acc = ep_protein_acc_num
      and teop_taxid = ep_organism_common_name
);

! echo "delete the entrez_name associations that already exist"
delete from tmp_entrez_orth_name where exists (
   select 1 from entrez_gene
    where teon_entrez_id = eg_acc_num
      and teon_symbol = eg_symbol
      and teon_name = eg_name
);

! echo "delete the entrez_xref associations that already exist"
delete from tmp_entrez_orth_xref where exists (
   select 1 from entrez_to_xref
    where teox_entrez_id = ex_entrez_acc_num
      and teox_xref = ex_xref
);

--------------------------------------------------------------------
-- whatever is left is new

insert into entrez_gene (
	eg_acc_num,
	eg_symbol,
	eg_name
)
select  * from  tmp_entrez_orth_name
;

insert into entrez_to_protein (
	ep_organism_common_name,
	ep_entrez_acc_num,
	ep_protein_acc_num
)
select distinct * from  tmp_entrez_orth_prot
;

insert into entrez_to_xref (
	ex_entrez_acc_num,
	ex_xref
)
select distinct * from  tmp_entrez_orth_xref
;


--------------------------------------------------------------------------
drop table tmp_entrez_orth_prot;
drop table tmp_entrez_orth_name;
drop table tmp_entrez_orth_xref;


--rollback work;
--
commit work;

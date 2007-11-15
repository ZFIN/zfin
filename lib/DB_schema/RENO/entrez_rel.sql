begin work;

create temp table tmp_ent (taxid varchar(30), second_id varchar(50),
       	    	  	  	 third_acc varchar(50), fourth_acc varchar(50))
with no log ;


load from /research/zusers/staylor/hoover/RenoData/HMpiped
  insert into tmp_ent ;

update tmp_ent
  set taxid = "Human"
  where taxid = "9606";

update tmp_ent
  set taxid = "Mouse"
  where taxid = "10090";

--select first 1 * from tmp_ent ;

--select count(*) from tmp_ent where (taxid !="Human" and taxid !="Mouse");

--HM_txid_gene_id_acc

--!echo "second_id/entrez_id = accbk_acc_num, this is uniSTS??";
--!echo "this should be zero, as these are supposedly the related Entrez ids";
--select first 1 * 
--  from tmp_ent, accession_bank, foreign_db_contains
--  where exists (Select 'x'
--  	       	       from accession_bank
--		       where second_id = accbk_acc_num)
--  and second_id = accbk_acc_num
--  and fdbcont_zdb_id = accbk_fdbcont_zdb_id
--  and taxid = fdbcont_organism_common_name;

--!echo "the third should be a nucleotide accession" ;
--!echo "third_acc = accbk_acc_num";
--select first 1 *
---  from tmp_ent, accession_bank, foreign_db_contains
--  where exists (Select 'x'
--  	       	       from accession_bank
--		       where third_acc = accbk_acc_num)
--  and accbk_acc_num = third_acc
--  and fdbcont_zdb_id = accbk_fdbcont_zdb_id
--  and taxid = fdbcont_organism_common_name;

!echo "fourth should be the protein hit accession from Nomenclature runs"; 
!echo "fourth_acc = accbk_acc_num";
select first 1 *
  from tmp_ent, accession_bank, foreign_db_contains
  where exists (Select 'x'
  	       	       from accession_bank
		       where fourth_acc = accbk_acc_num)
 and accbk_acc_num = fourth_acc
 and fdbcont_zdb_id = accbk_fdbcont_zdb_id 
 and taxid = fdbcont_organism_common_name;

create index e_index on tmp_ent (second_id)
  using btree in idxdbs4 ;

update statistics high for table accession_bank ;
update statistics high for table tmp_ent ;

set constraints all deferred ;

insert into accession_bank (accbk_acc_num, accbk_fdbcont_zdb_id)
  select distinct second_id,fdbcont_zdb_id
    from tmp_ent, foreign_db_contains
    where exists (Select 'x' 
                        from accession_bank b
    	  		where b.accbk_acc_num = fourth_Acc
			)
   and fdbcont_fdb_db_name = 'Entrez Gene'
    and fdbcont_organism_common_name = taxid 
    and not exists (Select 'x'
    	    	   	   from accession_bank, foreign_db_contains
			   where accbk_acc_num = second_id
			   and fdbcont_zdb_id = accbk_fdbcont_zdb_id
			   and fdbcont_organism_common_name = taxid
			   and fdbcont_Fdb_db_name = 'Entrez Gene');

select count(*), accbk_fdbcont_zdb_id, accbk_acc_num 
  from accession_bank
  group by accbk_acc_num, accbk_fdbcont_zdb_id
  having count(*) > 1;

set constraints all immediate ;


insert into accession_relationship (accrel_zdb_id,
					accrel_accbk_pk_id_1,
					accrel_accbk_pk_id_2,
					accrel_accrelt_type)
select get_id("ACCREL"),
  a.accbk_pk_id,
  b.accbk_pk_id,
  case
     when taxid = 'Human'
     then 
     	  "Human Protein hit to Entrez Accession"
     when taxid = 'Mouse'
     then
	  "Mouse Protein hit to Entrez Accession"
     end
  from accession_bank a, accession_bank b, tmp_ent 
  where a.accbk_acc_num = fourth_acc 
  and b.accbk_acc_num = second_id ;

create temp table tmp_related_gene (taxid int, entrez_id varchar(50),
       	    	  		   	  symbol varchar(30),
					  s_name varchar(255))
 with no log ;

load from /research/zusers/staylor/hoover/RenoData/HM-txid_geneid_sym_name.unl
  insert into tmp_related_gene ;

create index entrez_index on tmp_related_gene (entrez_id)
  using btree in idxdbs3 ;

update statistics high for table tmp_related_gene ;

update statistics high for table tmp_ent ;

update accession_bank
  set accbk_abbreviation = (Select symbol
      			      from tmp_related_gene
			      where accbk_acc_num = entrez_id)
  where exists (select 'x' from tmp_related_gene
  	       	       where accbk_Acc_num = entrez_id);

update accession_bank
  set accbk_name = (Select s_name
      			      from tmp_related_gene
			      where accbk_acc_num = entrez_id)
  where exists (select 'x'
  	       	       from tmp_related_gene
		       where accbk_acc_num = entrez_id);

commit work ;
--rollback work ;
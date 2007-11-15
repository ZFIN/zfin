begin work ;

create temp table tmp_mim (entrez_acc varchar(50), mim varchar(50))
with no log ;

create temp table tmp_mgi (entrez_acc varchar(50), mgi varchar(50))
with no log ;

load from /research/zusers/staylor/RenoBranch2/RenoUnix/ZFIN_WWW/lib/DB_schema/RENO/entrez/geneid_mim.unl
 insert into tmp_mim;

load from /research/zusers/staylor/RenoBranch2/RenoUnix/ZFIN_WWW/lib/DB_schema/RENO/entrez/geneid_mgi.unl
 insert into tmp_mgi;

update tmp_mgi
  set mgi = replace(mgi,"MGI:","");

update tmp_mim
  set mim = replace(mim,"MIM:","");

create temp table tmp_exist_mgi_entrez_id (mgi_id varchar(50))
 with no log;

create temp table tmp_exist_mim_entrez_id (mim_id varchar(50))
 with no log;

create index mgi_mgi_index
  on tmp_mgi (mgi) using btree in idxdbs1;

create index mgi_entrez_index
  on tmp_mgi (entrez_acc) using btree in idxdbs2;

update statistics high for table tmp_mgi;

insert into tmp_Exist_mgi_entrez_id 
  select distinct (replace(mgi,"MGI:",""))
    from accession_bank, accession_relationship, tmp_mgi
      where accbk_acc_num = entrez_acc
      and accrel_accbk_pk_id_2 = accbk_pk_id;

create temp table tmp_exist_entrez_mgi (entrez_id varchar(50))
with no log ;

create temp table tmp_exist_entrez_mim (entrez_id varchar(50))
with no log ;

insert into tmp_exist_entrez_mgi 
  select accbk_acc_num
    from accession_bank, tmp_mgi
    where entrez_acc = accbk_acc_num ;

insert into tmp_exist_entrez_mim
  select accbk_acc_num
    from accession_bank, tmp_mim
    where entrez_acc = accbk_acc_num ;

set constraints all deferred ;

insert into accession_bank (accbk_acc_num, 
			    accbk_fdbcont_zdb_id)
  select distinct (replace(mgi,"MGI:","")),(select fdbcont_zdb_id 
  	 	     from foreign_db_contains
  	 	     where fdbcont_organism_common_name = 'Mouse'
		     and fdbcont_fdbdt_data_type = 'orthologue'
		     and fdbcont_fdbdt_super_type = 'orthologue'
		     and fdbcont_fdb_db_name = 'MGI')
    from tmp_mgi;

insert into accession_bank (accbk_acc_num, 
			    accbk_fdbcont_zdb_id)
  select distinct entrez_acc,(select a.fdbcont_zdb_id 
  	 	     from foreign_db_contains a
  	 	     where a.fdbcont_organism_common_name = 'Mouse'
		     and a.fdbcont_fdbdt_data_type = 'orthologue'
		     and a.fdbcont_fdbdt_super_type = 'orthologue'
		     and a.fdbcont_fdb_db_name = 'Entrez Gene')
    from tmp_mgi
    where not exists (Select 'x'
    	      	     	     from tmp_exist_entrez_mgi
			     where entrez_id = entrez_acc);


insert into accession_relationship ( 
       	    			   	accrel_accbk_pk_id_1, 
					accrel_accbk_pk_id_2, 
					accrel_accrelt_type)
  select distinct
  accrel_accbk_pk_id_1,
  b.accbk_pk_id,
  "Mouse Protein hit to MGI"
  from accession_relationship, accession_bank a, tmp_Exist_mgi_entrez_id, accession_bank b, tmp_mgi
  where a.accbk_pk_id = accrel_accbk_pk_id_2
  and a.accbk_acc_num = entrez_acc
  and b.accbk_acc_num = mgi
  and mgi = mgi_id;

insert into accession_relationship ( 
       	    			   	accrel_accbk_pk_id_1, 
					accrel_accbk_pk_id_2, 
					accrel_accrelt_type)
  select distinct
  a.accbk_pk_id,
  b.accbk_pk_id,
  "Entrez to MGI"
  from accession_bank a, tmp_mgi, accession_bank b
  where a.accbk_acc_num = entrez_acc
  and b.accbk_acc_num = mgi;


update accession_relationship 
  set accrel_zdb_id = get_id('ACCREL')
  where accrel_zdb_id is null ;


select count(*), accrel_accbk_pk_id_1, accrel_accbk_pk_id_2, accrel_accrelt_type, a.accbk_acc_num, b.accbk_acc_num
   from accession_relationship, accession_bank a, accession_bank b
   where a.accbk_pk_id = accrel_accbk_pk_id_1
and b.accbk_pk_id = accrel_accbk_pk_id_2
   group by accrel_accbk_pk_id_1, accrel_accbk_pk_id_2, accrel_accrelt_type, a.accbk_acc_num, b.accbk_acc_num
   having count(*) > 1;

------------OMIM---------------------------

create index mim_mim_index
  on tmp_mim (mim) using btree in idxdbs2;

create index mim_entrez_index
  on tmp_mim (entrez_acc) using btree in idxdbs3;

update statistics high for table tmp_mim;

insert into tmp_Exist_mim_entrez_id 
  select distinct (replace(mim,"MIM:",""))
    from accession_bank, accession_relationship, tmp_mim
      where accbk_acc_num = entrez_acc
      and accrel_accbk_pk_id_2 = accbk_pk_id
    ;

insert into accession_bank (accbk_acc_num, 
			    accbk_fdbcont_zdb_id)
  select distinct (replace(mim,"MIM:","")),(select fdbcont_zdb_id 
  	 	     from foreign_db_contains
  	 	     where fdbcont_organism_common_name = 'Human'
		     and fdbcont_fdbdt_data_type = 'orthologue'
		     and fdbcont_fdbdt_super_type = 'orthologue'
		     and fdbcont_fdb_db_name = 'OMIM')
    from tmp_mim;


insert into accession_bank (accbk_acc_num, 
			    accbk_fdbcont_zdb_id)
  select distinct entrez_acc,(select fdbcont_zdb_id 
  	 	     from foreign_db_contains
  	 	     where fdbcont_organism_common_name = 'Human'
		     and fdbcont_fdbdt_data_type = 'orthologue'
		     and fdbcont_fdbdt_super_type = 'orthologue'
		     and fdbcont_fdb_db_name = 'Entrez Gene')
    from tmp_mim
    where not exists (Select 'x'
    	      	     	     from tmp_exist_entrez_mim
			     where entrez_id = entrez_acc);


insert into accession_relationship (
       	    			   	accrel_accbk_pk_id_1, 
					accrel_accbk_pk_id_2, 
					accrel_accrelt_type)
  select distinct
  accrel_accbk_pk_id_1,
  b.accbk_pk_id,
  "Human Protein hit to OMIM"
  from accession_relationship, accession_bank a, tmp_mim, tmp_Exist_mim_entrez_id, accession_bank b
  where a.accbk_pk_id = accrel_accbk_pk_id_2
  and a.accbk_acc_num = entrez_acc
  and b.accbk_acc_num = mim
  and mim = mim_id;


insert into accession_relationship ( 
       	    			   	accrel_accbk_pk_id_1, 
					accrel_accbk_pk_id_2, 
					accrel_accrelt_type)
  select distinct
  a.accbk_pk_id,
  b.accbk_pk_id,
  "Entrez to OMIM"
  from accession_bank a, tmp_mim, accession_bank b
  where a.accbk_acc_num = entrez_acc
  and b.accbk_acc_num = mim;

update accession_relationship 
  set accrel_zdb_id = get_id('ACCREL')
  where accrel_zdb_id is null ;

select count(*), accrel_accbk_pk_id_1, accrel_accbk_pk_id_2, accrel_accrelt_type, a.accbk_acc_num, b.accbk_acc_num
   from accession_relationship, accession_bank a, accession_bank b
   where a.accbk_pk_id = accrel_accbk_pk_id_1
and b.accbk_pk_id = accrel_accbk_pk_id_2
   group by accrel_accbk_pk_id_1, accrel_accbk_pk_id_2, accrel_accrelt_type, a.accbk_acc_num, b.accbk_acc_num
   having count(*) > 1;

select count(*), accrelt_type
  from accrel_type
  group by accrelt_type
  having count(*) > 1;

set constraints all immediate ;

select * from accession_relationship, foreign_db_contains c, foreign_db_contains d, accession_bank a, accession_bank b
  where accrel_accbk_pk_id_1 = a.accbk_pk_id
  and accrel_accbk_pk_id_2 = b.accbk_pk_id
  and c.fdbcont_zdb_id = a.accbk_fdbcont_zdb_id
  and d.fdbcont_zdb_id = b.accbk_fdbcont_zdb_id
  and a.accbk_acc_num = 'Q3UGS8' ;

select count(*) from accession_relationship
where accrel_accrelt_type = 'Entrez to OMIM';

select count(*) from accession_relationship
where accrel_accrelt_type = 'Entrez to MGI';

select count(*) from accession_relationship
where accrel_accrelt_type = 'Mouse Protein hit to MGI';

select count(*) from accession_relationship
where accrel_accrelt_type = 'Human Protein hit to OMIM';

--rollback work ;
commit work ;
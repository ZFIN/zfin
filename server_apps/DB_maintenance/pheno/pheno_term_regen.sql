begin work;

create temp table pheno_term_fast_search_tmp(
	ptfs_pk_id serial8 not null ,
	ptfs_phenos_pk_id int8,
	ptfs_term_zdb_id varchar(50) not null ,
	ptfs_tag varchar(25) ,
	ptfs_is_direct_annotation boolean default 'f' , 
	ptfs_phenos_created_date datetime year to second not null ,
	ptfs_created_date datetime year to second default current year to second
	
);

insert into pheno_term_fast_search_tmp
(
   ptfs_phenos_pk_id,
   ptfs_term_zdb_id,
   ptfs_tag,
   ptfs_is_direct_annotation,
   ptfs_phenos_created_date
)
select 
   phenos_pk_id,
   phenos_entity_1_superterm_zdb_id,
   phenos_tag,
   't',
   phenos_created_date
from
   phenotype_statement
;


insert into pheno_term_fast_search_tmp
(
   ptfs_phenos_pk_id,
   ptfs_term_zdb_id,
   ptfs_tag,
   ptfs_is_direct_annotation,
   ptfs_phenos_created_date
)
select 
   phenos_pk_id,
   phenos_entity_1_subterm_zdb_id,
   phenos_tag,
   't',
   phenos_created_date
from
   phenotype_statement
where
   phenos_entity_1_subterm_zdb_id is not null
;


insert into pheno_term_fast_search_tmp
(
   ptfs_phenos_pk_id,
   ptfs_term_zdb_id,
   ptfs_tag,
   ptfs_is_direct_annotation,
   ptfs_phenos_created_date
)
select 
   phenos_pk_id,
   phenos_entity_2_superterm_zdb_id,
   phenos_tag,
   't',
   phenos_created_date
from
   phenotype_statement
where
   phenos_entity_2_superterm_zdb_id is not null
;


insert into pheno_term_fast_search_tmp
(
   ptfs_phenos_pk_id,
   ptfs_term_zdb_id,
   ptfs_tag,
   ptfs_is_direct_annotation,
   ptfs_phenos_created_date
)
select 
   phenos_pk_id,
   phenos_entity_2_subterm_zdb_id,
   phenos_tag,
   't',
   phenos_created_date
from
   phenotype_statement
where
   phenos_entity_2_subterm_zdb_id is not null
;



insert into pheno_term_fast_search_tmp
(
  ptfs_phenos_pk_id,
  ptfs_term_zdb_id,
  ptfs_tag,
  ptfs_phenos_created_date
)
select 
  phenos_pk_id,
  t2.term_zdb_id,
  phenos_tag,
  phenos_created_date
from
  phenotype_statement, all_term_contains, term t2
where
  phenos_entity_1_superterm_zdb_id = alltermcon_contained_zdb_id
  and alltermcon_container_zdb_id = t2.term_zdb_id
  
;



insert into pheno_term_fast_search_tmp
(
  ptfs_phenos_pk_id,
  ptfs_term_zdb_id,
  ptfs_tag,
  ptfs_phenos_created_date
)
select 
  phenos_pk_id,
  t2.term_zdb_id,
  phenos_tag,
  phenos_created_date
from
  phenotype_statement, all_term_contains, term t2
where
  phenos_entity_1_subterm_zdb_id = alltermcon_contained_zdb_id
  and alltermcon_container_zdb_id = t2.term_zdb_id
  
;



insert into pheno_term_fast_search_tmp
(
  ptfs_phenos_pk_id,
  ptfs_term_zdb_id,
  ptfs_tag,
  ptfs_phenos_created_date
)
select 
  phenos_pk_id,
  t2.term_zdb_id,
  phenos_tag,
  phenos_created_date
from
  phenotype_statement, all_term_contains, term t2
where
  phenos_entity_2_superterm_zdb_id = alltermcon_contained_zdb_id
  and alltermcon_container_zdb_id = t2.term_zdb_id
  
;


insert into pheno_term_fast_search_tmp
(
  ptfs_phenos_pk_id,
  ptfs_term_zdb_id,
  ptfs_tag,
  ptfs_phenos_created_date
)
select 
  phenos_pk_id,
  t2.term_zdb_id,
  phenos_tag,
  phenos_created_date
from
  phenotype_statement, all_term_contains, term t2
where
  phenos_entity_2_subterm_zdb_id = alltermcon_contained_zdb_id
  and alltermcon_container_zdb_id = t2.term_zdb_id
  
;


delete from pheno_term_fast_search;

insert into pheno_term_fast_search (
	ptfs_phenos_pk_id , 
	ptfs_term_zdb_id ,
	ptfs_tag ,
	ptfs_is_direct_annotation ,
	ptfs_phenos_created_date ,
	ptfs_created_date )
select 
	ptfs_phenos_pk_id , 
	ptfs_term_zdb_id ,
	ptfs_tag ,
	ptfs_is_direct_annotation ,
	ptfs_phenos_created_date ,
	ptfs_created_date 
from pheno_term_fast_search_tmp;	

commit work;

update statistics low for table pheno_term_fast_search;

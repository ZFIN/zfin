begin work;

delete from pheno_term_fast_search_tmp;

commit work ;

begin work ;

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
  alltermcon_container_zdb_id,
  phenos_tag,
  phenos_created_date
from
  phenotype_statement, all_term_contains
where
  phenos_entity_1_superterm_zdb_id = alltermcon_contained_zdb_id  
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
  alltermcon_container_zdb_id,
  phenos_tag,
  phenos_created_date
from
  phenotype_statement, all_term_contains
where
  phenos_entity_1_subterm_zdb_id = alltermcon_contained_zdb_id  
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
  alltermcon_container_zdb_id,
  phenos_tag,
  phenos_created_date
from
  phenotype_statement, all_term_contains
where
  phenos_entity_2_superterm_zdb_id = alltermcon_contained_zdb_id
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
  alltermcon_container_zdb_id,
  phenos_tag,
  phenos_created_date
from
  phenotype_statement, all_term_contains
where
  phenos_entity_2_subterm_zdb_id = alltermcon_contained_zdb_id  
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

update statistics high for table pheno_term_fast_search;

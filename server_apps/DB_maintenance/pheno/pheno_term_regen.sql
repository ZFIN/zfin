begin work;

delete from pheno_term_fast_search_tmp;

commit work ;

begin work ;

insert into pheno_term_fast_search_tmp
(
   ptfs_psg_id,
   ptfs_term_zdb_id,
   ptfs_tag,
   ptfs_is_direct_annotation,
   ptfs_phenos_created_date
)
select 
   psg_id,
   psg_e1a_zdb_id,
   psg_tag,
   't',
   current year to second
from
   phenotype_observation_generated
;


insert into pheno_term_fast_search_tmp
(
   ptfs_psg_id,
   ptfs_term_zdb_id,
   ptfs_tag,
   ptfs_is_direct_annotation,
   ptfs_phenos_created_date
)
select 
   psg_id,
   psg_e1b_zdb_id,
   phenos_tag,
   't',
   current year to second
from
   phenotype_observation_generated
where
   psg_e1b_zdb_id is not null
;


insert into pheno_term_fast_search_tmp
(
   ptfs_psg_id,
   ptfs_term_zdb_id,
   ptfs_tag,
   ptfs_is_direct_annotation,
   ptfs_phenos_created_date
   
)
select 
   psg_id,
   psg_e2a_zdb_id,
   phenos_tag,
   't',
   current year to second
from
   phenotype_observation_generated
where
   psg_e2a_zdb_id is not null
;


insert into pheno_term_fast_search_tmp
(
   ptfs_psg_id,
   ptfs_term_zdb_id,
   ptfs_tag,
   ptfs_is_direct_annotation,
   ptfs_phenos_created_date
)
select 
   psg_id,
   psg_e2b_zdb_id,
   phenos_tag,
   't',
   current year to second
from
   phenotype_observation_generated
where
   psg_e2b_zdb_id is not null
;



insert into pheno_term_fast_search_tmp
(
  ptfs_psg_id,
  ptfs_term_zdb_id,
  ptfs_tag,
  ptfs_phenos_created_date
)
select 
  psg_id,
  alltermcon_container_zdb_id,
  phenos_tag,
  current year to second
from
  phenotype_observation_generated, all_term_contains
where
  psg_e1a_zdb_id = alltermcon_contained_zdb_id  
;



insert into pheno_term_fast_search_tmp
(
  ptfs_psg_id,
  ptfs_term_zdb_id,
  ptfs_tag,
  ptfs_phenos_created_date
)
select 
  psg_id,
  alltermcon_container_zdb_id,
  phenos_tag,
  current year to second
from
  phenotype_observation_generated, all_term_contains
where
  psg_e1b_zdb_id = alltermcon_contained_zdb_id  
;



insert into pheno_term_fast_search_tmp
(
  ptfs_psg_id,
  ptfs_term_zdb_id,
  ptfs_tag,
  ptfs_phenos_created_date
)
select 
  psg_id,
  alltermcon_container_zdb_id,
  phenos_tag,
  current year to second
from
  phenotype_observation_generated, all_term_contains
where
  psg_e2a_zdb_id = alltermcon_contained_zdb_id
;


insert into pheno_term_fast_search_tmp
(
  ptfs_psg_id,
  ptfs_term_zdb_id,
  ptfs_tag,
  ptfs_phenos_created_date
)
select 
  psg_id,
  alltermcon_container_zdb_id,
  phenos_tag,
  current year to second
from
  phenotype_observation_generated, all_term_contains
where
  psg_e2b_zdb_id = alltermcon_contained_zdb_id  
;


delete from pheno_term_fast_search;

insert into pheno_term_fast_search (
	ptfs_psg_id , 
	ptfs_term_zdb_id ,
	ptfs_tag ,
	ptfs_is_direct_annotation ,
	ptfs_created_date )
select 
	ptfs_psg_id , 
	ptfs_term_zdb_id ,
	ptfs_tag ,
	ptfs_is_direct_annotation ,
	current year to second 
from pheno_term_fast_search_tmp;	


commit work;

update statistics high for table pheno_term_fast_search;

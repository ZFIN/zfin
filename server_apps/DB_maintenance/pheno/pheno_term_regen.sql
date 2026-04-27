-- Regenerate pheno_term_fast_search using rename-and-recreate pattern
-- to avoid deadlocks with the indexer during the swap.
-- Old tables are renamed with a timestamp suffix for later cleanup.

DO $$
DECLARE
    old_name text;
    ts text := to_char(now(), 'YYMMDDHH24MI') || '_' || substring(md5(random()::text), 1, 4);
BEGIN

drop table if exists pheno_term_fast_search_tmp;

create table pheno_term_fast_search_tmp as select * from pheno_term_fast_search where false;

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
   now()
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
   psg_tag,
   't',
   now()
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
   psg_tag,
   't',
   now()
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
   psg_tag,
   't',
   now()
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
  psg_tag,
  now()
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
  psg_tag,
  now()
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
  psg_tag,
  now()
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
  psg_tag,
  now()
from
  phenotype_observation_generated, all_term_contains
where
  psg_e2b_zdb_id = alltermcon_contained_zdb_id  
;


-- Build indexes on the staging table before the swap
create index pheno_term_fast_search_psg_id_index_transient
    on pheno_term_fast_search_tmp (ptfs_psg_id);
create index pheno_term_fast_search_term_id_index_transient
    on pheno_term_fast_search_tmp (ptfs_term_zdb_id);

-- Swap: rename old table out, promote new table

IF EXISTS (SELECT 1 FROM information_schema.tables
           WHERE table_name = 'pheno_term_fast_search' AND table_schema = 'public') THEN
    old_name := 'pheno_term_fast_search_old_' || ts;
    RAISE NOTICE 'Renaming pheno_term_fast_search to %', old_name;
    EXECUTE 'ALTER TABLE pheno_term_fast_search RENAME TO ' || old_name;
    EXECUTE 'TRUNCATE ' || old_name;

    -- Rename indexes to avoid collisions
    EXECUTE 'ALTER INDEX IF EXISTS pheno_term_fast_search_pkey RENAME TO ' || old_name || '_pkey';
    EXECUTE 'ALTER INDEX IF EXISTS pheno_term_fast_search_psg_id_index RENAME TO ' || old_name || '_psg_id_index';
    EXECUTE 'ALTER INDEX IF EXISTS pheno_term_fast_search_term_id_index RENAME TO ' || old_name || '_term_id_index';
END IF;

-- Promote staging table to live
ALTER TABLE pheno_term_fast_search_tmp RENAME TO pheno_term_fast_search;

-- Rename transient indexes to permanent names
ALTER INDEX pheno_term_fast_search_psg_id_index_transient
    RENAME TO pheno_term_fast_search_psg_id_index;
ALTER INDEX pheno_term_fast_search_term_id_index_transient
    RENAME TO pheno_term_fast_search_term_id_index;

-- Add primary key
ALTER TABLE pheno_term_fast_search ADD PRIMARY KEY (ptfs_pk_id);

-- Add foreign key constraints
ALTER TABLE pheno_term_fast_search ADD CONSTRAINT pheno_term_fast_search_psg_fk
    FOREIGN KEY (ptfs_psg_id) REFERENCES phenotype_observation_generated (psg_id);
ALTER TABLE pheno_term_fast_search ADD CONSTRAINT pheno_term_fast_search_term_fk
    FOREIGN KEY (ptfs_term_zdb_id) REFERENCES term (term_zdb_id);

END $$;

create or replace function regen_names_create_temp_tables()
returns void as $$

  -- ---------------------------------------------------------------------
  -- Creates the three temp tables used by all the regen_names functions.
  --
  -- PRECONDITIONS:
  --   regen_zdb_id_temp may already exist.
  --   regen_all_names_temp may already exist.
  --   regen_all_name_ends_temp may already exist.
  --
  -- INPUT VARS
  --   none.
  --
  -- OUTPUT VARS:
  --   none
  --
  -- RETURNS:
  --   Success: Nothing
  --   Failure: Throws whatever exception happened.
  --
  -- EFFECTS:
  --   Success:
  --     regen_zdb_id_temp exists and is empty.
  --     regen_all_names_temp exists and is empty.
  --     regen_all_name_ends_temp exists and is empty.
  --   Error:
  --     None, some, or all of the tables may exist and have data in them.
  --     transaction is not committed or rolled back.
  -- ---------------------------------------------------------------------

  begin

    -- -------------------------------------------------------------------
    --   create regen_zdb_id_temp
    -- -------------------------------------------------------------------

    drop table if exists regen_zdb_id_temp;
    create temporary table regen_zdb_id_temp  
      (
	rgnz_zdb_id		text,
        primary key (rgnz_zdb_id)
      ) ;


    -- -------------------------------------------------------------------
    --   create regen_all_names_temp
    -- -------------------------------------------------------------------    
   drop table if exists regen_all_names_temp; 
   create temporary table regen_all_names_temp
      (
	rgnallnm_name		varchar (255) not null,
	rgnallnm_zdb_id		text not null,
	rgnallnm_significance	integer not null,
	rgnallnm_precedence	varchar(80) not null,
	rgnallnm_name_lower	varchar(255) not null,
        rgnallnm_serial_id	serial8
      );


    -- -------------------------------------------------------------------
    --   create regen_all_name_ends_temp
    -- -------------------------------------------------------------------
   drop table if exists regen_all_name_ends_temp; 
   create temporary table regen_all_name_ends_temp
      (
        rgnnmend_name_end_lower    	varchar(255),
        rgnnmend_rgnallnm_serial_id	int8
      ) ;
 
  -- Collect related gene id

  -- this temp table only applies here, thus not defined
  -- in regen_names_create_temp_tables.sql
  drop table if exists regen_geno_related_gene_zdb_id_temp;
  create temporary table regen_geno_related_gene_zdb_id_temp
    (
        rgnrgz_gene_zdb_id      text,
	rgnrgz_geno_zdb_id	text
    );

  drop table if exists regen_geno_related_gene_zdb_id_distinct_temp;
  create temporary table regen_geno_related_gene_zdb_id_distinct_temp
    (
        rgnrgzd_gene_zdb_id      text,
	rgnrgzd_geno_zdb_id	 text
    );

  delete from regen_zdb_id_temp;
  delete from regen_all_names_temp;
  delete from regen_all_name_ends_temp;
  delete from regen_geno_related_gene_zdb_id_temp;
  delete from regen_geno_related_gene_zdb_id_distinct_temp;

end;

$$ LANGUAGE plpgsql;

create procedure regen_names_create_temp_tables()

  -- ---------------------------------------------------------------------
  -- Creates the three temp tables used by all the regen_names functions.
  --
  -- Considered several other options to using temp tables, but rejected 
  -- them all:
  --
  -- Considered using permanent tables for this, but rejected the idea:
  -- This would get us better debugging of regen_names* routines because 
  -- then the tables would still be around after an exception.  Didn't do 
  -- it because all the top level regen_names routines (regen_names,
  -- regen_names_fish, regen_names_locus, and regen_names_marker) need access
  -- to the tables.  Using permanent tables would allow any two routines
  -- to interfere with each other if run simultaneously.
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
    on exception in (-958, -316)
      -- Ignore these errors:
      --  -958: Temp table already exists.
      --  -316: Index name already exists.
    end exception with resume;


    -- -------------------------------------------------------------------
    --   create regen_zdb_id_temp
    -- -------------------------------------------------------------------

    create temp table regen_zdb_id_temp  
      (
	rgnz_zdb_id		varchar(50),
        primary key (rgnz_zdb_id)
      ) with NO LOG;


    -- -------------------------------------------------------------------
    --   create regen_all_names_temp
    -- -------------------------------------------------------------------    
    create temp table regen_all_names_temp
      (
	rgnallnm_name		varchar (255) not null,
	rgnallnm_zdb_id		varchar(50) not null,
	rgnallnm_significance	integer not null,
	rgnallnm_precedence	varchar(80) not null,
	rgnallnm_name_lower	varchar(255) not null
		check (rgnallnm_name_lower = lower(rgnallnm_name)),
        rgnallnm_serial_id	serial 
      ) with no log;


    -- -------------------------------------------------------------------
    --   create regen_all_name_ends_temp
    -- -------------------------------------------------------------------
    create temp table regen_all_name_ends_temp
      (
        rgnnmend_name_end_lower    	varchar(255),
        rgnnmend_rgnallnm_serial_id	integer
      ) with no log;
 
  -- Collect related gene id

  -- this temp table only applies here, thus not defined
  -- in regen_names_create_temp_tables.sql

  create temp table regen_geno_related_gene_zdb_id_temp
    (
        rgnrgz_gene_zdb_id      varchar(50),
	rgnrgz_geno_zdb_id	varchar(50)
    )with NO LOG;

  create temp table regen_geno_related_gene_zdb_id_distinct_temp
    (
        rgnrgzd_gene_zdb_id      varchar(50),
	rgnrgzd_geno_zdb_id	varchar(50)
    )with NO LOG;
end
  -- Paranoid code to delete records from the newly created tables.  Why?
  -- 
  -- The tables are not necessarily newly created.  They may have existed
  -- before this routine was called, left over from a previous invocation of a 
  -- regen_names routine by the same session.  In this case:
  --  o the create table statement will fail, then
  --  o the exception handler will be called
  --  o the exception handler will say "it's OK to fail to create the table"
  --    and resume execution.
  --
  -- Thus, we can we end up here with tables that already existed.  This routine
  -- says it returns the tables emtpy.  Therefore, we delete anything in them.

  delete from regen_zdb_id_temp;
  delete from regen_all_names_temp;
  delete from regen_all_name_ends_temp;
  delete from regen_geno_related_gene_zdb_id_temp;
  delete from regen_geno_related_gene_zdb_id_distinct_temp;
end procedure;

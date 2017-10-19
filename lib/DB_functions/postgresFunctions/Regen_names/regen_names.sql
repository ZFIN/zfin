create or replace function regen_names() 
  returns text as $success$

  -- ---------------------------------------------------------------------
  -- RETURNS:
  --   0 - Success
  --   1 - Failed because another copy of the routine is already running.
  --  -1 - Failed for some other reason.  
  --
  -- EFFECTS:
  --   Success:
  --     all_map_names and all_name_parts tables have been replaced with new
  --       versions of the tables.  
  --     If any staging tables existed from a previous run of this routine, 
  --       then they will have been dropped.

    declare namePrecedence name_precedence.nmprec_precedence%TYPE;
     	    nameSignificance integer;
     	    zdbFlagReturn integer;

    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   CREATE ALL_M_NAMES_NEW (ALL_MAP_NAMES) TABLE
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

    -- Contains all the possible names and abbreviations of markers, features,
    -- and genotypes.
    -- coincidentally are all the names that can occur in maps.

    -- Marker names come from marker(and from 
    -- accession numbers in db_link and orthlogue names/abbrevs in ortholog).
    -- Force the names to lower case.  We don't display out of this table,
    --  we only search it.
    -- 
    -- The entries in all_map_names are prioritized based on their 
    -- precedence/signifcance.  Here are the sgnificance and precedence values
    -- for the threee types of names.  This data comes from the name_precedence
    -- table.

     --   1 Current symbol            Marker
     --   2 Current name              Marker
     --   3 Clone name                Marker
     --   4 Marker relations          Marker (not used in regen_names)
     --   5 Previous name             Marker
     --   6 Ortholog                  Marker
     --   7 Accession number          Marker
     --   8 Sequence similarity       Marker
     --   9 Clone contains gene       Marker (not used in regen_names)
 
     -- 101 Genomic feature name               Genotype
     -- 102 Genomic feature abbreviation       Genotype
     -- 103 Genomic feature alias              Genotype
     -- 105 Gene symbol                        Genotype 
     -- 106 Gene name                          Genotype
     -- 107 Gene alias                         Genotype
     -- 120 Wildtype name		       Genotype
     -- 121 Genotype alias                     Genotype


  begin

    drop table if exists all_m_names_new;

    create table all_m_names_new 
      (
	-- ortho_name and mrkr_name are 255 characters long
	-- db_link.acc_num, and all the abbrev 
	-- columns are all 80 characters or less
	allmapnm_name		varchar (255) not null,
	allmapnm_zdb_id		text not null,
	allmapnm_significance	integer not null,
	allmapnm_precedence	varchar(80) not null,
	allmapnm_name_lower	varchar(255) not null
		check (allmapnm_name_lower = lower(allmapnm_name)),
        allmapnm_serial_id	serial8 not null 
      );
    
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   CREATE ALL_NAME_ENDS_NEW (ALL_NAME_ENDS) TABLE
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

    -- This table contains all possible ending substrings for every name 
    -- in all_map_names, including the full name itself.  This table is 
    -- used to speed up "name contains" searches.  Before this table existed
    -- such queries had to use 'like "%name%"' which forced a linear search
    -- of every name in all_map_names.  With this table, queries can now
    -- use 'like "name%" which will use the index.
    -- 
    -- This table does not contain trailing substrings for accession numbers.
    -- It contains only the whole accession number.
   
    drop table if exists all_name_ends_new;

    create table all_name_ends_new
      (
        allnmend_name_end_lower    	varchar(255),
        allnmend_allmapnm_serial_id	int8
      )
      ;
    



    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   create regen_zdb_id_temp, regen_all_names_temp, regen_all_name_ends_temp
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

    
    perform regen_names_create_temp_tables();
      

    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   Get Genotype into all_m_names_new
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

    -- gather names
    insert into regen_zdb_id_temp
        ( rgnz_zdb_id )
      select geno_zdb_id from genotype;

    insert into regen_zdb_id_temp
        ( rgnz_zdb_id )
      select fish_zdb_id from fish;


    -- takes regen_zdb_id_temp as input, adds recs to regen_all_names_temp
    perform regen_names_genotype_list();

    delete from regen_zdb_id_temp;


    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   Get Marker NAMES and ACCESSION NUMBERS into all_m_names_new.
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

    insert into regen_zdb_id_temp
        ( rgnz_zdb_id )
      select mrkr_zdb_id from marker;

    -- takes regen_zdb_id_temp as input, adds recs to regen_all_names_temp
    perform regen_names_marker_list();

    delete from regen_zdb_id_temp;


    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   Move from temp tables to permanent tables
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
          allmapnm_precedence, allmapnm_name_lower, allmapnm_serial_id )
      select rgnallnm_name, rgnallnm_zdb_id, rgnallnm_significance,
             rgnallnm_precedence, rgnallnm_name_lower, rgnallnm_serial_id
        from regen_all_names_temp;

    insert into all_name_ends_new
        ( allnmend_name_end_lower, allnmend_allmapnm_serial_id )
      select rgnnmend_name_end_lower, rgnnmend_rgnallnm_serial_id
        from regen_all_name_ends_temp;

    -- Be paranoid and delete everything from the temp tables.  Shouldn't
    -- need to do this, as this routine is called in it's own session
    -- and therefore the temp tables will be dropped when the routine ends.

    delete from regen_all_names_temp;
    delete from regen_all_name_ends_temp;


    -- -------------------------------------------------------------------
    --   create indexes; constraints that use them are added at the end.
    -- -------------------------------------------------------------------

 
    create unique index all_map_names_primary_key_index_transient
      on all_m_names_new (allmapnm_serial_id);
    -- alternate key
    
    create unique index allmapnm_alternate_key_index_transient
      on all_m_names_new (allmapnm_name, allmapnm_zdb_id);

    -- foreign keys
    create index allmapnm_zdb_id_index_transient
      on all_m_names_new (allmapnm_zdb_id);

    create index allmapnm_precedence_index_transient
      on all_m_names_new (allmapnm_precedence);

    -- other indexes
    create index allmapnm_name_lower_index_transient
      on all_m_names_new (allmapnm_name_lower);


    -- -------------------------------------------------------------------
    --   create indexes for all_name_ends
    -- -------------------------------------------------------------------

    create unique index all_name_ends_primary_key_index_transient
      on all_name_ends_new (allnmend_name_end_lower, allnmend_allmapnm_serial_id);

    create index allnmend_allmapnm_serial_id_index_transient
      on all_name_ends_new (allnmend_allmapnm_serial_id);


    -- --------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   Make changes visible to the world
    -- -------------------------------------------------------------------
    -- --------------------------------------------------------------------


      drop table all_name_ends;
      drop table all_map_names;

      alter table all_m_names_new rename  to all_map_names;
      alter table all_name_ends_new rename  to all_name_ends;

      alter index all_map_names_primary_key_index_transient rename to 
        all_map_names_primary_key_index;
      alter index allmapnm_alternate_key_index_transient rename
        to allmapnm_alternate_key_index;
      alter index allmapnm_zdb_id_index_transient rename
        to allmapnm_zdb_id_index;
      alter index allmapnm_precedence_index_transient rename
        to allmapnm_precedence_index;
      alter index allmapnm_name_lower_index_transient rename
        to allmapnm_name_lower_index;
      alter index all_name_ends_primary_key_index_transient rename
        to all_name_ends_primary_key_index;
      alter index allnmend_allmapnm_serial_id_index_transient rename
        to allnmend_allmapnm_serial_id_index;

      -- define constraints, indexes are defined earlier.

      alter table all_map_names add 
	primary key (allmapnm_serial_id);

      alter table all_map_names add constraint all_map_names_alternate_key
	unique (allmapnm_name, allmapnm_zdb_id);

      alter table all_map_names add constraint allmapnm_zdb_id_foreign_key
	foreign key (allmapnm_zdb_id)
        references zdb_active_data
        on delete cascade;

      alter table all_map_names add constraint allmapnm_precedence_foreign_key
	foreign key (allmapnm_precedence)
        references name_precedence;

      alter table all_name_ends add constraint all_name_ends_primary_key
	primary key (allnmend_name_end_lower, allnmend_allmapnm_serial_id);

      alter table all_name_ends add constraint allnmend_allmapnm_serial_id_foreign_key
        foreign key (allnmend_allmapnm_serial_id)
        references all_map_names
        on delete cascade;

return 'success';

end;

$success$ LANGUAGE plpgsql;

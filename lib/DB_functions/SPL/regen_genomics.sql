create dba function "informix".regen_genomics() returning integer

  -- ---------------------------------------------------------------------
  -- regen_genomics creates fast search tables related to the quick search
  -- of name fields.  (In a former life, it generated most of the fast search
  -- tables at ZFIN.)
  --
  -- This routine runs for a long time.  It plows over a lot of data.
  --
  -- It uses the general ZFIN approach for SPL routines that generate 
  -- fast search tables:
  --
  -- o Very good exception handling and debugging support.  SPL can be a 
  --   bear to debug unless you have the infrastructure in place.  See below.
  --
  -- o It does as much work as possible before doing anything that is
  --   visible outside of the script.  This means:
  --   - Script creates the output tables with names used only in this script.
  --   - At end of script, it enters a transaction, drops the existing tables,
  --     renames everything to their real names, and commits.
  --
  -- INPUT VARS:
  --   none.
  --
  -- OUTPUT VARS:
  --   none
  --
  -- RETURNS:
  --   0 - Success
  --   1 - Failed because another copy of the routine is already running.
  --  -1 - Failed for some other reason.  See 
  --       /tmp/regen_genomics_exception_<!--|DB_NAME|--> for details.
  --
  -- EFFECTS:
  --   Success:
  --     all_map_names and all_name_parts tables have been replaced with new
  --       versions of the tables.  
  --     If any staging tables existed from a previous run of this routine, 
  --       then they will have been dropped.
  --   Error:
  --     If -1 is returned then /tmp/regen_genomics_exception_<!--|DB_NAME|--> 
  --       will have been written and any staging tables used by this routine 
  --       will exist in whatever state they were in whenthe error occurred.
  --     Any changes made to permanent tables will have been rolled back.  That
  --       is, an error returns means thatthe permanent tables were not changed.
  --
  -- DEBUGGING:
  --
  -- There are several ways to debug this function.
  --
  -- 1. If this function encounters  an exception it writes the exception
  --    number and associated text out to the file 
  --
  --      /tmp/regen_genomics_exception_<!--|DB_NAME|-->.
  --
  --    This is a great place to start.  The associated text is often the
  --    name of a violated constraint, for example "u279_351".  The first
  --    number in the contraint name (in this case "279") is the table ID
  --    of the table with the violated constraint.  You can find the table
  --    name by looking in the systables table.
  --
  -- 2. Display additional messages to the /tmp/regen_genomics_exception
  --    file.  See the master exception handler code below for how this
  --    is done.  You might want to add a display message between the
  --    code for each table that is created.
  --
  -- 3. If the previous 2 approaches aren't enough then you can also turn
  --    on tracing.  Tracing produces a large volume of information and
  --    tends to run mind-numbingly slow.
  --
  --    To turn tracing on uncomment the next statement
  --
  -- set debug file to 'debug-regen';
  --
  --    This enables tracing, but doesn't turn it on.  To turn on tracing,
  --    add a "trace on;" before the first piece of code that you suspect
  --    is causing problems.  Add a "trace off;" after the last piece of
  --    code you suspect.

  --    At this point it becomes a narrowing process to figure out exactly
  --    where the problem is.  Let the function run for a while, kill it,
  --    and then look at the trace file.  If things appear OK in the 
  --    trace, move the "trace on;" to be later in the file and then rerun.
  -- ---------------------------------------------------------------------


  -- crank up the parallelism.

  set pdqpriority high;


  -- -------------------------------------------------------------------
  --   MASTER EXCEPTION HANDLER
  -- -------------------------------------------------------------------
  begin

    define exceptionMessage lvarchar;
    define sqlError integer;
    define isamError integer;
    define errorText varchar(255);
    define errorHint varchar(255);
   
    define nrows integer;	

    -- for the purpose of time testing	
    define timeMsg varchar(50);

    on exception
      set sqlError, isamError, errorText
      begin

	-- Something terrible happened while creating the new tables
	-- Get rid of them, and leave the original tables around

	on exception in (-255, -668)
	  --  255: OK to get a "Not in transaction" here, since
	  --       we might not be in a transaction when the rollback work 
	  --       below is performed.
	  --  668: OK to get a "System command not executed" here.
	  --       Is probably the result of the chmod failing because we
	  --	   are not the owner.
	end exception with resume;

        let exceptionMessage = 'echo "' || CURRENT ||
			       ' SQL Error: '  || sqlError::varchar(200) || 
			       ' ISAM Error: ' || isamError::varchar(200) ||
			       ' ErrorText: '  || errorText || 
		               ' ErrorHint: '  || errorHint ||
			       '" >> /tmp/regen_genomics_exception_<!--|DB_NAME|-->';
	system exceptionMessage;

	-- Change the mode of the regen_genomics_exception file.  This is
	-- only needed the first time it is created.  This allows us to 
	-- rerun the function from either the web page (as zfishweb) or 
	-- from dbaccess (as whoever).

	system '/bin/chmod 666 /tmp/regen_genomics_exception_<!--|DB_NAME|-->';

	-- If in a transaction, then roll it back.  Otherwise, by default
	-- exiting this exception handler will commit the transaction.
	rollback work;

	-- Don't drop the tables here.  Leave them around in an effort to
	-- figure out what went wrong.

	update zdb_flag set zflag_is_on = 'f'
		where zflag_name = "regen_genomics" 
	 	  and zflag_is_on = 't'; 
	return -1;
      end
    end exception;


    -- -------------------------------------------------------------------
    --   GRAB ZDB_FLAG
    -- -------------------------------------------------------------------

    let errorHint = "zdb_flag";
	
    update zdb_flag set zflag_is_on = 't'
	where zflag_name = "regen_genomics" 
	 and zflag_is_on = 'f';

    let nrows = DBINFO('sqlca.sqlerrd2');

    if (nrows == 0)	then
	return 1;
    end if
 			
    update zdb_flag set zflag_last_modified = CURRENT
	where zflag_name = "regen_genomics";


    -- -------------------------------------------------------------------
    --   CREATE ALL_M_NAMES_NEW (ALL_MAP_NAME) TABLE
    -- -------------------------------------------------------------------

    -- Contains all the possible names and abbreviations of markers, fish, and
    -- locii, which coincidentally are all the names that can occur in maps.
    --
    -- We should perhaps split names for markers, fish, and locii into 3 
    -- separate tables for perfromance reasons, but not today.
    --
    -- Most of the time, we recklessly insert duplicate records into 
    -- all_m_names_new as we populate it.  We remove them at the very end.
    -- The code is much easier and faster this way.
    --
    -- Marker names come from marker and locus (and from 
    -- accession numbers in db_link and orthlogue names/abbrevs in orthologue).
    -- Force the names to lower case.  We don't display out of this table,
    --  we only search it.
    -- 
    -- The entries in all_map_names are prioritized based on their signifcance.
    -- Actual marker names and symbols are the most significant, and accession
    -- numbers are the least significant.  The assignment of significance is
    -- as follows
    --
    --   1 marker abbrev
    --   2 marker name
    --   3 locus abbrev
    --   4 locus name
    --   5 marker alias 
    --   6 locus alias
    --   7 fish: allele name, locus name/wildtype name, locus abbrev, 
    --           wildtype abbrev
    --   8 fish: fish alias, locus alias, allele alias
    --   9 known correspondences for genes
    --  10 putative gene assignments
    --  11 othologue name, orthologue abbrev
    --  12 accession numbers from other databases
    --  13 sequence similarity

    let errorHint = "all_map_names";

    if (exists (select * from systables where tabname = "all_m_names_new")) then
      drop table all_m_names_new;
    end if

    create table all_m_names_new 
      (
	-- ortho_name and mrkr_name are 255 characters long
	-- locus_name, db_link.acc_num, and all the abbrev 
	-- columns are all 80 characters or less
	allmapnm_name		varchar (255) not null,
	allmapnm_zdb_id		varchar(50) not null,
	allmapnm_significance	integer not null,
	allmapnm_precedence	varchar(80) not null,
	allmapnm_name_lower	varchar(255) not null
		check (allmapnm_name_lower = lower(allmapnm_name))
      )
      fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3  
      extent size 8192 next size 8192 lock mode page;
    revoke all on all_m_names_new from "public";



    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   Get LOCUS Names into all_m_names_new
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

    -- all of these locus queries are more or less repeated in the fish
    -- names section.

    let errorHint = "Locus Abbrev";
    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select abbrev, zdb_id, 3, "Locus abbreviation", lower(abbrev)
        from locus
        where length(abbrev) > 0;  -- eliminates nulls and blanks

    let errorHint = "Locus Name";
    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select locus_name, zdb_id, 4, "Locus name", lower(locus_name)
        from locus;

    let errorHint = "Locus Aliases";
    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select dalias_alias,  dalias_data_zdb_id, 6, "Locus Previous name",
	     lower(dalias_alias)
        from data_alias, locus
        where dalias_data_zdb_id = zdb_id;



    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   Get FISH Names into all_m_names_new
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

    -- Get LOCUS names for fish.  Would like to just use the locus names
    -- already gathered into all_m_names_new (which is what we do for genes
    -- below), but we have problems with significance numbers.
    --
    -- These locus queries are more or less repeated above in the locus section.

    let errorHint = "Fish locus abbrev";
    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select l.abbrev, f.zdb_id, 7, "Locus abbreviation", lower(l.abbrev)
        from fish f, locus l
        where f.zdb_id = l.zdb_id
	  and f.name <> l.abbrev;

    let errorHint = "Fish locus aliases";
    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select dalias_alias, zdb_id, 8, "Locus Previous name", dalias_alias_lower
        from data_alias, fish
        where dalias_data_zdb_id = locus;

    let errorHint = "Fish Locus/wildtype name";
    -- cast below is needed, otherwise get blanks on end of "Locus name   "
    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select name, zdb_id, 7,
             case
	       when line_type = "mutant" then
	         "Locus name"::varchar(80)
	       else
                 "Wildtype name"::varchar(80)
	     end,
	     lower(name)
      from fish;

    let errorHint = "Fish allele";
    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select allele, zdb_id, 7, "Fish name/allele", lower(allele)
      from fish
      where allele is not NULL;

    let errorHint = "Fish wildtype abbrev";
    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select abbrev, zdb_id, 7, "Wildtype abbreviation", lower(abbrev)
        from fish
        where line_type = "wild type"
	  and abbrev <> name;

    let errorHint = "Fish aliases";
    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select dalias_alias, zdb_id, 8, "Fish Previous name", dalias_alias_lower
        from data_alias, fish
        where dalias_data_zdb_id = zdb_id;

    let errorHint = "Fish allele aliases";
    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select dalias_alias, f.zdb_id, 8, "Allele Previous name", 
	     dalias_alias_lower
        from data_alias aalias, fish f, alteration a
        where aalias.dalias_data_zdb_id = a.zdb_id
	  and a.allele = f.allele
	  and not exists
	      ( select 'x' 
		  from data_alias falias
		  where falias.dalias_data_zdb_id = f.zdb_id
		    and falias.dalias_alias = aalias.dalias_alias );



    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   Get Marker ACCESSION NUMBERS into all_m_names_new.
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

    -- Extract out accession numbers for other databases from db_links
    -- for markers.  

    -- The "distinct" below is needed because many acc_num/linked_recid
    --   combinations have an entry for GenBank and an entry for BLAST.

    let errorHint = "marker accession numbers";
    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select distinct dblink_acc_num, dblink_linked_recid, 
                      12, "Accession number", lower(dblink_acc_num)
        from db_link, marker
        where dblink_linked_recid = mrkr_zdb_id;

    let errorHint = "marker accession number aliases";
    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select dalias_alias, dblink_linked_recid, 12, "Accession number",
	     dalias_alias_lower
     from db_link, data_alias, marker
    where dalias_data_zdb_id = dblink_zdb_id 
      and dblink_linked_recid = mrkr_zdb_id;

    let errorHint = "marker orthologue accession numbers";
    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select distinct dblink_acc_num, c_gene_id, 12, "Accession number",
                      lower(dblink_acc_num)
        from db_link,  orthologue
        where dblink_linked_recid = orthologue.zdb_id;



    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   Get MARKER NAMES into all_m_names_new.
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------


    -- -------------------------------------------------------------------
    --   Get Marker locus names into all_m_names_new.
    -- -------------------------------------------------------------------

    -- For markers that have known loci, associate all locus names with 
    -- the markers.  This query takes advantage of the fact that all
    -- locus names have already been gathered in the table.

    let errorHint = "Locus names for markers";
    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select distinct allmapnm_name, cloned_gene , 9, "Locus", 
	              allmapnm_name_lower
    	from all_m_names_new, locus
    	where allmapnm_zdb_id = locus.zdb_id
    	  and cloned_gene is not null;



    -- -------------------------------------------------------------------
    --   Get orthologue names for markers into all_m_names_new.
    -- -------------------------------------------------------------------

    let errorHint = "marker orthologue abbrevs";
    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select distinct ortho_abbrev, c_gene_id, 11, "Orthologue", 
	     lower(ortho_abbrev)
        from orthologue	
        where ortho_abbrev is not null;

    let errorHint = "marker orthologue names";
    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select distinct ortho_name, c_gene_id allmapnm_zdb_id, 11, "Orthologue",
	              lower(ortho_name)
        from orthologue
        where ortho_name is not null
	  and ortho_abbrev <> ortho_name;


    -- -------------------------------------------------------------------
    --   Get aliases for markers into all_m_names_new.
    -- -------------------------------------------------------------------

    let errorHint = "marker aliases" ;
    insert into all_m_names_new
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
   	 select distinct dalias_alias, dalias_data_zdb_id , 
	    5 , "Previous name", dalias_alias_lower
    	from data_alias, alias_group, marker
    	where mrkr_zdb_id = dalias_data_zdb_id
    	and dalias_group = aliasgrp_name
    	and aliasgrp_significance = 1;

    let errorHint = "marker aliases, sequence similarities" ;
    insert into all_m_names_new
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
    	select distinct dalias_alias, dalias_data_zdb_id, 
		13 allmapnm_significance, "Sequence similarity", 
		dalias_alias_lower
    	from data_alias, alias_group, marker
   	 where mrkr_zdb_id = dalias_data_zdb_id
   	 and dalias_group = aliasgrp_name
    	and aliasgrp_significance = 2;


    -- -------------------------------------------------------------------
    --   Get putative names for markers into all_m_names_new.
    -- -------------------------------------------------------------------

    let errorHint = "marker putative names" ;
    insert into all_m_names_new
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select putgene_putative_gene_name, putgene_mrkr_zdb_id, 
	     10, "Putative name assignment",  lower(putgene_putative_gene_name)
    	from putative_non_zfin_gene; 

 	
    -- -------------------------------------------------------------------
    --   Get MARKER NAMES and SYMBOLS into all_m_names_new.
    -- -------------------------------------------------------------------

    let errorHint = "marker symbols" ;
    insert into all_m_names_new
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select mrkr_abbrev, mrkr_zdb_id, 1, "Current symbol", lower(mrkr_abbrev)
        from marker;

    let errorHint = "marker names" ;
    insert into all_m_names_new
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select mrkr_name, mrkr_zdb_id, 2, 
             case 
	       when mrkr_type in 
		 ( select mtgrpmem_mrkr_type 
		     from marker_type_group_member
		     where mtgrpmem_mrkr_type_group = "SEARCH_SEG" )
	       then "Clone name"::varchar(80) 
	       else "Current name"::varchar(80) 
	     end,
	     lower(mrkr_name)
        from marker
        where lower(mrkr_abbrev) <> lower(mrkr_name);



    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   Remove less significant duplicates from  all_m_names_new.
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

    -- if there are dupes with different significances, delete all but 
    -- the one(s) with the lowest.

    let errorHint = "all_map_names-delete_dupes";

    select allmapnm_name_lower, allmapnm_zdb_id, 
	   min(allmapnm_significance) allmapnm_significance 
      from all_m_names_new
      group by allmapnm_zdb_id, allmapnm_name_lower
      having count(*) > 1
      into temp tmp_amn_dup with no log;

    -- Index was added to tmp_amn_dup by Sierra on 2003/07/17.
    -- The optimizer in 9.3 made this necessary.  The index cut run time by 75%.
    -- Index is still needed in 9.4.
    create index t_tmp_amn_dup on tmp_amn_dup (allmapnm_zdb_id);	

    delete from all_m_names_new 
      where exists (
              select 'x'
                from tmp_amn_dup tad 
		where tad.allmapnm_name_lower   =  all_m_names_new.allmapnm_name_lower
		and   tad.allmapnm_zdb_id       =  all_m_names_new.allmapnm_zdb_id
		and   tad.allmapnm_significance <  all_m_names_new.allmapnm_significance);  
    drop table tmp_amn_dup;
	
	insert into regen_genomics_error
	select allmapnm_name, allmapnm_zdb_id, count(*)
      	  from all_m_names_new
          group by allmapnm_zdb_id, allmapnm_name
          having count(*) > 1;

    -- -------------------------------------------------------------------
    --   create indexes; constraints that use them are added at the end.
    -- -------------------------------------------------------------------

    let errorHint = "all_map_names-create_indexes";
    -- alternate key
    create unique index allmapnm_alternate_key_index_transient
      on all_m_names_new (allmapnm_name, allmapnm_zdb_id)
      fillfactor 100
      in idxdbs1;
    -- other indexes
    create index allmapnm_zdb_id_index_transient
      on all_m_names_new (allmapnm_zdb_id)
      fillfactor 100
      in idxdbs3;
    create index allmapnm_name_lower_index_transient
      on all_m_names_new (allmapnm_name_lower)
      fillfactor 100
      in idxdbs3;

    update statistics high for table all_m_names_new;



    -- --------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   Make changes visible to the world
    -- -------------------------------------------------------------------
    -- --------------------------------------------------------------------

    -- To this point, we haven't done anything visible to actual users.
    -- Now we start to make visible changes, so we enclose it all in a
    -- transaction and have an exception handler ready to roll back
    -- if an error occurs.

    begin work;

    let errorHint = "dropping & renaming old tables";
 
    begin -- local exception handler dropping, renaming, and constraints

      define esql, eisam int;

      on exception set esql, eisam
	-- Any error at this point, just rollback.  The rollback will
	-- restore all the old tables and their indices.
	rollback work;
	-- Now pass the error to the master handler to drop the new tables
	raise exception esql, eisam;
      end exception;

      on exception in (-206)
	-- ignore error when dropping a table that doesn't already exist
      end exception with resume;


      -- Now rename our new tables and indexes to have the permanent names.
      -- Also define primary keys and alternate keys.

      -- Note that the exception-handler at the top of this file is still active


      let errorHint = "rename ALL_MAP_NAMES ";

      drop table all_map_names;
      rename table all_m_names_new to all_map_names;
{
      rename index all_map_names_primary_key_index_transient
        to all_map_names_primary_key_index;
}
      rename index allmapnm_alternate_key_index_transient
        to allmapnm_alternate_key_index;
      rename index allmapnm_zdb_id_index_transient 
        to allmapnm_zdb_id_index;
      rename index allmapnm_name_lower_index_transient
        to allmapnm_name_lower_index;

      -- define constraints, indexes are defined earlier.
      -- primary key
      alter table all_map_names add constraint
	primary key (allmapnm_name, allmapnm_zdb_id)
	constraint all_map_names_primary_key;

      grant select on all_map_names to "public";


      --trace off;
    end -- Local exception handler

    commit work;

    update zdb_flag set zflag_is_on = "f"
	where zflag_name = "regen_genomics";
	  
    update zdb_flag set zflag_last_modified = CURRENT
	where zflag_name = "regen_genomics";

  end -- Global exception handler

  return 0;

end function;


grant execute on function "informix".regen_genomics () 
  to "public" as "informix";

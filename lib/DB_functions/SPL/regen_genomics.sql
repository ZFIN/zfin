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
    define namePrecedence like name_precedence.nmprec_precedence;
    define nameSignificance integer;

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
    -- -------------------------------------------------------------------
    --   CREATE ALL_M_NAMES_NEW (ALL_MAP_NAMES) TABLE
    -- -------------------------------------------------------------------
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
    -- The entries in all_map_names are prioritized based on their 
    -- precedence/signifcance.  Here are the sgnificance and precedence values
    -- for the threee types of names.  This data comes from the name_precedence
    -- table.

    --  1 Current symbol            Markers
    --  2 Current name              Markers
    --  2 Clone name                Markers
    --  3 Previous name             Markers
    --  4 Locus                     Markers

    --  4 Locus abbreviation        Fish, Locus
    --  4 Fish name/allele          Fish
    --  4 Wildtype name             Fish
    --  4 Wildtype abbreviation     Fish
    --  5 Locus name                Fish, Locus
    --  5 Fish Previous name        Fish
    --  5 Allele Previous name      Fish
    --  6 Locus Previous name       Fish, Locus

    --  7 Putative name assignment  Marker
    --  8 Orthologue                Marker
    --  9 Accession number          Marker
    -- 10 Sequence similarity       Marker
    -- 11 Clone contains gene       Marker (used elsewhere)

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
		check (allmapnm_name_lower = lower(allmapnm_name)),
        allmapnm_serial_id	serial 
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

    let errorHint = "Locus abbreviation";
    let namePrecedence = "Locus abbreviation";
    select nmprec_significance 
      into nameSignificance
      from name_precedence 
      where nmprec_precedence = namePrecedence;

    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select abbrev, zdb_id, nameSignificance, namePrecedence, lower(abbrev)
        from locus
        where length(abbrev) > 0;  -- eliminates nulls and blanks


    let errorHint = "Locus name";
    let namePrecedence = "Locus name";
    select nmprec_significance 
      into nameSignificance
      from name_precedence 
      where nmprec_precedence = namePrecedence;

    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select locus_name, zdb_id, nameSignificance, namePrecedence, 
             lower(locus_name)
        from locus;


    let errorHint = "Locus Previous name";
    let namePrecedence = "Locus Previous name";
    select nmprec_significance 
      into nameSignificance
      from name_precedence 
      where nmprec_precedence = namePrecedence;

    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select dalias_alias, dalias_data_zdb_id, nameSignificance, namePrecedence,
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

    let errorHint = "Locus abbreviation for fish";
    let namePrecedence = "Locus abbreviation";
    select nmprec_significance 
      into nameSignificance
      from name_precedence 
      where nmprec_precedence = namePrecedence;

    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select l.abbrev, f.zdb_id, nameSignificance, namePrecedence,
             lower(l.abbrev)
        from fish f, locus l
        where f.locus = l.zdb_id
	  and f.name <> l.abbrev;


    let errorHint = "Locus Previous name for fish";
    let namePrecedence = "Locus Previous name";
    select nmprec_significance 
      into nameSignificance
      from name_precedence 
      where nmprec_precedence = namePrecedence;

    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select dalias_alias, zdb_id, nameSignificance, namePrecedence, 
             dalias_alias_lower
        from data_alias, fish
        where dalias_data_zdb_id = locus;


    let errorHint = "Locus name for fish";
    let namePrecedence = "Locus name";
    select nmprec_significance 
      into nameSignificance
      from name_precedence 
      where nmprec_precedence = namePrecedence;

    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select name, zdb_id, nameSignificance, namePrecedence, lower(name)
      from fish
      where line_type = "mutant";

    let errorHint = "Wildtype name";
    let namePrecedence = "Wildtype name";
    select nmprec_significance 
      into nameSignificance
      from name_precedence 
      where nmprec_precedence = namePrecedence;

    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select name, zdb_id, nameSignificance, namePrecedence, lower(name)
      from fish
      where line_type = "wild type";


    let errorHint = "Fish name/allele";
    let namePrecedence = "Fish name/allele";
    select nmprec_significance 
      into nameSignificance
      from name_precedence 
      where nmprec_precedence = namePrecedence;

    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select allele, zdb_id, nameSignificance, namePrecedence, lower(allele)
      from fish
      where allele is not NULL;


    let errorHint = "Wildtype abbreviation";
    let namePrecedence = "Wildtype abbreviation";
    select nmprec_significance 
      into nameSignificance
      from name_precedence 
      where nmprec_precedence = namePrecedence;

    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select abbrev, zdb_id, nameSignificance, namePrecedence, lower(abbrev)
        from fish
        where line_type = "wild type"
	  and abbrev <> name;


    let errorHint = "Fish Previous name";
    let namePrecedence = "Fish Previous name";
    select nmprec_significance 
      into nameSignificance
      from name_precedence 
      where nmprec_precedence = namePrecedence;

    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select dalias_alias, zdb_id, nameSignificance, namePrecedence, 
             dalias_alias_lower
        from data_alias, fish
        where dalias_data_zdb_id = zdb_id;


    let errorHint = "Allele Previous name";
    let namePrecedence = "Allele Previous name";
    select nmprec_significance 
      into nameSignificance
      from name_precedence 
      where nmprec_precedence = namePrecedence;

    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select dalias_alias, f.zdb_id, nameSignificance, namePrecedence, 
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

    let errorHint = "Accession number";
    let namePrecedence = "Accession number";
    select nmprec_significance 
      into nameSignificance
      from name_precedence 
      where nmprec_precedence = namePrecedence;

    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select distinct dblink_acc_num, dblink_linked_recid, nameSignificance, 
                      namePrecedence, lower(dblink_acc_num)
        from db_link, marker
        where dblink_linked_recid = mrkr_zdb_id;


    let errorHint = "Accession number alias";
    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select dalias_alias, dblink_linked_recid, nameSignificance, 
             namePrecedence, dalias_alias_lower
     from db_link, data_alias, marker
    where dalias_data_zdb_id = dblink_zdb_id 
      and dblink_linked_recid = mrkr_zdb_id;


    let errorHint = "Accession number for orthologue";
    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select distinct dblink_acc_num, c_gene_id, nameSignificance, 
                      namePrecedence, lower(dblink_acc_num)
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
    let namePrecedence = "Locus";
    select nmprec_significance 
      into nameSignificance
      from name_precedence 
      where nmprec_precedence = namePrecedence;

    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select distinct allmapnm_name, cloned_gene, nameSignificance, 
                      namePrecedence, allmapnm_name_lower
    	from all_m_names_new, locus
    	where allmapnm_zdb_id = locus.zdb_id
    	  and cloned_gene is not null;



    -- -------------------------------------------------------------------
    --   Get orthologue names for markers into all_m_names_new.
    -- -------------------------------------------------------------------

    let errorHint = "Orthologue abbrev";
    let namePrecedence = "Orthologue";
    select nmprec_significance 
      into nameSignificance
      from name_precedence 
      where nmprec_precedence = namePrecedence;

    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select distinct ortho_abbrev, c_gene_id, nameSignificance, namePrecedence,
	     lower(ortho_abbrev)
        from orthologue	
        where ortho_abbrev is not null;


    let errorHint = "Orthologue name";
    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select distinct ortho_name, c_gene_id allmapnm_zdb_id, nameSignificance,
                      namePrecedence, lower(ortho_name)
        from orthologue
        where ortho_name is not null
	  and ortho_abbrev <> ortho_name;


    -- -------------------------------------------------------------------
    --   Get aliases for markers into all_m_names_new.
    -- -------------------------------------------------------------------

    let errorHint = "Previous name";
    let namePrecedence = "Previous name";
    select nmprec_significance 
      into nameSignificance
      from name_precedence 
      where nmprec_precedence = namePrecedence;

    insert into all_m_names_new
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
   	 select distinct dalias_alias, dalias_data_zdb_id, nameSignificance,
                         namePrecedence, dalias_alias_lower
    	from data_alias, alias_group, marker
    	where mrkr_zdb_id = dalias_data_zdb_id
    	and dalias_group = aliasgrp_name
    	and aliasgrp_name = "alias";

    let errorHint = "Sequence similarity";
    let namePrecedence = "Sequence similarity";
    select nmprec_significance 
      into nameSignificance
      from name_precedence 
      where nmprec_precedence = namePrecedence;

    insert into all_m_names_new
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
    	select distinct dalias_alias, dalias_data_zdb_id, nameSignificance,
                        namePrecedence, dalias_alias_lower
    	from data_alias, alias_group, marker
   	 where mrkr_zdb_id = dalias_data_zdb_id
   	 and dalias_group = aliasgrp_name
    	 and aliasgrp_name = "sequence similarity";


    -- -------------------------------------------------------------------
    --   Get putative names for markers into all_m_names_new.
    -- -------------------------------------------------------------------

    let errorHint = "Putative name assignment";
    let namePrecedence = "Putative name assignment";
    select nmprec_significance 
      into nameSignificance
      from name_precedence 
      where nmprec_precedence = namePrecedence;

    insert into all_m_names_new
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select putgene_putative_gene_name, putgene_mrkr_zdb_id, nameSignificance,
             namePrecedence, lower(putgene_putative_gene_name)
    	from putative_non_zfin_gene; 

 	
    -- -------------------------------------------------------------------
    --   Get MARKER NAMES and SYMBOLS into all_m_names_new.
    -- -------------------------------------------------------------------

    let errorHint = "Current symbol";
    let namePrecedence = "Current symbol";
    select nmprec_significance 
      into nameSignificance
      from name_precedence 
      where nmprec_precedence = namePrecedence;

    insert into all_m_names_new
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select mrkr_abbrev, mrkr_zdb_id, nameSignificance, namePrecedence, 
             lower(mrkr_abbrev)
        from marker;

    let errorHint = "Current name";
    let namePrecedence = "Current name";
    select nmprec_significance 
      into nameSignificance
      from name_precedence 
      where nmprec_precedence = namePrecedence;

    insert into all_m_names_new
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select mrkr_name, mrkr_zdb_id, nameSignificance, namePrecedence, 
	     lower(mrkr_name)
        from marker
        where lower(mrkr_abbrev) <> lower(mrkr_name)
          and not exists
              ( select 'x'
		     from marker_type_group_member
		     where mtgrpmem_mrkr_type_group = "SEARCH_SEG"
                       and mrkr_type = mtgrpmem_mrkr_type );


    let errorHint = "Clone name";
    let namePrecedence = "Clone name";
    select nmprec_significance 
      into nameSignificance
      from name_precedence 
      where nmprec_precedence = namePrecedence;

    insert into all_m_names_new
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
	  allmapnm_precedence, allmapnm_name_lower )
      select mrkr_name, mrkr_zdb_id, nameSignificance, namePrecedence,
	     lower(mrkr_name)
        from marker
        where lower(mrkr_abbrev) <> lower(mrkr_name)
          and exists
              ( select 'x'
		     from marker_type_group_member
		     where mtgrpmem_mrkr_type_group = "SEARCH_SEG"
                       and mrkr_type = mtgrpmem_mrkr_type );



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
	


    -- -------------------------------------------------------------------
    --   create indexes; constraints that use them are added at the end.
    -- -------------------------------------------------------------------

    let errorHint = "all_map_names-create_indexes";
    -- primary key
    create unique index all_map_names_primary_key_index_transient
      on all_m_names_new (allmapnm_serial_id)
      fillfactor 100
      in idxdbs1;
    -- alternate key
    create unique index allmapnm_alternate_key_index_transient
      on all_m_names_new (allmapnm_name, allmapnm_zdb_id)
      fillfactor 100
      in idxdbs1;
    -- foreign keys
    create index allmapnm_zdb_id_index_transient
      on all_m_names_new (allmapnm_zdb_id)
      fillfactor 100
      in idxdbs3;
    create index allmapnm_precedence_index_transient
      on all_m_names_new (allmapnm_precedence)
      fillfactor 100
      in idxdbs3;
    -- other indexes
    create index allmapnm_name_lower_index_transient
      on all_m_names_new (allmapnm_name_lower)
      fillfactor 100
      in idxdbs3;

    update statistics high for table all_m_names_new;



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
    
    let errorHint = "all_name_ends_new";
   
    if (exists ( select * 
                   from systables 
                   where tabname = "all_name_ends_new" )) then
      drop table all_name_ends_new;
    end if

    create table all_name_ends_new
      (
        allnmend_name_end_lower    	varchar(255),
        allnmend_allmapnm_serial_id	integer
      )
      fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3  
      extent size 8192 next size 8192 lock mode page;
    revoke all on all_name_ends_new from "public";


    -- temp table to hold all the name substrings for a given ZDB ID

    create temp table current_all_name_ends_temp 
      (
        current_name_end_lower      varchar(255),
        current_allmapnm_serial_id  serial,
        current_significance    integer
      ) 
      with no log;


    -- Identical substrings for a ZDB ID can be generated from different
    -- names with different significances.  When this happens we want
    -- to associate the substring with the most signigicant name only.
    -- This temp table does that for us.

    create temp table most_significant_temp
      (
        ms_name_lower           varchar(255),
        ms_significance         integer
      )
      with no log;      


    -- -------------------------------------------------------------------
    --   Generate trailing substrings
    -- -------------------------------------------------------------------

    -- Process the names in all_map_names in groups according to their
    -- associated ZDB ID.  The pseudocode is roughly:
    --
    -- for each ZDB ID in all_map_names
    --   for each name with that ZDB ID
    --     if name is accession number
    --       store it whole in temp table
    --     else
    --       store almost every possible trailing substring of that name in 
    --         temp table, quite happily storing duplicate substrings
    --     end if
    --   end for
    --   store distinct substrings in permanent table.  Where there are
    --     duplicates, associate the substring with the most significant
    --     name the substring came from.
    -- end for
    --
    -- In the actual code the 2 for loops are combined into 1 loop 
    -- for performance reasons.
    -- 
    -- I also experimented with first loading all the substrings for all
    -- the ZDB IDs into the temp table, and then doing one big insert
    -- for all ZDB IDs into the temp table.  This turned out not to be
    -- faster and it ran out of sort space.
    --
    -- This code has been carefully tweaked for performance.  Be careful.

    begin  -- generating substrings
      define nameZdbId, prevNameZdbId like zdb_active_data.zactvd_zdb_id;
      define nameLower, nameEnd varchar(255); -- like all_m_names_new.allmapnm_name_lower;
      define nameLength integer;
      define namePrecedence varchar(80); -- like all_m_names_new.allmapnm_precedence;
      define nameSignificance integer; -- like all_m_names_new.allmapnm_significance;
      define nameSerialId integer;
      define startColumn integer;

      let prevNameZdbId = "";

      let errorHint = "Creating substrings";
      foreach
	select allmapnm_name_lower, allmapnm_zdb_id, length(allmapnm_name_lower),
	       allmapnm_serial_id, allmapnm_precedence, allmapnm_significance
	  into nameLower, nameZdbId, nameLength, nameSerialId, namePrecedence,
	       nameSignificance
	  from all_m_names_new
	  order by allmapnm_zdb_id

	if prevNameZdbId <> nameZdbId then
	  -- ZDB ID changed, empty temp table into permanent table

	  -- THIS CODE IS DUPLICATED BELOW.  MAKE CHANGES IN BOTH PLACES.

	  -- Identical substrings for a ZDB ID can be generated from different
	  -- names with different significances.  When this happens we want
	  -- to associate the substring with the most signigicant name only.

	  -- This code initially had only one insert with a subquery that
	  -- selected the min significance.  However, doing it with two
	  -- inserts instead of the subquery is over 90% faster.

	  insert into most_significant_temp
	      ( ms_name_lower, ms_significance )
	    select current_name_end_lower, min(current_significance)
	      from current_all_name_ends_temp
	      group by current_name_end_lower;

	  insert into all_name_ends_new
	      ( allnmend_name_end_lower, allnmend_allmapnm_serial_id )
	    select distinct current_name_end_lower, current_allmapnm_serial_id
	      from current_all_name_ends_temp, most_significant_temp
	      where current_significance = ms_significance
		and ms_name_lower = current_name_end_lower
		and length(ms_name_lower) = octet_length(ms_name_lower);

	   delete from most_significant_temp;
	   delete from current_all_name_ends_temp;
	end if

	if namePrecedence = "Accession number" then
	  -- name is an accession number: take it whole.
	  -- must have exact matches on accession numbers
	  insert into current_all_name_ends_temp
	      ( current_name_end_lower, current_allmapnm_serial_id, 
		current_significance )
	    values 
	       ( nameLower, nameSerialId, nameSignificance );
	else
	  -- Break the name into almost every possible trailing substring.
	  -- For the string 'ab c', code generates substrings in this order:
	  -- 'ab c'
	  --  'b c'
	  --    'c'
	  for startColumn = 1 to nameLength
	    let nameEnd = substr(nameLower, startColumn);
	    -- Don't store substrings that start with a space
	    if nameEnd[1,1] <> "" then
	      insert into current_all_name_ends_temp
		  ( current_name_end_lower, current_allmapnm_serial_id, 
		    current_significance )
		values 
		  ( nameEnd, nameSerialId, nameSignificance );
	    end if -- ignore strings that start with blanks
	  end for
	end if -- name is not an accession number.
	let prevNameZdbId = nameZdbId;

      end foreach  -- foreach record in all_map_name
    end -- end generating substrings

    -- Dump substrings for last ZDB ID into temp table.
    -- THIS CODE IS DUPLICATED ABOVE.  MAKE CHANGES IN BOTH PLACES.
    -- See code above for comments.

    let errorHint = "last insert into most_significant_temp";
    insert into most_significant_temp
        ( ms_name_lower, ms_significance )
      select current_name_end_lower, min(current_significance)
        from current_all_name_ends_temp
        group by current_name_end_lower;

    let errorHint = "last insert into all_name_ends_new";
    insert into all_name_ends_new
        ( allnmend_name_end_lower, allnmend_allmapnm_serial_id )
      select distinct current_name_end_lower, current_allmapnm_serial_id
        from current_all_name_ends_temp, most_significant_temp
        where current_significance = ms_significance
          and ms_name_lower = current_name_end_lower
          and length(ms_name_lower) = octet_length(ms_name_lower);


    -- -------------------------------------------------------------------
    --   create indexes for all_name_ends
    -- -------------------------------------------------------------------

    -- primary key
    let errorHint = "all_name_ends primary key index";
    create unique index all_name_ends_primary_key_index_transient
      on all_name_ends_new (allnmend_name_end_lower, allnmend_allmapnm_serial_id)
      fillfactor 100
      in idxdbs2;

    let errorHint = "all_name_ends serial id index";
    create index allnmend_allmapnm_serial_id_index_transient
      on all_name_ends_new (allnmend_allmapnm_serial_id)
      fillfactor 100
      in idxdbs2;

    update statistics high for table all_name_ends_new;




    -- --------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   Make changes visible to the world
    -- -------------------------------------------------------------------
    -- --------------------------------------------------------------------

    -- To this point, we haven't done anything visible to actual users.
    -- Now we start to make visible changes, so we enclose it all in a
    -- transaction and have an exception handler ready to roll back
    -- if an error occurs.
    --
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


      let errorHint = "drop tables ";
      drop table all_name_ends;
      drop table all_map_names;

      let errorHint = "rename tables ";
      rename table all_m_names_new to all_map_names;
      rename table all_name_ends_new to all_name_ends;

      let errorHint = "rename indexes";
      rename index all_map_names_primary_key_index_transient
        to all_map_names_primary_key_index;
      rename index allmapnm_alternate_key_index_transient
        to allmapnm_alternate_key_index;
      rename index allmapnm_zdb_id_index_transient 
        to allmapnm_zdb_id_index;
      rename index allmapnm_precedence_index_transient
        to allmapnm_precedence_index;
      rename index allmapnm_name_lower_index_transient
        to allmapnm_name_lower_index;
      rename index all_name_ends_primary_key_index_transient
        to all_name_ends_primary_key_index;
      rename index allnmend_allmapnm_serial_id_index_transient
        to allnmend_allmapnm_serial_id_index;

      -- define constraints, indexes are defined earlier.

      let errorHint = "all_map_names PK constraint";
      alter table all_map_names add constraint
	primary key (allmapnm_serial_id)
	constraint all_map_names_primary_key;

      let errorHint = "all_map_names AK constraint";
      alter table all_map_names add constraint
	unique (allmapnm_name, allmapnm_zdb_id)
	constraint all_map_names_alternate_key;

      let errorHint = "allmapnm_zdb_id FK constraint";
      alter table all_map_names add constraint
	foreign key (allmapnm_zdb_id)
        references zdb_active_data
        on delete cascade
	constraint allmapnm_zdb_id_foreign_key;

      let errorHint = "allmapnm_precedence FK constraint";
      alter table all_map_names add constraint
	foreign key (allmapnm_precedence)
        references name_precedence
	constraint allmapnm_precedence_foreign_key;

      let errorHint = "all_name_ends PK constraint";
      alter table all_name_ends add constraint
	primary key (allnmend_name_end_lower, allnmend_allmapnm_serial_id)
	constraint all_name_ends_primary_key;

      let errorHint = "all_name_ends FK constraint";
      alter table all_name_ends add constraint
        foreign key (allnmend_allmapnm_serial_id)
        references all_map_names
        on delete cascade
        constraint allnmend_allmapnm_serial_id_foreign_key;

      grant select on all_map_names to "public";
      grant select on all_name_ends to "public";


      --trace off;
    end -- Local exception handler

    commit work;


    -- -------------------------------------------------------------------
    --   RELEASE ZDB_FLAG
    -- -------------------------------------------------------------------

    update zdb_flag set zflag_is_on = "f"
	where zflag_name = "regen_genomics";
	  
    update zdb_flag set zflag_last_modified = CURRENT
	where zflag_name = "regen_genomics";

  end -- Global exception handler

  return 0;

end function;


grant execute on function "informix".regen_genomics () 
  to "public" as "informix";

create dba function "informix".regen_genomics() returning integer

  -- regen_genomics creates the bulk of the fast search tables in ZFIN.
  -- Fast search tables are used to speed query access from web pages.

  -- DEBUGGING:
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

  -- set debug file to 'debug-regen';

  --    This enables tracing, but doesn't turn it on.  To turn on tracing,
  --    add a "trace on;" before the first piece of code that you suspect
  --    is causing problems.  Add a "trace off;" after the last piece of
  --    code you suspect.
  --
  --    At this point it becomes a narrowing process to figure out exactly
  --    where the problem is.  Let the function run for a while, kill it,
  --    and then look at the trace file.  If things appear OK in the 
  --    trace, move the "trace on;" to be later in the file and then rerun.

  -- Create all the new tables and views.

  begin	-- master exception handler

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

	on exception in (-206, -255, -668)
	  --  206: OK to get "Table not found" here, since we might
	  --       not have created all tables at the time of the exception
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


    -- zdb_flag
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

    -- crank up the parallelism.

    set pdqpriority high;

    ----------------  all_map_names;

    let errorHint = "all_map_names";
   

    -- Contains all the possible names and abbreviations of markers and fish,
    -- which coincidentally are all the names that can occur in maps.
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

    if (exists (select * from systables where tabname = "all_m_names_new")) then
      drop table all_m_names_new;
    end if

    create table all_m_names_new 
      (
	-- ortho_name and mrkr_name are 255 characters long
	-- locus_name, db_link.acc_num, and all the abbrev 
	-- columns are all 80 characters or less
	allmapnm_name		varchar (255),
	allmapnm_zdb_id		varchar(50),
	allmapnm_significance	integer not null,
	allmapnm_precedence	varchar(80),
	allmapnm_name_lower	varchar(255) 
		check (allmapnm_name_lower = lower(allmapnm_name))
      )
      fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3  
      extent size 8192 next size 8192 lock mode page;
    revoke all on all_m_names_new from "public";

    
    -- Get name, abbrev, and aliases from marker, fish, and locus
    -- Finally get accession numbers from db_link

 
    select abbrev allmapnm_name, zdb_id allmapnm_zdb_id, 3 allmapnm_significance,
	   "Locus abbreviation"::varchar(80) allmapnm_precedence, 
	   lower(abbrev) allmapnm_name_lower
    from locus
    where length(abbrev) > 0  -- eliminates nulls and blanks
    union  
    select locus_name allmapnm_name, zdb_id allmapnm_zdb_id, 4 allmapnm_significance,
	   "Locus name"::varchar(80) allmanpnm_precedence, 
	   lower(locus_name) allmapnm_name_lower
    from locus
    union  
    select dalias_alias allmapnm_name,  dalias_data_zdb_id allmapnm_zdb_id,
	    6 allmapnm_significance, 
	    "Locus Previous name"::varchar(80) allmanpnm_precedence, 
	    lower(dalias_alias) allmapnm_name_lower
    from data_alias, locus
    where dalias_data_zdb_id = zdb_id
    into temp all_locus_names_new with no log;	
    
    -- Get all fish names.  Start with allele name
    select allele allmapnm_name, zdb_id allmapnm_zdb_id, 7 allmapnm_significance, 
	   "Fish name/allele"::varchar(80) allmanpnm_precedence, 
	   lower(allele) allmapnm_name_lower
    from fish
    where allele is not NULL
    union  -- get locus name or wildtype name
    select name allmapnm_name, zdb_id allmapnm_zdb_id, 7 allmapnm_significance,
	   case
	     when line_type = "mutant" then
	       "Locus name"::varchar(80)
	     else
	       "Wildtype name"::varchar(80)
	   end allmanpnm_precedence, 
	   lower(name) allmapnm_name_lower	
    from fish
    union  -- get locus abbrev
    select l.abbrev allmapnm_name, f.zdb_id allmapnm_zdb_id, 
	   7 allmapnm_significance,
	   "Locus abbreviation"::varchar(80) allmapnm_precedence,
   	   lower(l.abbrev) allmapnm_name_lower
      from fish f, locus l
      where f.zdb_id = l.zdb_id
	and f.name <> l.abbrev
    union  -- get wildtype abbrev
    select abbrev allmapnm_name, zdb_id allmapnm_zdb_id, 
	   7 allmapnm_significance,
	   "Wildtype abbreviation"::varchar(80) allmapnm_precedence,
   	   lower(abbrev) allmapnm_name_lower
      from fish
      where line_type = "wild type"
	and abbrev <> name
    union  -- get fish aliases
    select dalias_alias allmapnm_name, zdb_id allmapnm_zdb_id, 8 allmapnm_significance,
	   "Fish Previous name"::varchar(80) allmanpnm_precedence, 
	   lower(dalias_alias) allmapnm_name_lower
    from data_alias, fish
    where dalias_data_zdb_id = zdb_id
    union  -- get locus aliases
    select dalias_alias allmapnm_name, zdb_id allmapnm_zdb_id, 
	   8 allmapnm_significance,
	   "Locus Previous name"::varchar(80) allmanpnm_precedence, 
	   lower(dalias_alias) allmapnm_name_lower
      from data_alias, fish
      where dalias_data_zdb_id = locus
    union  -- get allele aliases
    select dalias_alias allmapnm_name, f.zdb_id allmapnm_zdb_id, 
	   8 allmapnm_significance,
	   "Allele Previous name"::varchar(80) allmanpnm_precedence, 
	   lower(dalias_alias) allmapnm_name_lower
      from data_alias aalias, fish f, alteration a
      where aalias.dalias_data_zdb_id = a.zdb_id
	and a.allele = f.allele
	and not exists
	    ( select 1 
		from data_alias falias
		where falias.dalias_data_zdb_id = f.zdb_id
		  and falias.dalias_alias = aalias.dalias_alias )
    into temp all_fish_names_new with no log;

    -- a smaller set of all_marker_names_new which is used for getting
    -- accession numbers and 	
    select mrkr_abbrev allmapnm_name, mrkr_zdb_id allmapnm_zdb_id, 1 allmapnm_significance,
	   "Current symbol"::varchar(80) allmanpnm_precedence, 
	   lower(mrkr_abbrev) allmapnm_name_lower 
    from marker
    union
    select mrkr_name allmapnm_name, mrkr_zdb_id allmapnm_zdb_id, 2 allmapnm_significance,
	   case 
	   when mrkr_type in (select mtgrpmem_mrkr_type from marker_type_group_member
				where mtgrpmem_mrkr_type_group = "SEARCH_SEG")
	        then "Clone name"::varchar(80) 
	   else
		"Current name"::varchar(80) 
	   end allmanpnm_precedence,  lower(mrkr_name) allmapnm_name_lower
    from marker
    where lower(mrkr_abbrev) <> lower(mrkr_name)	
    into temp all_marker_names_new with no log;

 

    -- Extract out accession numbers for other databases from db_links
    -- for any ZDB object that has at least one record in the all_map_names 
    -- table. Assume none of the accession numbers are already in

    -- The "distinct" below is needed because many acc_num/linked_recid
    --   combinations have an entry for Genbank and an entry for BLAST.
    -- The last <> condition is needed because somewhere in the database an
    --   accession number is already being defined as an alias.


    let errorHint = "all_map_names-accession_numbers";
   

    select dblink_acc_num as allmapnm_name, dblink_linked_recid as allmapnm_zdb_id, 12 as allmapnm_significance,
	   "Accession number"::varchar(80) as allmapnm_precedence, 
	   lower(dblink_acc_num) as allmapnm_name_lower
    from db_link, all_marker_names_new
    where dblink_linked_recid = allmapnm_zdb_id
    and lower(dblink_acc_num) <> lower(allmapnm_name)	    
    union
    select dalias_alias as allmapnm_name, dblink_linked_recid as allmapnm_zdb_id, 12 as allmapnm_significance,
	   "Accession number"::varchar(80) as allmapnm_precedence, 
	   lower(dalias_alias) as allmapnm_name_lower
     from db_link, data_alias, all_marker_names_new
    where dalias_data_zdb_id = dblink_zdb_id 
      and dblink_linked_recid = allmapnm_zdb_id
      and lower(dalias_alias) <> lower(allmapnm_name)	    
    union
    select dblink_acc_num as allmapnm_name, c_gene_id as allmapnm_zdb_id, 12 as allmapnm_significance,
	   "Accession number"::varchar(80) as allmapnm_precedence, 
	   lower(dblink_acc_num) as allmapnm_name_lower
    from db_link,  orthologue
    where dblink_linked_recid = orthologue.zdb_id		
    into temp all_acc_names_new with no log;


    let errorHint = "all_map_names-orthologue-names";

 
    select ortho_abbrev allmapnm_name, c_gene_id allmapnm_zdb_id, 
	   11 allmapnm_significance, 
	   "Orthologue"::varchar(80) allmapnm_precedence, 
	   lower(ortho_abbrev) allmapnm_name_lower
      from orthologue	
      where ortho_abbrev is not null
    UNION
    select ortho_name allmapnm_name, c_gene_id allmapnm_zdb_id, 
	   11 allmapnm_significance, 
	   "Orthologue"::varchar(80) allmapnm_precedence, 
	   lower(ortho_name) allmapnm_name_lower
      from orthologue
      where ortho_name is not null
    into temp all_ortho_names_new with no log;
    

    let errorHint = "all_map_names-insert-to-table";

    insert into all_m_names_new
    	select * from all_acc_names_new;
	
    let errorHint = "first" ;

    insert into all_m_names_new
    	select * from all_ortho_names_new;	


    insert into all_m_names_new
   	 select distinct dalias_alias, dalias_data_zdb_id , 
	    5 , "Previous name", lower(dalias_alias)
    	from data_alias, alias_group, marker
    	where mrkr_zdb_id = dalias_data_zdb_id
    	and dalias_group = aliasgrp_name
    	and aliasgrp_significance = 1;
    
    -- sequence simalarities
    insert into all_m_names_new
    	select distinct dalias_alias, dalias_data_zdb_id, 
		13 allmapnm_significance, "Sequence similarity", lower(dalias_alias)
    	from data_alias, alias_group, marker
   	 where mrkr_zdb_id = dalias_data_zdb_id
   	 and dalias_group = aliasgrp_name
    	and aliasgrp_significance = 2;

    insert into all_m_names_new
    	select distinct putgene_putative_gene_name, putgene_mrkr_zdb_id, 
	       10, "Putative name assignment",  lower(putgene_putative_gene_name)
    	from putative_non_zfin_gene; 

    insert into all_m_names_new 
    	select distinct a.allmapnm_name, cloned_gene , 9 ,
	    "Locus", lower(a.allmapnm_name)
    	from all_locus_names_new a, locus
    	where a.allmapnm_zdb_id = locus.zdb_id
    	and cloned_gene is not null;

 	
     insert into all_m_names_new 
		select distinct *  
        	from all_fish_names_new
        	where allmapnm_name <> ''
        	and  allmapnm_name is not NULL;
		
      insert into all_m_names_new
		select distinct *
        	from all_marker_names_new
        	where allmapnm_name <> ''
        	and  allmapnm_name is not NULL;

      insert into all_m_names_new
		select distinct *
		from all_locus_names_new
        	where allmapnm_name <> ''
        	and  allmapnm_name is not NULL;


   -- I do not think there is any gaurentee we havent duplicated a name-zdbid with same or different signigicance ...
   -- if there are dupes with different signigicances, delete all but the one(s) with the lowest.

        let errorHint = "all_map_names-delete_dupes";

	select  allmapnm_name_lower, allmapnm_zdb_id, min(allmapnm_significance) allmapnm_significance 
	from all_m_names_new group by 2,1 having count(*) > 1
	into temp tmp_amn_dup with no log;
        
	create index t_tmp_amn_dup on tmp_amn_dup (allmapnm_zdb_id);	

   -- staylor created an index on the temp table above 071703.  
   -- It seems that there is a difference in the optimizer in 9.3 that makes this index necessary.  
   -- Run-time before for regen_genomics.sql = >> 15 minutes. 
   -- Run-time after for regen_genomics.sql = < 4 minutes.
   -- However, this time difference does not happen for all users.  
   -- TomC and staylor experience the time delay, DaveC does not.  
   -- We have no idea why this happens, but the creation of this index seems to help for users experiencing
   -- the delay.

	delete from all_m_names_new where exists (
		select 1 from  tmp_amn_dup  tad 
		where tad.allmapnm_name_lower   =  all_m_names_new.allmapnm_name_lower
		and   tad.allmapnm_zdb_id       =  all_m_names_new.allmapnm_zdb_id
		and   tad.allmapnm_significance <  all_m_names_new.allmapnm_significance
	);  
	drop table tmp_amn_dup;
	

    let errorHint = "all_map_names-create_indexes";

    -- create indexes; constraints that use them are added at the end.

    if (exists (select *
	          from sysindexes
		  where idxname = "all_map_names_primary_key_index_b")) then
      -- use the "a" set of names
      -- primary key
      create unique index all_map_names_primary_key_index_a
	on all_m_names_new (allmapnm_name, allmapnm_zdb_id)
	fillfactor 100
	in idxdbs1;
      -- other indexes
      create index allmapnm_zdb_id_index_a
        on all_m_names_new (allmapnm_zdb_id)
	fillfactor 100
	in idxdbs3;
      create index allmapnm_name_lower_index_a
        on all_m_names_new (allmapnm_name_lower)
        fillfactor 100
        in idxdbs3;

    else
      -- primary key
      create unique index all_map_names_primary_key_index_b
	on all_m_names_new (allmapnm_name, allmapnm_zdb_id)
	fillfactor 100
	in idxdbs1;
      -- other indexes
      create index allmapnm_zdb_id_index_b
        on all_m_names_new (allmapnm_zdb_id)
	fillfactor 100
	in idxdbs3;
      create index allmapnm_name_lower_index_b
        on all_m_names_new (allmapnm_name_lower)
        fillfactor 100
        in idxdbs3;

    end if

    update statistics high for table all_m_names_new;



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


      -- Now rename our new tables to have the permanent names.
      -- Also define primary keys and alternate keys.  The indexes to support 
      -- these constraints are defined at the end of the sections that populate 
      -- the tables.

      -- Note that the exception-handler at the top of this file is still active



      -- ===== ALL_MAP_NAMES =====
      let errorHint = "rename ALL_MAP_NAMES ";

      drop table all_map_names;
      rename table all_m_names_new to all_map_names;

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

create dba function "informix".regen_fishsearch()
  returning integer

-- Creates the fish_search table, a fast search table used to quickly
-- search mutant/fish data from the web pages.

-- DEBUGGING:  Uncomment the next two statements to turn on a debugging trace.
--             (and change the first one to point to your OWN dang directory!)
-- set debug file to '/tmp/debug-regen-fish';
-- trace on;

-- Create all the new tables and views.
-- If an exception occurs here, drop all the newly-created tables

  begin	-- master exception handler

    define nrows integer;
    
    on exception
      begin
	-- Something terrible happened while creating the new table
	-- Get rid of it, and leave the original table around.

	on exception in (-206)
	  -- OK to get "Table not found" here, since we might
	  -- not have created all tables at the time of the exception
	end exception with resume;

	drop table fishsearch_new;

	update zdb_flag set zflag_is_on = 'f'
		where zflag_name = "regen_fishsearch" 
	 	  and zflag_is_on = 't'; 

	return -1;
      end
    end exception;
	
    update zdb_flag set zflag_is_on = 't'
	where zflag_name = "regen_fishsearch" 
	 and zflag_is_on = 'f';

    let nrows = DBINFO('sqlca.sqlerrd2');

    if (nrows == 0)	then
	return 1;
    end if
 			
    update zdb_flag set zflag_last_modified = CURRENT
	where zflag_name = "regen_fishsearch";
			

    -- Create a new fishsearch table under a temp name, loaded with results 
    -- of a huge join across the underlying tables.

    create table fishsearch_new 
      (
	fish_id		varchar (50), 
	name		varchar (255)
	  not null,
	fishsearch_name_order	varchar(100)
	  not null,
	line_type	varchar (30),
	abbrev		varchar (20), 
	phenotype	lvarchar(6000), 
	chrom_num	varchar(3),
	chrom_change	varchar (30), 
	comments	lvarchar, 
	allele		varchar (20),
	fishsearch_allele_order varchar(50)
	  not null,
	mutagen		varchar (20),
	pheno_keywords	lvarchar, 
	locus		varchar (50),
	gene_id		varchar (50), 
	gene_abbrev	varchar (15),
	alt_zdb_id	varchar (50)
      )
      fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3  
      extent size 1024 next size 1024 lock mode row;
    revoke all on fish_search from "public";

    -- This can take a few minutes, so be patient.


    insert into fishsearch_new 
      select a.zdb_id, a.name, fish_name_order, a.line_type, e.abbrev,
	     a.phenotype, b.chrom_num, d.chrom_change, a.comments,
	     d.allele, alt_allele_order, d.mutagen, a.pheno_keywords, d.locus,
	     'unknown'::varchar(50),'unknown'::varchar(15), d.zdb_id
	from fish a, chromosome b, int_fish_chromo c, alteration d, locus e
	where a.zdb_id = c.source_id 
	  and c.target_id = b.zdb_id 
	  and d.chrom_id = b.zdb_id 
	  and a.line_type = 'mutant' 
	  and a.locus = e.zdb_id;

    -- NULLs were put in place of the corresponding gene id and abbrev,
    -- add them in from the locus & marker tables for mutants with corresponding
    -- genes
    -- trace on;

    update fishsearch_new
      set (gene_id, gene_abbrev) = 
	    (( select mrkr_zdb_id, mrkr_abbrev 
		 from marker, locus l
		 where fishsearch_new.locus=l.zdb_id 
		   and l.cloned_gene = mrkr_zdb_id ))
      where exists 
	      ( select 'x' 
		  from marker, locus 
		  where fishsearch_new.locus = locus.zdb_id 
		    and locus.cloned_gene = mrkr_zdb_id );


    -- To this point, we haven't done anything visible to actual users.
    -- Now we start to make visible changes, so we enclose it all in a
    -- transaction and have an exception handler ready to roll back
    -- if an error occurs.

    begin work;

    -- Delete the old table.  It may not exist (if the DB has just
    -- been created), so ignore errors from the drop.

    begin -- local exception handler for dropping of original table

      on exception in (-206)
	-- ignore any table that doesn't already exist
      end exception with resume;

      drop table fish_search;

    end -- local exception handler for dropping of original table

    -- Now rename our new tables to have the permanent names.
    -- Note that the exception-handler at the top of this file is still active

    begin -- local exception handler
      define esql, eisam int;

      on exception set esql, eisam
	-- Any error at this point, just rollback.  The rollback will
	-- restore the old table and its indices.
	rollback work;
	-- Now pass the error to the master handler to drop the new table
	raise exception esql, eisam;
      end exception;

      rename table fishsearch_new to fish_search;

      -- primary key

      create unique index fish_search_primary_key_index
	on fish_search (fish_id)
	fillfactor 100
	in idxdbs3;
      alter table fish_search add constraint
	primary key (fish_id)
	  constraint fish_search_primary_key;

      -- other indexes

      create index fish_search_abbrev_index
	on fish_search (abbrev)
	fillfactor 100
	in idxdbs3;

      create index fish_search_alt_zdb_id_index 
	on fish_search (alt_zdb_id)
	fillfactor 100
	in idxdbs3;

      create index fish_search_locus_index 
	on fish_search (locus)
	fillfactor 100
	in idxdbs3;

      grant select on fish_search to "public";

    end -- Local exception handler

    commit work;

  end -- Global exception handler

  -- Update statistics on table that was just created.

  begin work;

  update statistics high for table fish_search;

  commit work;


  update zdb_flag set zflag_is_on = "f"
	where zflag_name = "regen_fishsearch";
	  
  update zdb_flag set zflag_last_modified = CURRENT
	where zflag_name = "regen_fishsearch";


  return 0;

end function;

grant execute on function "informix".regen_fishsearch () 
  to "public" as "informix";

create dba function "informix".regen_fishsearch()
  returning integer

  -- Creates the fish_search table, a fast search table used to quickly
  -- search mutant/fish data from the web pages.

  -- DEBUGGING:
  -- There are several ways to debug this function.
  --
  -- 1. If this function encounters  an exception it writes the exception
  --    number and associated text out to the file 
  --
  --      /tmp/regen_fishsearch_exception_<!--|DB_NAME|-->.
  --
  --    This is a great place to start.  The associated text is often the
  --    name of a violated constraint, for example "u279_351".  The first
  --    number in the contraint name (in this case "279") is the table ID
  --    of the table with the violated constraint.  You can find the table
  --    name by looking in the systables table.
  --
  -- 2. Display additional messages to the /tmp/regen_fishsearch_exception
  --    file.  See the master exception handler code below for how this
  --    is done.  You might want to add a display message between the
  --    code for each table that is created.
  --
  -- 3. If the previous 2 approaches aren't enough then you can also turn
  --    on tracing.  Tracing produces a large volume of information and
  --    tends to run mind-numbingly slow.
  --
  --    To turn tracing on uncomment the next statement

  -- set debug file to '/tmp/debug-regen-fish';
  -- trace on;

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
  -- If an exception occurs here, drop all the newly-created tables

  begin	-- master exception handler

    define exceptionMessage lvarchar;
    define sqlError integer;
    define isamError integer;
    define errorText varchar(255);
    define errorHint varchar(255);
    define nrows integer;
    
    on exception
      set sqlError, isamError, errorText
      begin
	-- Something terrible happened while creating the new table
	-- Get rid of it, and leave the original table around.

        on exception in (-206, -255, -668)
          --  206: OK to get "Table not found" here, since we might
          --       not have created all tables at the time of the exception
          --  255: OK to get a "Not in transaction" here, since
          --       we might not be in a transaction when the rollback work 
          --       below is performed.
          --  668: OK to get a "System command not executed" here.
          --       Is probably the result of the chmod failing because we
          --       are not the owner.
	end exception with resume;

        let exceptionMessage = 'echo "' || CURRENT ||
                               ' SQL Error: '  || sqlError::varchar(200) || 
                               ' ISAM Error: ' || isamError::varchar(200) ||
                               ' ErrorText: '  || errorText || 
                               ' ErrorHint: '  || errorHint ||
                               '" >> /tmp/regen_fishsearch_exception_<!--|DB_NAME|-->';
        system exceptionMessage;

        -- Change the mode of the regen_fishsearch_exception file.  This is
        -- only needed the first time it is created.  This allows us to 
        -- rerun the function from either the web page (as zfishweb) or 
        -- from dbaccess (as whoever).

        system '/bin/chmod 666 /tmp/regen_fishsearch_exception_<!--|DB_NAME|-->';

        -- If in a transaction, then roll it back.  Otherwise, by default
        -- exiting this exception handler will commit the transaction.
        rollback work;

        -- Don't drop the table here.  Leave it around in an effort to
        -- figure out what went wrong.

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
			
    -- crank up the parallelism.

    set pdqpriority high;

    -- Create a new fishsearch table under a temp name, loaded with results 
    -- of a huge join across the underlying tables.

    let errorHint = "dropping fishsearch_new";

    if (exists (select * from systables where tabname = "fishsearch_new")) then
      drop table fishsearch_new;
    end if

    let errorHint = "creating fishsearch_new";

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

    let errorHint = "inserting into fishsearch_new";

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

    let errorHint = "setting gene id and abbrev in fishsearch_new";

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

    let errorHint = "dropping fishsearch";

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

      let errorHint = "renaming fishsearch_new";

      rename table fishsearch_new to fish_search;

      -- primary key

      let errorHint = "creating PK";

      create unique index fish_search_primary_key_index
	on fish_search (fish_id)
	fillfactor 100
	in idxdbs3;
      alter table fish_search add constraint
	primary key (fish_id)
	  constraint fish_search_primary_key;

      -- other indexes

      let errorHint = "creating other indexes";

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

      create index fish_search_allele_index 
	on fish_search (allele)
	fillfactor 100
	in idxdbs3;

      create index fish_search_gene_id_index 
	on fish_search (gene_id)
	fillfactor 100
	in idxdbs3;

      create index fish_search_gene_abbrev_index 
	on fish_search (gene_abbrev)
	fillfactor 100
	in idxdbs3;

      grant select on fish_search to "public";

    end -- Local exception handler

    --commit work;

  --end -- Global exception handler

  -- Update statistics on table that was just created.

 -- begin work;

  update statistics high for table fish_search;

--Now that fish_search has been created, time for some updating

--now update fish search and chromosome tables with appropriate chromosome(LG)
--values.
--For this, we perform joins on mapped marker and linkage member
--on the following keys : gene_id,locus and then fish_id
--Finally the chrom num is also updated if the associated est has been mapped.

--we need to first obtain those fish that have multiple mappings and 
--filter them out.

   if (exists (select * from systables where tabname = "tmp_multimap")) then
      drop table tmp_multimap;
   end if
   create temp table tmp_multimap (
     gene_id varchar(50),
     fish_id varchar(50),
     chrom varchar(3)
   ) with no log;

   insert into tmp_multimap 
   select distinct gene_id,fish_id,or_lg 
   from fish_search,mapped_marker
   where gene_id=marker_id and chrom_num ='0';
   
   if (exists (select * from systables where tabname = "tmp_bad_fish")) then
      drop table tmp_bad_fish;
   end if
   create temp table tmp_bad_fish (
     fish_id varchar(50)) with no log;

   insert into tmp_bad_fish
   select fish_id from tmp_multimap 
   group by fish_id
   having count(fish_id) > 1;

--First update fish search doing a join with mapped marker on gene_id

   update fish_search 
   set chrom_num = 
      (select distinct or_lg 
        from mapped_marker 
          where gene_id=marker_id) 
   where gene_id in 
   (select marker_id from mapped_marker) 
   and (chrom_num like '0' or chrom_num is null) 
   and fish_id not like 'ZDB-FISH-990427-3' and
    fish_id not like 'ZDB-FISH-040824-6';

--Then update fish search doing a join with mapped marker on locus
   
   update fish_search 
   set chrom_num = 
   (select or_lg 
     from mapped_marker 
      where locus =marker_id) 
   where locus in 
   (select marker_id from mapped_marker) 
   and (chrom_num like '0' or chrom_num is null);

--Then update fish search doing a join with mapped marker on fish
   
   update fish_search 
   set chrom_num = 
   (select or_lg 
     from mapped_marker 
      where fish_id =marker_id) 
   where fish_id in 
   (select marker_id from mapped_marker) 
   and (chrom_num like '0' or chrom_num is null);

--Now update fish search doing a join with linkage member on gene_id

   update fish_search 
   set chrom_num=
   (select distinct lnkg_or_lg 
     from linkage,linkage_member 
      where gene_id =lnkgmem_member_zdb_id 
      and  lnkgmem_linkage_zdb_id=lnkg_zdb_id) 
   where gene_id in 
   (select lnkgmem_member_zdb_id from linkage_member) 
   and (chrom_num like '0' or chrom_num is null);

--Then update fish search doing a join with linkage member on locus

   update fish_search 
   set chrom_num=
   (select lnkg_or_lg 
     from linkage,linkage_member 
      where locus =lnkgmem_member_zdb_id 
      and  lnkgmem_linkage_zdb_id=lnkg_zdb_id)
   where locus in 
   (select lnkgmem_member_zdb_id from linkage_member) 
   and (chrom_num like '0' or chrom_num is null);

--Now we need to check if an allele has been mapped, if it is all alleles on
--that locus need to be assigned the same LG location.
--so create a temp table and add an entry for the LOCUS instead.

   if (exists (select * from systables where tabname = "tmp_link")) then
      drop table tmp_link;
   end if
   create temp table tmp_link (
     link varchar(50),
     fish_member varchar(50)
     ) with no log;

   insert into tmp_link 
   select * from linkage_member 
   where lnkgmem_member_zdb_id   like 'ZDB-FISH%';

   if (exists (select * from systables where tabname = "tmp_locus")) then
      drop table tmp_locus;
   end if
   create temp table tmp_locus (
    link varchar(50),
    mem_locus varchar(50),
    chrom varchar(3)
    ) with no log;

   insert into tmp_locus 
   select distinct link,locus,lnkg_or_lg from tmp_link, fish,linkage 
   where fish_member=zdb_id
   and link=lnkg_zdb_id;

   update fish_search 
   set chrom_num=
   (select distinct lnkg_or_lg 
    from linkage,tmp_locus 
    where locus =mem_locus 
      and  link=lnkg_zdb_id)
   where locus in 
   (select distinct mem_locus from tmp_locus) 
   and (chrom_num like '0' or chrom_num is null);

--Then update fish search doing a join with linkage member on fish_id
   
   update fish_search 
   set chrom_num=
   (select distinct lnkg_or_lg 
     from linkage,linkage_member 
      where fish_id =lnkgmem_member_zdb_id 
      and  lnkgmem_linkage_zdb_id=lnkg_zdb_id) 
  where fish_id in 
  (select lnkgmem_member_zdb_id from linkage_member) 
  and (chrom_num like '0' or chrom_num is null);


--put all ests that are mapped into one table(tmp_est)

   if (exists (select * from systables where tabname = "est")) then
      drop table est;
   end if
   create temp table est (
     marker_id varchar(50),
     geneid varchar(50),
     chrom varchar(3)) with no log;

   if (exists (select * from systables where tabname = "tmp_est")) then
      drop table tmp_est;
   end if
   create temp table tmp_est (
     marker_id varchar(50),
     geneid varchar(50),
     chrom varchar(3)) with no log;


    insert into tmp_est (marker_id, geneid, chrom)
    select distinct marker_id, gene_id, or_lg
    from mapped_marker,marker_relationship,fish_search 
    where gene_id =mrel_mrkr_1_zdb_id and mrel_mrkr_2_zdb_id=marker_id 
    ;

--pull out the ests that have more than one linkage location
--there should be 4 rows.

   if (exists (select * from systables where tabname = "tmp_bad_est")) then
      drop table tmp_bad_est;
   end if
   create temp table tmp_bad_est (my_count integer, 
			marker_id varchar(50),
     			geneid varchar(50)) with no log;

   insert into tmp_bad_est (my_count, marker_id, geneid)
	select count(*), marker_id, geneid
			   from tmp_est 
			   group by marker_id, geneid
			   having count(*) >1  ;


--pull records that don't have ests mapped to multiple locations
--into a table that can update fish_search--this assumes only 
--one est per gene is mapped, which is not true in several cases.

   insert into est (marker_id, geneid, chrom)
   select marker_id, geneid, chrom
   from tmp_est 
   where geneid not in (select geneid from tmp_bad_est) ;

   if (exists (select * from systables where tabname = "tmp_genes_only")) then
      drop table tmp_genes_only;
   end if

   create temp table tmp_genes_only (geneid varchar(50), chrom integer,
					my_count integer)
   with no log ;

   insert into tmp_genes_only (geneid, chrom, my_count)
   select geneid, chrom, count(*) 
     from est
     group by geneid, chrom ;

   if (exists (select * from systables where tabname = "tmp_genes_only_no_multp_ests")) then
      drop table tmp_genes_only_no_multp_ests;
   end if

   create temp table tmp_genes_no_multp_ests (geneid varchar(50))
   with no log ;

   insert into tmp_genes_no_multp_ests (geneid)
   select geneid from tmp_genes_only
   group by geneid
   having count(*) < 2 ;

update fish_search 
   set chrom_num= (select chrom
    			from tmp_genes_only
     			where gene_id = geneid
			and geneid in (select geneid 
					from tmp_genes_no_multp_ests
			)) 
   where (chrom_num like '0' or chrom_num is null) 
;

 update chromosome 
   set chrom_num=
   (select chrom_num 
     from fish_search,int_fish_chromo  
      where fish_id=source_id and zdb_id=target_id) 
   where (chrom_num like '0' or chrom_num is null);

--the fun ends here!
  commit work;
end; --end global exception handler
  update zdb_flag set zflag_is_on = "f"
	where zflag_name = "regen_fishsearch";
	  
  update zdb_flag set zflag_last_modified = CURRENT
	where zflag_name = "regen_fishsearch";

  return 0;

end function;

grant execute on function "informix".regen_fishsearch () 
  to "public" as "informix";

drop function regen_genomics; 

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
			


    ---------------- panels
    let errorHint = "panels";	
	
    if (exists (select *
	          from systables
		  where tabname = "panels_new")) then
      drop table panels_new;
    end if

    create table panels_new 
      (
	zdb_id		varchar(50), 
	entry_time	datetime year to fraction,
	name		varchar(50), 
	abbrev		varchar(20), 
	panel_date	date,
	producer	varchar(50), 
	owner		varchar(50), 
	source		varchar(50),
	comments	clob, 
	ptype		varchar(50), 
	status		varchar(10),
	disp_order	integer,
	metric		varchar(5)
	-- can't name constraints (yet) because they'll conflict with 
	-- constraints of production version of table.
      )
      in tbldbs2
      PUT comments in (smartbs1) (LOG)
      extent size 8 next size 8 lock mode page;
    revoke all on panels_new from "public";


    insert into panels_new
      select zdb_id, entry_time, panel_name, panel_abbrev, panel_date,
	     panel_producer, owner, source, comments, 'Rad_Hybrid', status,
	     disp_order, 'cR'
	from rh_panel;

    insert into panels_new
      select zdb_id, entry_time, cross_name, cross_abbrev, cross_date,
	     cross_producer, owner, source, comments, 'Meiotic', status,
	     disp_order, 'cM'
	from meiotic_panel;


    -- create indexes; constraints that use them are added at the end.

    if (exists (select *
	          from sysindexes
		  where idxname = "panels_primary_key_index_b")) then

      -- use the "a" set of names
      -- primary key
      create unique index panels_primary_key_index_a
	on panels_new (zdb_id)
	fillfactor 100
	in idxdbs3;
      -- alternate keys
      create unique index panels_name_index_a
	on panels_new (name)
	fillfactor 100
	in idxdbs3;
      create unique index panels_abbrev_index_a
	on panels_new (abbrev)
	fillfactor 100
	in idxdbs3;
    else 
      -- primary key
      create unique index panels_primary_key_index_b
	on panels_new (zdb_id)
	fillfactor 100
	in idxdbs3;
      -- alternate keys
      create unique index panels_name_index_b
	on panels_new (name)
	fillfactor 100
	in idxdbs3;
      create unique index panels_abbrev_index_b
	on panels_new (abbrev)
	fillfactor 100
	in idxdbs3;
    end if

    update statistics high for table panels_new;
 



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
    --   7 fish name, fish allele
    --   8 fish alias
    --   9 known correspondences for genes
    --  10 putative gene assignments
    --  11 othologue name, orthologue abbrev
    --  12 accession numbers from other databases

    if (exists (select * from systables where tabname = "all_m_names_new")) then
      drop table all_m_names_new;
    end if

    create table all_m_names_new 
      (
	-- ortho_name and mrkr_name are 120 characters long
	-- locus_name, db_link.acc_num, and all the abbrev 
	-- columns are all 80 characters or less
	allmapnm_name		varchar (120) 
	  check (allmapnm_name = lower(allmapnm_name)),
	allmapnm_zdb_id		varchar(50),
	allmapnm_significance	integer
	  not null
      )
      fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3  
      extent size 1024 next size 1024 lock mode page;
    revoke all on all_m_names_new from "public";

    
    -- Get name, abbrev, and aliases from marker, fish, and locus
    -- Finally get accession numbers from db_link

    --insert into all_m_names_new (allmapnm_name, allmapnm_zdb_id, allmapnm_significance)

   
    select lower(abbrev) allmapnm_name, zdb_id allmapnm_zdb_id, 3 allmapnm_significance
    from locus
    where abbrev is not NULL 
    union all
    select lower(locus_name) allmapnm_name, zdb_id allmapnm_zdb_id, 4 allmapnm_significance
    from locus
    union all 
    select lower(dalias_alias) allmapnm_name, 
	   dalias_data_zdb_id allmapnm_zdb_id, 6 allmapnm_significance
    from data_alias, locus
    where dalias_data_zdb_id = zdb_id
    into temp all_locus_names_new with no log;	
    
    select lower(allele) allmapnm_name, zdb_id allmapnm_zdb_id, 7 allmapnm_significance
    from fish
    where allele is not NULL
    union all    
    select lower(name) allmapnm_name, zdb_id allmapnm_zdb_id, 7 allmapnm_significance
    from fish
    union all
    select lower(dalias_alias) allmapnm_name, dalias_data_zdb_id allmapnm_zdb_id, 8 allmapnm_significance
    from data_alias, fish
    where dalias_data_zdb_id = zdb_id
    into temp all_fish_names_new with no log;

    -- a smaller set of all_marker_names_new which is used for getting
    -- accession numbers and 	
    select lower(mrkr_abbrev)allmapnm_name, mrkr_zdb_id allmapnm_zdb_id, 1 allmapnm_significance
    from marker
    union all
    select lower(mrkr_name) allmapnm_name, mrkr_zdb_id allmapnm_zdb_id, 2 allmapnm_significance
    from marker
    into temp all_marker_names_temp with no log;

 

    -- Finally, extract out accession numbers for other databases from db_links
    -- for any ZDB object that has at least one record in the all_map_names 
    -- table.  Some db_link records have multiple comma-separated accession 
    -- numbers per record.  Assume none of the accession numbers are already in

    -- Take the easy route with records that are not comma separated
    -- The "distinct" below is needed because many acc_num/linked_recid
    --   combinations have an entry for Genbank and an entry for BLAST.
    -- The last <> condition is needed because somewhere in the database an
    --   accession number is already being defined as an alias.

    -- ahh but all Genbank records with acc_num[9] = ','  also have two 
    -- rows in the blast one contaning each of the acc on either side of the comma
    -- so just skip the commas and be done.


    let errorHint = "all_map_names-accession_numbers";
   

    select allmapnm_name allmapnm_name, allmapnm_zdb_id allmapnm_zdb_id, min(allmapnm_significance)  allmapnm_significance 
     from all_marker_names_temp
     where allmapnm_name <> ''
     and  allmapnm_name is not NULL
     group by 1,2
     into temp all_marker_names_new with no log;		
 	
    select lower(acc_num) allmapnm_name, linked_recid allmapnm_zdb_id, 12 allmapnm_significance
    from db_link, all_marker_names_new
    where acc_num[9] <> ',' 
    and db_link.linked_recid = allmapnm_zdb_id
    and lower(acc_num) <> allmapnm_name	    
    union
    select  lower(acc_num) allmapnm_name, c_gene_id allmapnm_zdb_id, 12 allmapnm_significance
    from db_link,  orthologue
    where acc_num[9] <> ','
    and db_link.linked_recid = orthologue.zdb_id
    into temp all_acc_names_new with no log;



    let errorHint = "all_map_names-get-marker-names";


    insert into all_marker_names_new
    select distinct lower(dalias_alias) allmapnm_name, dalias_data_zdb_id allmapnm_zdb_id, 5 allmapnm_significance
    from data_alias, alias_group, marker
    where mrkr_zdb_id = dalias_data_zdb_id
    and dalias_group = aliasgrp_name
    and aliasgrp_significance = 1;
    
    -- sequence simalarities
    insert into all_marker_names_new
    select distinct lower(dalias_alias) allmapnm_name, dalias_data_zdb_id allmapnm_zdb_id, 13 allmapnm_significance
    from data_alias, alias_group, marker
    where mrkr_zdb_id = dalias_data_zdb_id
    and dalias_group = aliasgrp_name
    and aliasgrp_significance = 2;

    insert into all_marker_names_new
    select lower(putgene_putative_gene_name) allmapnm_name, putgene_mrkr_zdb_id allmapnm_zdb_id, 10 allmapnm_significance
    from putative_non_zfin_gene; 

    insert into all_marker_names_new 
    select distinct a.allmapnm_name, cloned_gene allmapnm_zdb_id, 9 allmapnm_significance
    from all_locus_names_new a, locus
    where a.allmapnm_zdb_id = locus.zdb_id
    and cloned_gene is not null;

    insert into all_marker_names_new 
    select distinct lower(ortho_name) allmapnm_name, c_gene_id allmapnm_zdb_id, 11 allmapnm_significance
    from orthologue;
    
    insert into all_marker_names_new 
    select distinct lower(ortho_abbrev) allmapnm_name, c_gene_id allmapnm_zdb_id, 11 allmapnm_significance
    from orthologue
    where ortho_abbrev is not null
    ;	

 
    let errorHint = "all_map_names-mini-significance";

	insert into all_m_names_new 
		select allmapnm_name, allmapnm_zdb_id, min(allmapnm_significance)  
        	from all_fish_names_new
        	where allmapnm_name <> ''
        	and  allmapnm_name is not NULL
        	group by 2,1;
		
	insert into all_m_names_new
		select allmapnm_name allmapnm_name, allmapnm_zdb_id allmapnm_zdb_id, min(allmapnm_significance)  allmapnm_significance 
        	from all_marker_names_new
        	where allmapnm_name <> ''
        	and  allmapnm_name is not NULL
        	group by 1,2;

	insert into all_m_names_new
		select *
		from  all_acc_names_new;

	insert into all_m_names_new
		select allmapnm_name, allmapnm_zdb_id, min(allmapnm_significance)  
		from all_locus_names_new
        	where allmapnm_name <> ''
        	and  allmapnm_name is not NULL
        	group by 2,1;

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
	in idxdbs4;
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
	in idxdbs4;
    end if

    update statistics high for table all_m_names_new;


    ----------------- all_genes
    let errorHint = "all_genes";
  
    if (exists (select *
	          from systables
		  where tabname = "all_g_new")) then
      drop table all_g_new;
    end if

    create table all_g_new
      (
	gene_name	varchar (120), 
	lg_location	numeric(8,2), 
	metric		varchar(5),
	zdb_id		varchar(50), 
	OR_lg		varchar(2), 
	panel_id	varchar(50),
	panel_abbrev	varchar(20),
	abbrev		varchar(20),
	allgene_abbrev_order varchar(60)
	  not null,
	owner		varchar(50),
	entry_date	datetime year to fraction, 
	locus_zdb_id	varchar (50),
	locus_name	varchar (80)

	-- all_genes does not have a primary key
      )
      fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3  
      extent size 256 next size 256 lock mode page;
    revoke all on all_g_new from "public";


    -- get genes that are mapped

    insert into all_g_new 
	(gene_name, lg_location, metric, zdb_id, OR_lg, panel_id, panel_abbrev,
	 abbrev, allgene_abbrev_order, owner, entry_date, locus_zdb_id, locus_name ) 
      select mrkr_name, lg_location, mm.metric, mrkr_zdb_id, OR_lg, pn.zdb_id,
	     pn.abbrev, mrkr_abbrev, mrkr_abbrev_order, mm.owner, mm.entry_date,
	     locus.zdb_id, locus.locus_name
	from marker, mapped_marker mm, panels_new pn, OUTER locus
	where mm.marker_id = mrkr_zdb_id 
	  and mm.refcross_id = pn.zdb_id 
	  and mrkr_zdb_id = locus.cloned_gene
	  and mrkr_type in ('GENE');

    -- mapped by independent linkages
    insert into all_g_new
	(gene_name,lg_location, metric, zdb_id, or_lg, panel_id, panel_abbrev,
	 abbrev, allgene_abbrev_order, owner, entry_date, locus_zdb_id, locus_name)
      select x0.mrkr_name,
	     NULL::numeric(8,2),
	     'NULL'::varchar(5),
	     x0.mrkr_zdb_id,
	     x11.lnkg_or_lg,
	     x11.lnkg_zdb_id,
	     'NULL'::varchar(10),
	     x0.mrkr_abbrev,
	     x0.mrkr_abbrev_order,
	     'NULL'::varchar(50),
	     NULL::datetime year to fraction,
	     x2.zdb_id,
	     x2.locus_name 
	from marker x0,
	     linkage_member x1,
	     linkage x11,	
	     outer locus x2 
	where x1.lnkgmem_member_zdb_id = x0.mrkr_zdb_id
	  and x1.lnkgmem_linkage_zdb_id = x11.lnkg_zdb_id
	  AND x0.mrkr_zdb_id = x2.cloned_gene
          and x0.mrkr_type in ('GENE');


    -- get genes that aren't mapped

    insert into all_g_new
      select mrkr_name, 0, '', mrkr_zdb_id,0, 'na'::varchar(50),
	     'na'::varchar(10), mrkr_abbrev, mrkr_abbrev_order, 'na'::varchar(50),
	     NULL::datetime year to fraction, locus.zdb_id, locus.locus_name
	from marker, OUTER locus
	where not exists 
	      ( select * 
		  from mapped_marker mm
		  where mrkr_zdb_id = mm.marker_id )
	  and mrkr_zdb_id = locus.cloned_gene
	  and mrkr_type in ('GENE')
	  and not exists
	      ( select * 
		  from linkage_member
		  where mrkr_zdb_id = lnkgmem_member_zdb_id
		);

    -- create indexes; constraints that use them are added at the end.

    if (exists (select *
	          from sysindexes
		  where idxname = "all_genes_zdb_id_index_b")) then
      -- use the "a" set of names
      -- other indexes
      create index all_genes_zdb_id_index_a 
	on all_g_new (zdb_id)
	fillfactor 100
	in idxdbs3;
    else
      -- other indexes
      create index all_genes_zdb_id_index_b
	on all_g_new (zdb_id)
	fillfactor 100
	in idxdbs3;
    end if

    update statistics high for table all_g_new;



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

      -- ===== PANELS =====
      let errorHint = "rename  PANELS ";    
 
      -- The following statement also drops the view mapped_anons,
      -- because it depends upon panels.

      drop table panels;
      rename table panels_new to panels;
 
      -- define constraints.  indexes for them are defined earlier.
      -- primary key
      alter table panels add constraint
	primary key (zdb_id)
	  constraint panels_primary_key;

      -- alternate keys
      alter table panels add constraint
	unique (name)
	  constraint panels_name_unique;
      alter table panels add constraint
	unique (abbrev)
	  constraint panels_abbrev_unique;

      grant select on panels to "public";



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



      -- ===== ALL_GENES =====

      drop table all_genes;
      rename table all_g_new to all_genes;

      -- no constraints

      grant select on all_genes to "public";



      ---------------- mapped_anons

      create view mapped_anons
	  (zdb_id, marker_name, abbrev, OR_lg, lg_location,
	   panel_abbrev, panel_id, owner)
	as 
	select mrkr_zdb_id, mrkr_name, mrkr_abbrev, OR_lg, lg_location,
	       p.abbrev, p.zdb_id, mm.owner
	  from marker, mapped_marker mm, panels p
	  where mm.marker_id = mrkr_zdb_id 
	    and mm.refcross_id = p.zdb_id
	    and mrkr_type <> 'GENE';

      grant select on mapped_anons to public;

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
  
update statistics for function regen_genomics;

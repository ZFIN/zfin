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

  -- set debug file to '/tmp/debug-regen_<!--|DB_NAME|-->';

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

	return 0;
      end
    end exception;


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
 


    ---------------- paneled_markers
    let errorHint = "paneled_markers";

    if (exists (select *
	          from systables
		  where tabname = "paneled_m_new")) then
      drop table paneled_m_new;
    end if

    create table paneled_m_new 
      (
	zdb_id		varchar(50), 
	mname		varchar(120),
	abbrev			varchar(20)
	  not null,
	panldmrkr_abbrev_order	varchar(60)
	  not null,
	mtype		varchar(10), 
	OR_lg		varchar(2),
	lg_location	numeric(8,2), 
	metric		varchar(5), 
	target_abbrev	varchar(20),
	target_id	varchar(50), 
	private		boolean, 
	owner		varchar(50),
	scores		varchar(200), 
	framework_t	boolean, 
	entry_date	datetime year to fraction, 
	map_name	varchar(30)

	-- Paneled_markers does not have a primary key.
      )
      fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3
      extent size 2048 next size 2048 lock mode page;
    revoke all on paneled_m_new from "public";

    insert into paneled_m_new
      select mrkr_zdb_id, mrkr_name, mrkr_abbrev, mrkr_abbrev_order, 
	     mrkr_type, mm.OR_lg,
	     mm.lg_location, mm.metric, pn.abbrev, pn.zdb_id, mm.private, 
	     mm.owner, mm.scoring_data, mm.framework_t, mm.entry_date, 
	     mm.map_name
	from marker, mapped_marker mm, panels_new pn
	where mm.marker_id = mrkr_zdb_id 
	  and mm.refcross_id = pn.zdb_id
	  and mm.marker_type <> 'SNP';

    -- display all Tuebingen mutants on map_marker search 
    --  these will eventually be going in using the linked marker approach 
    insert into paneled_m_new
      select a.zdb_id, a.name, a.allele, fish_allele_order, 'MUTANT', b.OR_lg,
	     b.lg_location, b.metric, c.abbrev, c.zdb_id, b.private, b.owner,
	     b.scoring_data, b.framework_t, b.entry_date, b.map_name
	from fish a, mapped_marker b, panels_new c
	where b.marker_id = a.zdb_id 
	  and b.refcross_id = c.zdb_id
          and b.marker_type <> 'SNP';

    -- Temporary ?? adjustment to get locus records into paneled_markers
    -- as well.  Suggested by Tom, approved by Judy, and implemented by Dave
    -- on 2000/11/10

    insert into paneled_m_new
      select a.zdb_id, a.locus_name, a.abbrev, locus_abbrev_order, 
	     'MUTANT', b.OR_lg,
	     b.lg_location, b.metric, c.abbrev, c.zdb_id, b.private, b.owner,
	     b.scoring_data, b.framework_t, b.entry_date, b.map_name
	from locus a, mapped_marker b, panels_new c
	where b.marker_id = a.zdb_id
	  and b.refcross_id = c.zdb_id
	  and b.marker_type <> 'SNP';
	
    --update paneled_m_new set map_name = (select mrkr_abbrev from marker where mrkr_zdb_id = zdb_id)where mtype = 'SNP'; 

    update paneled_m_new 
      set map_name = NULL 
      where map_name = abbrev;

    -- create indexes; constraints that use them are added at the end.

    if (exists (select *
	          from sysindexes
		  where idxname = "paneled_markers_mtype_index_b")) then
      -- use the "a" set of names
      -- other indexes
      create index paneled_markers_mtype_index_a
	on paneled_m_new (mtype) 
	fillfactor 100
	in idxdbs3;
      create index paneled_markers_mname_index_a 
	on paneled_m_new (mname)
	fillfactor 100
	in idxdbs3;
      create index paneled_markers_target_id_index_a
	on paneled_m_new (target_id) 
	fillfactor 100
	in idxdbs3;
      create index paneled_markers_zdb_id_index_a 
	on paneled_m_new (zdb_id)
	fillfactor 100
	in idxdbs3;
    else
      -- other indexes
      create index paneled_markers_mtype_index_b
	on paneled_m_new (mtype) 
	fillfactor 100
	in idxdbs3;
      create index paneled_markers_mname_index_b 
	on paneled_m_new (mname)
	fillfactor 100
	in idxdbs3;
      create index paneled_markers_target_id_index_b
	on paneled_m_new (target_id) 
	fillfactor 100
	in idxdbs3;
      create index paneled_markers_zdb_id_index_b
	on paneled_m_new (zdb_id)
	fillfactor 100
	in idxdbs3;
    end if

    update statistics high for table paneled_m_new;


    --------------- public_paneled_markers
    let errorHint = "public_paneled_markers";

    if (exists (select *
	          from systables
		  where tabname = "public_paneled_m_new")) then
      drop table public_paneled_m_new;
    end if

    create table public_paneled_m_new 
      (
	zdb_id		varchar(50),
	abbrev		varchar(20), 
	mtype		varchar(10), 
	OR_lg		varchar(2),
	lg_location		numeric(8,2), 
	metric		varchar(5), 
	target_abbrev	varchar(20),
	mghframework	boolean,
	target_id		varchar(50),
        map_name        varchar(20)  

	-- public_paneled_markers does not have a primary key.
      )
      fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3  
      extent size 1024 next size 1024 lock mode page;
    revoke all on public_paneled_m_new from "public";


    insert into public_paneled_m_new
      select mrkr_zdb_id, mrkr_abbrev, mrkr_type, mm.OR_lg, mm.lg_location,
	     mm.metric, pn.abbrev, 'f'::boolean, mm.refcross_id, mm.map_name
	from marker, mapped_marker mm, panels_new pn
	where mm.marker_id = mrkr_zdb_id
	  and mm.refcross_id = pn.zdb_id
	  and mm.private = 'f';
    
    -- Create a temporary index
    create index public_paneled_m_new_zdb_id_index 
      on public_paneled_m_new (zdb_id)
      in idxdbs3;	  


    -- to flag markers that are (publicly) mapped on more than one lg

    select distinct a.zdb_id 
      from public_paneled_m_new a, public_paneled_m_new b
      where a.zdb_id = b.zdb_id 
	and a.or_lg <> b.or_lg 
      into temp dup_tmp with no log;

    update public_paneled_m_new set abbrev = conc(abbrev,'*')
      where zdb_id in ( select * from dup_tmp);

    drop table dup_tmp;


    --  a temporary fix to display all Tuebingen mutants on map_marker search
    --  these will eventually be going in using the linked marker approach 
    insert into public_paneled_m_new
      select a.zdb_id, a.allele, 'MUTANT', b.OR_lg,
	     b.lg_location, b.metric, c.abbrev, 'f'::boolean, b.refcross_id,b.map_name
	from fish a, mapped_marker b, panels_new c
	where b.marker_id = a.zdb_id 
	  and b.refcross_id = c.zdb_id
	  and b.private = 'f';

    -- Temporary ?? adjustment to get locus records into public_paneled_markers
    -- as well.  Suggested by Tom, approved by Judy, and implemented by Dave
    -- on 2000/11/10

    insert into public_paneled_m_new
      select a.zdb_id, a.abbrev, 'MUTANT', b.or_lg, b.lg_location, b.metric,
	     c.abbrev, 'f'::boolean, b.refcross_id , b.map_name
	from locus a, mapped_marker b, panels_new c
	where b.marker_id = a.zdb_id and b.refcross_id = c.zdb_id
	  and b.private = 'f';


    update public_paneled_m_new 
      set mghframework = 't'::boolean 
      where exists 
	    ( select 'x' 
		from mapped_marker b
		where public_paneled_m_new.zdb_id = b.marker_id  
		  and b.refcross_id = 'ZDB-REFCROSS-980521-11'
		  and b.marker_type = 'SSLP' );

    -- get SNP names onto the map

    insert into public_paneled_m_new
      select mm.marker_id,mm.map_name, mm.marker_type, mm.OR_lg, mm.lg_location,
             mm.metric, pn.abbrev, 'f'::boolean, mm.refcross_id, mrkr_abbrev
        from marker, mapped_marker mm, panels_new pn
        where mm.marker_type = 'SNP'
          and mm.refcross_id = pn.zdb_id
          and mm.private = 'f'
	  and mrkr_zdb_id = mm.marker_id;



    -- to add connecting lines to the mapper between genes & ests commom 
    -- to ln54 & t51. 
    update public_paneled_m_new 
      set mghframework = 't'::boolean 
      where zdb_id in 
	    ( select m1.marker_id 
		from mapped_marker m1, mapped_marker m2 
		where m1.refcross_id = 'ZDB-REFCROSS-990426-6'
		  and m2.refcross_id = 'ZDB-REFCROSS-990707-1'
		  and m1.marker_type in  ('GENE','EST')
		  and m1.marker_id = m2.marker_id );


    -- drop temporary index from above
    drop index public_paneled_m_new_zdb_id_index;

    -- create indexes; constraints that use them are added at the end.

    if (exists (select *
	          from sysindexes
		  where idxname = "public_paneled_markers_mtype_index_b")) then
      -- use the "a" set of names
      -- other indexes
      create index public_paneled_markers_mtype_index_a
	on public_paneled_m_new (mtype)
	fillfactor 100
	in idxdbs3;
      -- to speed up map generation	
      create index public_paneled_markers_target_abbrev_etc_index_a
	on public_paneled_m_new (target_abbrev,or_lg,mtype,zdb_id)
	fillfactor 100
	in idxdbs3;
      create index public_paneled_markers_zdb_id_index_a
	on public_paneled_m_new (zdb_id)
	fillfactor 100
	in idxdbs3;
    else
      -- other indexes
      create index public_paneled_markers_mtype_index_b
	on public_paneled_m_new (mtype)
	fillfactor 100
	in idxdbs3;
      -- to speed up map generation	
      create index public_paneled_markers_target_abbrev_etc_index_b
	on public_paneled_m_new (target_abbrev,or_lg,mtype,zdb_id)
	fillfactor 100
	in idxdbs3;
      create index public_paneled_markers_zdb_id_index_b
	on public_paneled_m_new (zdb_id)
	fillfactor 100
	in idxdbs3;
    end if

    update statistics high for table public_paneled_m_new;


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
    --   6 locus name alias, locus abbrev alias
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
    select lower(mrkr_abbrev)allmapnm_name, mrkr_zdb_id allmapnm_zdb_id, 1 allmapnm_significance
    from marker
    union all
    select lower(mrkr_name) allmapnm_name, mrkr_zdb_id allmapnm_zdb_id, 2 allmapnm_significance
    from marker
    union all
    select lower(abbrev) allmapnm_name, zdb_id allmapnm_zdb_id, 3 allmapnm_significance
    from locus
    where abbrev is not NULL 
    union all
    select lower(locus_name) allmapnm_name, zdb_id allmapnm_zdb_id, 4 allmapnm_significance
    from locus
    union all
    select distinct lower(dalias_alias) allmapnm_name, dalias_data_zdb_id allmapnm_zdb_id, 5 allmapnm_significance
    from data_alias, alias_group, marker
    where mrkr_zdb_id = dalias_data_zdb_id
    and dalias_group = aliasgrp_name
    and aliasgrp_significance = 1
    union all 
    select lower(lcsali_locus_name_alias) allmapnm_name, lcsali_locus_zdb_id allmapnm_zdb_id, 6 allmapnm_significance
    from locus_alias
    union all 
    select lower(lcsali_locus_abbrev_alias) allmapnm_name, lcsali_locus_zdb_id allmapnm_zdb_id, 6 allmapnm_significance
    from locus_alias
    where lcsali_locus_abbrev_alias is not NULL 
    and lcsali_locus_abbrev_alias <> ""
    union all
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
    into temp amnn with no log;
    

   let errorHint = "all_map_names-known_correspondance";

    create index amnn_tmp_index
        on amnn (allmapnm_name, allmapnm_zdb_id)
        in idxdbs1;

    -- For genes that have known correspondences with loci, also include the
    -- locus's possible names as possible names for the gene.
    insert into amnn 
    select distinct amn2.allmapnm_name, cloned_gene allmapnm_zdb_id, 9 allmapnm_significance
    from amnn amn2, locus
    where amn2.allmapnm_zdb_id = locus.zdb_id
    and cloned_gene is not null
    and not exists (
        select 0 
        from amnn an3 
        where an3.allmapnm_name   = amn2.allmapnm_name 
        and   an3.allmapnm_zdb_id = locus.cloned_gene
    );
    drop index 	amnn_tmp_index;

    insert into amnn
    -- Include putative gene assignments 
    select lower(putgene_putative_gene_name) allmapnm_name, putgene_mrkr_zdb_id allmapnm_zdb_id, 10 allmapnm_significance
    from putative_non_zfin_gene
    ;
    -- For genes also include orthologue names and abbrevs as possible names.
    -- Ken says not to include the orthologue accession numbers in this table.
    -- Judy and Dave agree.

    let errorHint = "all_map_names-orthologs";

    insert into amnn
    select distinct lower(ortho_name) allmapnm_name, c_gene_id allmapnm_zdb_id, 11 allmapnm_significance
    from orthologue
    ;
    insert into amnn
    select distinct lower(ortho_abbrev) allmapnm_name, c_gene_id allmapnm_zdb_id, 11 allmapnm_significance
    from orthologue
    where ortho_abbrev is not null 
    ;
  
    insert into all_m_names_new (allmapnm_name, allmapnm_zdb_id, allmapnm_significance)
        select allmapnm_name, allmapnm_zdb_id, min(allmapnm_significance)  
        from amnn
        where allmapnm_name <> ''
        and  allmapnm_name is not NULL
        group by 1,2;

    drop table amnn;
    
    let errorHint = "all_map_names-accession_numbers";

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

    create unique index all_m_names_new_primary_key_index
    on all_m_names_new (allmapnm_name, allmapnm_zdb_id) in idxdbs1;

    select lower(acc_num) allmapnm_name, linked_recid allmapnm_zdb_id, 12 allmapnm_significance
    from db_link, all_m_names_new
    where acc_num[9] <> ',' 
    and db_link.linked_recid = allmapnm_zdb_id
    and lower(acc_num) <> allmapnm_name 
    union 
    select  lower(acc_num) allmapnm_name, c_gene_id allmapnm_zdb_id, 12 allmapnm_significance
    from db_link, all_m_names_new, orthologue
    where acc_num[9] <> ','
    and db_link.linked_recid = orthologue.zdb_id
    and orthologue.c_gene_id = allmapnm_zdb_id
    and lower(acc_num) <> allmapnm_name
    into temp amnn with no log
    ;

    create unique index amnn_primary_key_index
        on amnn (allmapnm_name, allmapnm_zdb_id)
        in idxdbs1;
        
   delete from amnn where exists( 
        select *
        from all_m_names_new an
        where an.allmapnm_zdb_id = amnn.allmapnm_zdb_id
        and   an.allmapnm_name   = amnn.allmapnm_name
    );

    drop index all_m_names_new_primary_key_index;

    insert into all_m_names_new select * from amnn;

    drop table amnn;


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


    --trace on;


    ----------------  all_markers;
    let errorHint = "all_markers";

    if (exists (select *
        from systables
        where tabname = "all_m_new")) then
        drop table all_m_new;
    end if

    create table all_m_new 
      (
	zdb_id		varchar(50), 
	mname		varchar(120),
	mtype		varchar(10), 
	abbrev		varchar(20),
	allmrkr_abbrev_order	varchar(60)
      )
      fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3  
      extent size 512 next size 512 lock mode page;
    revoke all on all_m_new from "public";

    insert into all_m_new
	(zdb_id, mname, mtype, abbrev, allmrkr_abbrev_order)
      select mrkr_zdb_id, mrkr_name, mrkr_type, mrkr_abbrev, mrkr_abbrev_order
	from marker;

    insert into all_m_new
      select zdb_id, locus_name, 'MUTANT'::varchar(10), 
             abbrev, locus_abbrev_order
	from locus;
    -- no zmap

    -- create indexes; constraints that use them are added at the end.
    if (exists (select *
	          from sysindexes
		  where idxname = "all_markers_primary_key_index_b")) then
      -- use the "a" set of names
      -- primary key  
      create unique index all_markers_primary_key_index_a
	on all_m_new (zdb_id)
	fillfactor 100
	in idxdbs3;
    else
      -- primary key  
      create unique index all_markers_primary_key_index_b
	on all_m_new (zdb_id)
	fillfactor 100
	in idxdbs3;
    end if

    update statistics high for table all_m_new;



    -------------- total_links_copy
    let errorHint = "total_links_copy";

    -- table total_links_copy is used to display Haffter linkages only

    if (exists (select *
	          from systables
		  where tabname = "total_l_new_copy")) then
      drop table total_l_new_copy;
    end if

    create table total_l_new_copy 
      (
	from_id		varchar(50), 
	to_id		varchar(50), 
	dist		numeric(8,2),
	LOD		numeric(8,2), 
	owner		varchar(50), 
	private		boolean
      )
      fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3  
      extent size 32 next size 32 lock mode page;
    revoke all on total_l_new_copy from "public";

    insert into total_l_new_copy
      select m1_id as from_id, m2_id as to_id, dist, LOD, owner, private
	from linkages_COPY;
    insert into total_l_new_copy
      select m2_id as from_id, m1_id as to_id, dist, LOD, owner, private
	from linkages_COPY;

    -- create indexes; constraints that use them are added at the end.

    if (exists (select *
	          from sysindexes
		  where idxname = "total_links_copy_primary_key_index_b")) then
      -- use the "a" set of names
      -- primary key
      create unique index total_links_copy_primary_key_index_a
	on total_l_new_copy (from_id,to_id)
	fillfactor 100
	in idxdbs3;
    else
      -- primary key
      create unique index total_links_copy_primary_key_index_b
	on total_l_new_copy (from_id,to_id)
	fillfactor 100
	in idxdbs3;
    end if

    update statistics high for table total_l_new_copy;


    ------------- all_linked_members;
    let errorHint = "all_linked_members";

    if (exists (select *
	          from systables
		  where tabname = "all_l_m_new")) then
      drop table all_l_m_new;
    end if

    create table all_l_m_new 
      (
	alnkgmem_linkage_zdb_id varchar(50),
	alnkgmem_member_zdb_id  varchar(50),
	alnkgmem_member_name    varchar(120),
	alnkgmem_member_abbrev  varchar(20),
	alnkgmem_member_abbrev_order  varchar(60),
	alnkgmem_marker_type    varchar(10),
	alnkgmem_source_zdb_id  varchar(50),
	alnkgmem_private	boolean,
	alnkgmem_comments	lvarchar,
	alnkgmem_num_auths      integer,
	alnkgmem_source_name    varchar(40),
	alnkgmem_or_lg		varchar(2),
	alnkgmem_num_members    integer
      )
      fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3  
      extent size 128 next size 128 lock mode page;
    revoke all on all_l_m_new from "public";

    
    insert into all_l_m_new
      select lnkg_zdb_id, lnkgmem_member_zdb_id, mname, 
	     abbrev, allmrkr_abbrev_order, mtype,
	     lnkg_source_zdb_id, lnkg_private, lnkg_comments, '',
	     'NULL'::varchar(40), lnkg_or_lg,''
	from linkage, linkage_member, all_m_new
	  where lnkg_zdb_id = lnkgmem_linkage_zdb_id 
	    and lnkgmem_member_zdb_id = all_m_new.zdb_id ;

    insert into all_l_m_new
      select lnkg_zdb_id, lnkgmem_member_zdb_id, name, 
	     locus.abbrev, locus_abbrev_order, 'MUTANT',
	     lnkg_source_zdb_id, lnkg_private, lnkg_comments, '',
	     'NULL'::varchar(40), lnkg_or_lg, ''
	from linkage, linkage_member, fish, locus 
	where lnkg_zdb_id = lnkgmem_linkage_zdb_id 
	  and lnkgmem_member_zdb_id = fish.zdb_id 
	  and fish.locus = locus.zdb_id
	  and fish.line_type = 'mutant';
    
    update all_l_m_new 
      set alnkgmem_source_name =
	  ( select full_name 
	      from person 
	      where alnkgmem_source_zdb_id = person.zdb_id )
      where alnkgmem_source_zdb_id[1,9] = 'ZDB-PERS-';

    update all_l_m_new 
      set alnkgmem_num_auths = 
	  ( select num_auths 
	      from publication
	      where alnkgmem_source_zdb_id = publication.zdb_id );

    update all_l_m_new 
      set alnkgmem_source_name = 
	  ( select pub_mini_ref 
	      from publication
	      where alnkgmem_source_zdb_id = publication.zdb_id )
      where alnkgmem_source_zdb_id[1,8] = 'ZDB-PUB-';


    update all_l_m_new 
      set alnkgmem_num_members = 
	  ( select count(*) 
	      from linkage_member
	      where lnkgmem_linkage_zdb_id = alnkgmem_linkage_zdb_id );


    -- create indexes; constraints that use them are added at the end.
    if (exists (select *
	          from sysindexes
		  where idxname = "all_linked_members_primary_key_index_b")) then
      -- use the "a" set of names
      -- primary key
      create unique index all_linked_members_primary_key_index_a
	on all_l_m_new (alnkgmem_linkage_zdb_id,alnkgmem_member_zdb_id)
	fillfactor 100
	in idxdbs3;
      -- other indexes
      create index alnkgmem_member_zdb_id_index_a
	on all_l_m_new (alnkgmem_member_zdb_id)
	fillfactor 100
	in idxdbs3;
    else
      -- primary key
      create unique index all_linked_members_primary_key_index_b
	on all_l_m_new (alnkgmem_linkage_zdb_id,alnkgmem_member_zdb_id)
	fillfactor 100
	in idxdbs3;
      -- other indexes
      create index alnkgmem_member_zdb_id_index_b
	on all_l_m_new (alnkgmem_member_zdb_id)
	fillfactor 100
	in idxdbs3;
    end if;

    update statistics high for table all_l_m_new;



    ------------- all_mapped_markers;
    let errorHint = "all_mapped_markers";

    if (exists (select *
	          from systables
		  where tabname = "all_m_m_new")) then
      drop table all_m_m_new;
    end if

    create table all_m_m_new 
      (
	zdb_id		varchar(50), 
	mname		varchar(120),
	abbrev		varchar(20),
	allmapmrkr_abbrev_order	 varchar(60)
	  not null,
	mtype		varchar(10), 
	OR_lg		varchar(2),
	lg_location	numeric(8,2) ,
	target_abbrev	varchar(20),
	target_id	varchar(50), 
	private		boolean ,
	owner		varchar(50),
	dist		numeric(8,2), 
	metric		varchar(5), 
	m2_type		varchar(10),
	entry_date	datetime year to fraction

      -- all_mapped_markers does not have a primary key.
      )
      fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3  
      extent size 2048 next size 2048 lock mode page;
    revoke all on all_m_m_new from "public";


    insert into all_m_m_new
      select zdb_id, mname, abbrev, panldmrkr_abbrev_order,
	     mtype, OR_lg, lg_location, target_abbrev,
	     target_id, private, owner, NULL::numeric(8,2), metric,
	     'NULL'::varchar(10), entry_date
      from paneled_m_new;
    -- paneled_m_new already had the zmap data


    insert into all_m_m_new
      select alnkgmem_member_zdb_id, alnkgmem_member_name, 
	     alnkgmem_member_abbrev, alnkgmem_member_abbrev_order,
	     alnkgmem_marker_type, alnkgmem_or_lg,
	     NULL::numeric(8,2), 'NULL'::varchar(20),
	     alnkgmem_linkage_zdb_id, alnkgmem_private, alnkgmem_source_zdb_id,
	     NULL::numeric(8,2), 'NULL'::varchar(5), 'NULL'::varchar(10),
	     NULL::datetime year to fraction
	from all_l_m_new
	where alnkgmem_or_lg <> '0';

    -- create indexes; constraints that use them are added at the end.

    if (exists (select *
	          from sysindexes
		  where idxname = "all_mapped_markers_mname_index_b")) then
      -- use the "a" set of names
      -- other indexes
      create index all_mapped_markers_mname_index_a
	on all_m_m_new (mname)
	fillfactor 100
	in idxdbs3;
      create index all_mapped_markers_mtype_index_a
	on all_m_m_new (mtype)
	fillfactor 100
	in idxdbs3;
      create index all_mapped_markers_zdb_id_index_a
	on all_m_m_new (zdb_id)
	fillfactor 100
	in idxdbs3;
    else
      -- other indexes
      create index all_mapped_markers_mname_index_b
	on all_m_m_new (mname)
	fillfactor 100
	in idxdbs3;
      create index all_mapped_markers_mtype_index_b
	on all_m_m_new (mtype)
	fillfactor 100
	in idxdbs3;
      create index all_mapped_markers_zdb_id_index_b
	on all_m_m_new (zdb_id)
	fillfactor 100
	in idxdbs3;
    end if

    update statistics high for table all_m_m_new;



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
	private		boolean,
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
	 abbrev, allgene_abbrev_order, private, owner, entry_date, locus_zdb_id, locus_name ) 
      select mrkr_name, lg_location, mm.metric, mrkr_zdb_id, OR_lg, pn.zdb_id,
	     pn.abbrev, mrkr_abbrev, mrkr_abbrev_order, mm.private, mm.owner, mm.entry_date,
	     locus.zdb_id, locus.locus_name
	from marker, mapped_marker mm, panels_new pn, OUTER locus
	where mm.marker_id = mrkr_zdb_id 
	  and mm.refcross_id = pn.zdb_id 
	  and mrkr_zdb_id = locus.cloned_gene
	  and mrkr_type in ('GENE','EST');

    -- mapped by independent linkages
    insert into all_g_new
	(gene_name,lg_location, metric, zdb_id, or_lg, panel_id, panel_abbrev,
	 abbrev, allgene_abbrev_order, private, owner, entry_date, locus_zdb_id, locus_name)
      select x0.mrkr_name,
	     NULL::numeric(8,2),
	     'NULL'::varchar(5),
	     x0.mrkr_zdb_id,
	     x1.alnkgmem_or_lg,
	     x1.alnkgmem_linkage_zdb_id,
	     'NULL'::varchar(10),
	     x0.mrkr_abbrev,
	     x0.mrkr_abbrev_order,
	     x1.alnkgmem_private,
	     'NULL'::varchar(50),
	     NULL::datetime year to fraction,
	     x2.zdb_id,
	     x2.locus_name 
	from marker x0,
	     all_l_m_new x1 ,
	     outer locus x2 
	where x1.alnkgmem_member_zdb_id = x0.mrkr_zdb_id
	  AND x0.mrkr_zdb_id = x2.cloned_gene
          and x0.mrkr_type in ('GENE','EST');


    -- get genes that aren't mapped

    insert into all_g_new
      select mrkr_name, 0, '', mrkr_zdb_id,0, 'na'::varchar(50),
	     'na'::varchar(10), mrkr_abbrev, mrkr_abbrev_order, 'f'::boolean, 'na'::varchar(50),
	     NULL::datetime year to fraction, locus.zdb_id, locus.locus_name
	from marker, OUTER locus
	where not exists 
	      ( select * 
		  from mapped_marker mm
		  where mrkr_zdb_id = mm.marker_id )
	  and mrkr_zdb_id = locus.cloned_gene
	  and mrkr_type in ('GENE','EST')
	  and not exists
	      ( select * 
		  from all_l_m_new
		  where mrkr_zdb_id = alnkgmem_member_zdb_id
		    and alnkgmem_marker_type in ('GENE','EST') );

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



    ------------------ mapped_genes
    let errorHint = "mapped_genes";

    if (exists (select *
	          from systables
		  where tabname = "mapped_g_new")) then
      drop table mapped_g_new;
    end if

    create table mapped_g_new 
      (
	zdb_id		varchar(50) not null,
	gene_name	varchar(120),
	abbrev		varchar(15),
	mapgene_abbrev_order varchar(60)
	  not null,
	or_lg		varchar(2),
	lg_location	decimal(8,2),
	panel_abbrev	varchar(20),
	panel_id	varchar(50),
	private		boolean,
	owner		varchar(50),
	metric		varchar(5),
	entry_date	datetime year to fraction(3),
	locus_zdb_id	varchar(50),
	locus_name	varchar(80)
      )
      fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3  
      extent size 128 next size 128 lock mode page;
    revoke all on mapped_g_new from "public";

    insert into mapped_g_new
	(zdb_id, gene_name, abbrev, mapgene_abbrev_order, or_lg, lg_location, panel_abbrev, panel_id,
	 private, owner, metric, entry_date, locus_zdb_id, locus_name)
      select 
	  mrkr_zdb_id,
	  mrkr_name,
	  mrkr_abbrev,
	  mrkr_abbrev_order,
	  x1.or_lg,
	  x1.lg_location, 
	  x2.abbrev,
	  x2.zdb_id,
	  x1.private,
	  x1.owner,
	  x1.metric,
	  x1.entry_date,
	  x3.zdb_id,
	  x3.locus_name 
	from marker,
	     mapped_marker x1 ,
	     panels x2,
	     outer locus x3 
	where x1.marker_id = mrkr_zdb_id
	  AND x1.refcross_id = x2.zdb_id
	  AND mrkr_zdb_id = x3.cloned_gene
	  AND mrkr_type = 'GENE';

    -- mapped by independent linkages
    insert into mapped_g_new
	(zdb_id, gene_name, abbrev, mapgene_abbrev_order, or_lg, lg_location, panel_abbrev, panel_id,
	 private, owner, metric, entry_date, locus_zdb_id, locus_name)
      select 
	  x0.mrkr_zdb_id,
	  x0.mrkr_name,
	  x0.mrkr_abbrev,
	  x0.mrkr_abbrev_order,
	  x1.alnkgmem_or_lg,
	  NULL::numeric(8,2),
	  'NULL'::varchar(5),
	  x1.alnkgmem_linkage_zdb_id,
	  x1.alnkgmem_private,
	  'NULL'::varchar(50),
	  'NULL'::varchar(5),
	  NULL::datetime year to fraction,
	  x2.zdb_id,
	  x2.locus_name 
	from marker x0,
	     all_l_m_new x1 ,
	     outer locus x2 
	where x1.alnkgmem_member_zdb_id = x0.mrkr_zdb_id
	  AND x0.mrkr_zdb_id = x2.cloned_gene
	  AND mrkr_type = 'GENE';

    -- create indexes; constraints that use them are added at the end.

    if (exists (select *
	          from sysindexes
		  where idxname = "mapped_genes_zdb_id_index_b")) then
      -- use the "a" set of names
      -- other indexes
      create index mapped_genes_zdb_id_index_a
	on mapped_g_new (zdb_id)
	fillfactor 100
	in idxdbs1;
    else
      -- other indexes
      create index mapped_genes_zdb_id_index_b
	on mapped_g_new (zdb_id)
	fillfactor 100
	in idxdbs1;
    end if

    update statistics high for table mapped_g_new;



    ------------------ sources
    let errorHint = "sources";

    if (exists (select *
	          from systables
		  where tabname = "sources_new")) then
      drop table sources_new;
    end if

    create table sources_new 
      (
	zdb_id		varchar(50), 
	name		varchar(150), 
	address		lvarchar
      )
      fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3  
      extent size 512 next size 512 lock mode page;
    revoke all on sources_new from "public";

    insert into sources_new
      select zdb_id, full_name, address
	from person;

    insert into sources_new
      select zdb_id, name, address
	from lab;

    insert into sources_new
      select zdb_id, name, address
	from company;

    -- create indexes; constraints that use them are added at the end.

    if (exists (select *
	          from sysindexes
		  where idxname = "sources_primary_key_index_b")) then
      -- use the "a" set of names
      -- primary key
      create unique index sources_primary_key_index_a
	on sources_new (zdb_id)
	fillfactor 100
	in idxdbs1;
    else
      -- primary key
      create unique index sources_primary_key_index_b
	on sources_new (zdb_id)
	fillfactor 100
	in idxdbs1;
    end if

    update statistics high for table sources_new;


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



      -- ===== PANELED_MARKERS =====
      let errorHint = "rename PANELED_MARKERS ";

      drop table paneled_markers;
      rename table paneled_m_new to paneled_markers;

      -- define constraints, indexes are defined earlier.
      -- however, there are no constraints on this table.

      grant select on paneled_markers to "public";



      -- ===== PUBLIC_PANELED_MARKERS =====
      let errorHint = "rename PUBLIC_PANELED_MARKERS ";

      drop table public_paneled_markers;
      rename table public_paneled_m_new to public_paneled_markers;

      -- define constraints, indexes are defined earlier.
      -- however, there are no constraints on this table.

      grant select on public_paneled_markers to "public";



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



      -- ===== ALL_MARKERS =====
      let errorHint = "rename ALL_MARKERS";

      drop table all_markers;
      rename table all_m_new to all_markers;

      -- define constraints.  indexes for them are defined earlier.
      -- primary key
      alter table all_markers add constraint
	primary key (zdb_id)
	  constraint all_markers_primary_key;

      grant select on all_markers to "public";



      -- ===== TOTAL_LINKS_COPY =====

      drop table total_links_copy;
      rename table total_l_new_copy to total_links_copy;

      -- define constraints.  indexes for them are defined earlier.
      -- primary key
      alter table total_links_copy add constraint
	primary key (from_id,to_id)
	  constraint total_links_copy_primary_key;

      grant select on total_links_copy to "public";



      -- ===== ALL_LINKED_MEMBERS =====

      drop table all_linked_members;
      rename table all_l_m_new to all_linked_members;

      -- define constraints.  indexes for them are defined earlier.
      -- primary key
      alter table all_linked_members add constraint
	primary key (alnkgmem_linkage_zdb_id,alnkgmem_member_zdb_id)
	  constraint all_linked_members_primary_key;

      grant select on all_linked_members to "public";



      -- ===== ALL_MAPPED_MARKERS =====

      drop table all_mapped_markers;
      rename table all_m_m_new to all_mapped_markers;

      -- no constraints

      grant select on all_mapped_markers to "public";



      -- ===== ALL_GENES =====

      drop table all_genes;
      rename table all_g_new to all_genes;

      -- no constraints

      grant select on all_genes to "public";



      -- ===== MAPPED_GENES =====

      drop table mapped_genes;
      rename table mapped_g_new to mapped_genes;

      -- no constraints

      grant select on mapped_genes to "public";



      -- ===== SOURCES =====

      drop table sources;
      rename table sources_new to sources;

      -- define constraints.  indexes for them are defined earlier.
      -- primary key
      alter table sources add constraint
	primary key (zdb_id)
	  constraint sources_primary_key;

      grant select on sources to "public";


      -- Last, create the view


      ---------------- mapped_anons

      create view mapped_anons
	  (zdb_id, marker_name, abbrev, OR_lg, lg_location,
	   panel_abbrev, panel_id, private, owner)
	as 
	select mrkr_zdb_id, mrkr_name, mrkr_abbrev, OR_lg, lg_location,
	       p.abbrev, p.zdb_id, mm.private, mm.owner
	  from marker, mapped_marker mm, panels p
	  where mm.marker_id = mrkr_zdb_id 
	    and mm.refcross_id = p.zdb_id
	    and mrkr_type <> 'GENE';

      grant select on mapped_anons to public;

      --trace off;
    end -- Local exception handler

    commit work;

  end -- Global exception handler

  return 1;

end function;


grant execute on function "informix".regen_genomics () 
  to "public" as "informix";
  
update statistics for function regen_genomics;


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
  --      /tmp/regen_genomics_exception.
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
  --    To turn tracing on uncomment the next statement (and change the
  --    the filename to be unique to your database!).

  -- set debug file to '/tmp/debug-regen';

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
			       ' SQL Error: ' || sqlError::varchar(200) || 
			       ' ISAM Error: ' || isamError::varchar(200) ||
			       ' ErrorText: ' || errorText ||
			       '" >> /tmp/regen_genomics_exception';
	system exceptionMessage;

	-- Change the mode of the regen_genomics_exception file.  This is
	-- only needed the first time it is created.  This allows us to 
	-- rerun the function from either the web page (as zfishweb) or 
	-- from dbaccess (as whoever).

	system '/bin/chmod 666 /tmp/regen_genomics_exception';

	-- If in a transaction, then roll it back.  Otherwise, by default
	-- exiting this exception handler will commit the transaction.
	rollback work;

	-- Don't drop the tables here.  Leave them around in an effort to
	-- figure out what went wrong.

	return 0;
      end
    end exception;


    ---------------- panels

    if (exists (select *
	          from systables
		  where tabname = "panels_new")) then
      drop table panels_new;
    end if

    create table panels_new (
      zdb_id		varchar(50), 
      entry_time		datetime year to fraction,
      name		varchar(50), 
      abbrev		varchar(10), 
      panel_date		date,
      producer		varchar(50), 
      owner		varchar(50), 
      source		varchar(50),
      comments		clob, 
      ptype		varchar(50), 
      status		varchar(10),
      disp_order		integer,
      metric		varchar(5),
      -- can't name constraints because they'll conflict with 
      -- constraints of production version of table.
      primary key (zdb_id),
      unique (name),
      unique (abbrev)
    )
    fragment by round robin in zfindbs_a, zfindbs_b, zfindbs_c;

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



    ---------------- paneled_markers

    if (exists (select *
	          from systables
		  where tabname = "paneled_m_new")) then
      drop table paneled_m_new;
    end if

    create table paneled_m_new (
      zdb_id		varchar(50), 
      mname		varchar(80),
      abbrev		varchar(20), 
      mtype		varchar(10), 
      OR_lg		varchar(2),
      lg_location		numeric(8,2), 
      metric		varchar(5), 
      target_abbrev	varchar(10),
      target_id		varchar(50), 
      private		boolean, 
      owner		varchar(50),
      scores		varchar(200), 
      framework_t		boolean, 
      entry_date		datetime year to fraction, 
      map_name		varchar(30)

      -- Paneled_markers does not have a primary key.
    )
    fragment by round robin in zfindbs_a, zfindbs_b, zfindbs_c;

    insert into paneled_m_new
      select mrkr_zdb_id, mrkr_name, mrkr_abbrev, mrkr_type, mm.OR_lg,
	     mm.lg_location, mm.metric, pn.abbrev, pn.zdb_id, mm.private, 
	     mm.owner, mm.scoring_data, mm.framework_t, mm.entry_date, 
	     mm.map_name
	from marker, mapped_marker mm, panels_new pn
	where mm.marker_id = mrkr_zdb_id 
	  and mm.refcross_id = pn.zdb_id;

    -- display all Tuebingen mutants on map_marker search 
    --  these will eventually be going in using the linked marker approach 
    insert into paneled_m_new
      select a.zdb_id, a.name, a.allele, 'MUTANT', b.OR_lg,
	     b.lg_location, b.metric, c.abbrev, c.zdb_id, b.private, b.owner,
	     b.scoring_data, b.framework_t, b.entry_date, b.map_name
	from fish a, mapped_marker b, panels_new c
	where b.marker_id = a.zdb_id 
	  and b.refcross_id = c.zdb_id;

    -- Temporary ?? adjustment to get locus records into paneled_markers
    -- as well.  Suggested by Tom, approved by Judy, and implemented by Dave
    -- on 2000/11/10

    insert into paneled_m_new
      select a.zdb_id, a.locus_name, a.abbrev, 'MUTANT', b.OR_lg,
	     b.lg_location, b.metric, c.abbrev, c.zdb_id, b.private, b.owner,
	     b.scoring_data, b.framework_t, b.entry_date, b.map_name
	from locus a, mapped_marker b, panels_new c
	where b.marker_id = a.zdb_id
	  and b.refcross_id = c.zdb_id;


    -- add the ZMAP markers	
    -- commented out till we have to implement will need to do all-gene etc too
    -- insert into paneled_m_new 
    --   select zdb_id, mname, abbrev, mtype, or_lg, lg_location, metric,
    --	      target_abbrev, target_id, 'f'::boolean, 'ZDB-PERS-960805-642',
    --	      'NA', 'f'::boolean, entry_date 
    --     from zmap_pub_pan_mark;	


    -- Create a temporary index
    create index paneled_m_new_zdb_id_index 
      on paneled_m_new (zdb_id);
    update paneled_m_new 
      set map_name = NULL 
      where map_name = abbrev;


    --------------- public_paneled_markers

    if (exists (select *
	          from systables
		  where tabname = "public_paneled_m_new")) then
      drop table public_paneled_m_new;
    end if

    create table public_paneled_m_new (
      zdb_id		varchar(50),
      abbrev		varchar(20), 
      mtype		varchar(10), 
      OR_lg		varchar(2),
      lg_location		numeric(8,2), 
      metric		varchar(5), 
      target_abbrev	varchar(10),
      mghframework	boolean,
      target_id		varchar(50)

      -- public_paneled_markers does not have a primary key.
    )
    fragment by round robin in zfindbs_a, zfindbs_b, zfindbs_c;

    insert into public_paneled_m_new
      select mrkr_zdb_id, mrkr_abbrev, mrkr_type, mm.OR_lg, mm.lg_location,
	     mm.metric, pn.abbrev, 'f'::boolean, mm.refcross_id
	from marker, mapped_marker mm, panels_new pn
	where mm.marker_id = mrkr_zdb_id
	  and mm.refcross_id = pn.zdb_id
	  and mm.private = 'f';


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
	     b.lg_location, b.metric, c.abbrev, 'f'::boolean, b.refcross_id
	from fish a, mapped_marker b, panels_new c
	where b.marker_id = a.zdb_id 
	  and b.refcross_id = c.zdb_id
	  and b.private = 'f';

    -- Temporary ?? adjustment to get locus records into public_paneled_markers
    -- as well.  Suggested by Tom, approved by Judy, and implemented by Dave
    -- on 2000/11/10

    insert into public_paneled_m_new
      select a.zdb_id, a.abbrev, 'MUTANT', b.or_lg, b.lg_location, b.metric,
	     c.abbrev, 'f'::boolean, b.refcross_id 
	from locus a, mapped_marker b, panels_new c
	where b.marker_id = a.zdb_id and b.refcross_id = c.zdb_id
	  and b.private = 'f';

    -- add the ZMAPmarkers	 more Temporary ??		  
    -- insert into public_paneled_m_new 
    --   select zdb_id, abbrevp, mtype, or_lg, lg_location, metric, 
    --          target_abbrev, mghframework, target_id 
    --     from zmap_pub_pan_mark 
    --     where 1 = 1;	


    update public_paneled_m_new 
      set mghframework = 't'::boolean 
      where exists 
	    ( select 'x' 
		from mapped_marker b
		where public_paneled_m_new.zdb_id = b.marker_id  
		  and b.refcross_id = 'ZDB-REFCROSS-980521-11'
		  and b.marker_type = 'SSLP' );


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


    -- Create a temporary index
    create index public_paneled_m_new_zdb_id_index 
      on public_paneled_m_new (zdb_id);




    ----------------  all_map_names;

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

    if (exists (select *
	          from systables
		  where tabname = "all_m_names_new")) then
      drop table all_m_names_new;
    end if

    create table all_m_names_new (
      -- ortho_name is 120 characters long
      -- mrkr_name, locus_name, db_link.acc_num, and all the abbrev 
      -- columns are all 80 characters or less
      allmapnm_name varchar (120) 
	not null
	check (allmapnm_name = lower(allmapnm_name)),

      allmapnm_zdb_id varchar(50)
	not null,

      allmapnm_significance		integer
	not null,

      primary key (allmapnm_name, allmapnm_zdb_id)
    )
    fragment by round robin in zfindbs_a, zfindbs_b, zfindbs_c;


    -- Get name, abbrev, and aliases from marker, fish, and locus
    -- Finally get accession numbers from db_link

    -- Get abbrevs first 

    insert into all_m_names_new 
	(allmapnm_name, allmapnm_zdb_id, allmapnm_significance)
      select lower(mrkr_abbrev), mrkr_zdb_id, 1
	from marker;

    insert into all_m_names_new 
	(allmapnm_name, allmapnm_zdb_id, allmapnm_significance)
      select lower(abbrev), zdb_id, 3
	from locus
	where abbrev is not NULL;

    insert into all_m_names_new 
	(allmapnm_name, allmapnm_zdb_id, allmapnm_significance)
      select lower(allele), zdb_id, 7
	from fish
	where allele is not NULL;

    -- Get names next

    insert into all_m_names_new 
	(allmapnm_name, allmapnm_zdb_id, allmapnm_significance)
      select lower(mrkr_name), mrkr_zdb_id, 2
	from marker
	where not exists
	      ( select * 
		  from all_m_names_new an
		  where lower(mrkr_name) = an.allmapnm_name
		    and mrkr_zdb_id = an.allmapnm_zdb_id );


    insert into all_m_names_new 
	(allmapnm_name, allmapnm_zdb_id, allmapnm_significance)
      select lower(locus_name), zdb_id, 4
	from locus
	where not exists
	      ( select * 
		  from all_m_names_new an
		  where lower(locus_name) = an.allmapnm_name
		    and locus.zdb_id = an.allmapnm_zdb_id );


    insert into all_m_names_new 
	(allmapnm_name, allmapnm_zdb_id, allmapnm_significance)
      select lower(name), zdb_id, 7
	from fish
	where not exists
	      ( select * 
		  from all_m_names_new an
		  where lower(name) = an.allmapnm_name
		    and fish.zdb_id = an.allmapnm_zdb_id );



    -- Get the aliases for marker, fish and locus.

    insert into all_m_names_new 
	(allmapnm_name, allmapnm_zdb_id, allmapnm_significance)
      select distinct lower(mrkrali_marker_name_alias), 
	     mrkrali_marker_zdb_id, 5
	from marker_alias
	where not exists
	      ( select * 
		  from all_m_names_new an
		  where lower(mrkrali_marker_name_alias) = an.allmapnm_name
		    and mrkrali_marker_zdb_id = an.allmapnm_zdb_id );



    -- get aliases for for locus and fish, both of which have their own alias 
    -- tables.

    insert into all_m_names_new 
	(allmapnm_name, allmapnm_zdb_id, allmapnm_significance)
      select lower(lcsali_locus_name_alias), lcsali_locus_zdb_id, 6
	from locus_alias
	where not exists
	      ( select * 
		  from all_m_names_new an
		  where lower(lcsali_locus_name_alias) = an.allmapnm_name
		    and lcsali_locus_zdb_id = an.allmapnm_zdb_id );

    insert into all_m_names_new 
	(allmapnm_name, allmapnm_zdb_id, allmapnm_significance)
      select lower(lcsali_locus_abbrev_alias), lcsali_locus_zdb_id, 6
	from locus_alias
	where lcsali_locus_abbrev_alias is not NULL 
	  and lcsali_locus_abbrev_alias <> ""
	  and not exists
	      ( select * 
		  from all_m_names_new an
		  where lower(lcsali_locus_abbrev_alias) = an.allmapnm_name
		    and lcsali_locus_zdb_id = an.allmapnm_zdb_id );


    insert into all_m_names_new 
	(allmapnm_name, allmapnm_zdb_id, allmapnm_significance)
      select lower(fishali_fish_name_alias), fishali_fish_zdb_id, 8
	from fish_alias
	where not exists
	      ( select * 
		  from all_m_names_new an
		  where lower(fishali_fish_name_alias) = an.allmapnm_name
		    and fishali_fish_zdb_id = an.allmapnm_zdb_id );


    -- For genes that have known correspondences with loci, also include the
    -- locus's possible names as possible names for the gene.

    insert into all_m_names_new
	(allmapnm_name, allmapnm_zdb_id, allmapnm_significance)
      select allmapnm_name, cloned_gene, 9
	from all_m_names_new an2, locus
	where an2.allmapnm_zdb_id = locus.zdb_id
	  and cloned_gene is not null
	  and not exists        -- avoid duplicates
	      ( select * 
		  from all_m_names_new an3
		  where an2.allmapnm_name = an3.allmapnm_name
		    and cloned_gene = an3.allmapnm_zdb_id );


    -- Include putative gene assignments 

    insert into all_m_names_new 
	(allmapnm_name, allmapnm_zdb_id, allmapnm_significance)
      select lower(putgene_putative_gene_name), putgene_mrkr_zdb_id, 10
	from putative_gene
	where not exists		-- avoid duplicates
	      ( select * 
		  from all_m_names_new an
		  where an.allmapnm_name = putgene_putative_gene_name
		    and an.allmapnm_zdb_id = putgene_mrkr_zdb_id );



    -- For genes also include orthologue names and abbrevs as possible names.
    -- Ken says not to include the orthologue accession numbers in this table.
    -- Judy and Dave agree.

    insert into all_m_names_new 
	(allmapnm_name, allmapnm_zdb_id, allmapnm_significance)
      select distinct lower(ortho_name), c_gene_id, 11
	from orthologue
	where not exists
	      ( select *
		from all_m_names_new an
		where c_gene_id = an.allmapnm_zdb_id
		  and lower(ortho_name) = an.allmapnm_name );

    insert into all_m_names_new 
	(allmapnm_name, allmapnm_zdb_id, allmapnm_significance)
      select distinct lower(ortho_abbrev), c_gene_id, 11
	from orthologue
	where not exists
	      ( select *
		from all_m_names_new an
		where c_gene_id = an.allmapnm_zdb_id
		  and lower(ortho_abbrev) = an.allmapnm_name );



    -- Finally, extract out accession numbers for other databases from db_links
    -- for any ZDB object that has at least one record in the all_map_names 
    -- table.  Some db_link records have multiple comma-separated accession 
    -- numbers per record.  Assume none of the accession numbers are already in

    -- Take the easy route with records that are not comma separated
    -- The "distinct" below is needed because many acc_num/linked_recid
    --   combinations have an entry for Genbank and an entry for BLAST.
    -- The last <> condition is needed because somewhere in the database an
    --   accession number is already being defined as an alias.


    insert into all_m_names_new 
	(allmapnm_name, allmapnm_zdb_id, allmapnm_significance)
      select distinct lower(acc_num), linked_recid, 12
	from db_link, all_m_names_new
	where acc_num <> "DUMMY" 
	  and acc_num not like "%,%"
	  and db_link.linked_recid = allmapnm_zdb_id
	  and lower(acc_num) <> allmapnm_name;

    -- Process comma separated accession numbers

    begin
      define marker_zdb_id   varchar(50);
      define acc_nums        varchar(50);
      define acc_nums_length int;
      define start_col       int;
      define stop_col        int;
      define single_acc_num  varchar(50);

      foreach 
	select linked_recid, lower(acc_num), length(acc_num)
	  into marker_zdb_id, acc_nums, acc_nums_length
	  from db_link
	  where acc_num like "%,%"
	    and exists
		( select *
		    from all_m_names_new
		    where allmapnm_zdb_id = linked_recid )

	-- scan acc_nums looking for comma separated accession numbers
	let start_col = 1;
	let stop_col = 1;
	while (start_col < acc_nums_length + 1)
	  if (stop_col > acc_nums_length or
	      substring(acc_nums from stop_col for 1) = ",") then

	    -- hit separator or end of data.  Add acc_num if not blank
	    if (stop_col > start_col) then
	      let single_acc_num = 
		trim(substring(acc_nums from start_col 
					for stop_col - start_col));
	      if (single_acc_num <> "" and
		  not exists
		   (select *
		      from all_m_names_new
		      where allmapnm_name = single_acc_num
			and allmapnm_zdb_id = marker_zdb_id)) then 
		insert into all_m_names_new
		  (allmapnm_name, allmapnm_zdb_id, allmapnm_significance)
		  values (single_acc_num, marker_zdb_id, 12);
	      end if
	    end if
	    -- move past current name
	    let start_col = stop_col + 1;
	  end if
	  -- move column to check forward one space
	  let stop_col = stop_col + 1;
	end while
      end foreach
    end

    --trace on;

    ----------------  all_markers;

    if (exists (select *
	          from systables
		  where tabname = "all_m_new")) then
      drop table all_m_new;
    end if

    create table all_m_new (
      zdb_id		varchar(50), 
      mname		varchar (80),
      mtype		varchar(10), 
      abbrev		varchar(20),

      primary key (zdb_id)
    )
    fragment by round robin in zfindbs_a, zfindbs_b, zfindbs_c;

    insert into all_m_new
	(zdb_id, mname, mtype, abbrev)
      select mrkr_zdb_id, mrkr_name, mrkr_type, mrkr_abbrev
	from marker;

    insert into all_m_new
      select zdb_id, locus_name, 'MUTANT'::varchar(10), abbrev
	from locus;
    -- no zmap



    -------------- total_links_copy

    -- table total_links_copy is used to display Haffter linkages only

    if (exists (select *
	          from systables
		  where tabname = "total_l_new_copy")) then
      drop table total_l_new_copy;
    end if

    create table total_l_new_copy (
      from_id		varchar(50), 
      to_id		varchar(50), 
      dist		numeric(8,2),
      LOD			numeric(8,2), 
      owner		varchar(50), 
      private		boolean,

      primary key (from_id, to_id)
    )
    fragment by round robin in zfindbs_a, zfindbs_b, zfindbs_c;

    insert into total_l_new_copy
      select m1_id as from_id, m2_id as to_id, dist, LOD, owner, private
	from linkages_COPY;
    insert into total_l_new_copy
      select m2_id as from_id, m1_id as to_id, dist, LOD, owner, private
	from linkages_COPY;



    ------------- all_linked_members;  

    if (exists (select *
	          from systables
		  where tabname = "all_l_m_new")) then
      drop table all_l_m_new;
    end if

    create table all_l_m_new (
      alnkgmem_linkage_zdb_id varchar(50),
      alnkgmem_member_zdb_id  varchar(50),
      alnkgmem_member_name    varchar(80),
      alnkgmem_member_abbrev  varchar(10),
      alnkgmem_marker_type    varchar(10),
      alnkgmem_source_zdb_id  varchar(50),
      alnkgmem_private	  boolean,
      alnkgmem_comments	  lvarchar,
      alnkgmem_num_auths      integer,
      alnkgmem_source_name    varchar(40),
      alnkgmem_or_lg	  varchar(2),
      alnkgmem_num_members    integer,

      primary key (alnkgmem_linkage_zdb_id, alnkgmem_member_zdb_id)
    )
    fragment by round robin in zfindbs_a , zfindbs_b , zfindbs_c;

    insert into all_l_m_new
      select lnkg_zdb_id, lnkgmem_member_zdb_id, mname, abbrev, mtype,
	     lnkg_source_zdb_id, lnkg_private, lnkg_comments, '',
	     'NULL'::varchar(40), lnkg_or_lg,''
	from linkage, linkage_member, all_markers
	  where lnkg_zdb_id = lnkgmem_linkage_zdb_id 
	    and lnkgmem_member_zdb_id = all_markers.zdb_id ;

    insert into all_l_m_new
      select lnkg_zdb_id, lnkgmem_member_zdb_id, name, locus.abbrev, 'MUTANT',
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
      where alnkgmem_source_zdb_id like '%PER%';

    update all_l_m_new 
      set alnkgmem_num_auths = 
	  ( select num_auths 
	      from publication
	      where alnkgmem_source_zdb_id = publication.zdb_id );

    update all_l_m_new 
      set alnkgmem_source_name = 
	  ( select SUBSTR(authors,1,position(',',authors)) || ' et al' 
	      from publication
	      where alnkgmem_source_zdb_id = publication.zdb_id )
      where alnkgmem_source_zdb_id like '%PUB%'
	and alnkgmem_num_auths > 1;

    update all_l_m_new 
      set alnkgmem_source_name = 
	  ( select authors 
	      from publication
	      where alnkgmem_source_zdb_id = publication.zdb_id )
      where alnkgmem_source_zdb_id like '%PUB%'
	and alnkgmem_num_auths = 1;


    update all_l_m_new 
      set alnkgmem_num_members = 
	  ( select count(*) 
	      from linkage_member
	      where lnkgmem_linkage_zdb_id = alnkgmem_linkage_zdb_id );

    ------------- all_mapped_markers; 

    if (exists (select *
	          from systables
		  where tabname = "all_m_m_new")) then
      drop table all_m_m_new;
    end if

    create table all_m_m_new (
      zdb_id varchar(50), 
      mname varchar(80),
      abbrev varchar(20), 
      mtype varchar(10), 
      OR_lg varchar(2),
      lg_location numeric(8,2) ,
      target_abbrev varchar(10),
      target_id varchar(50), 
      private boolean ,
      owner varchar(50),
      dist numeric(8,2), 
      metric varchar(5), 
      m2_type varchar(10),
      entry_date datetime year to fraction

      -- all_mapped_markers does not have a primary key.
    )
    fragment by round robin in zfindbs_a, zfindbs_b, zfindbs_c;

    insert into all_m_m_new
      select zdb_id, mname, abbrev, mtype, OR_lg, lg_location, target_abbrev,
	     target_id, private, owner, NULL::numeric(8,2), metric,
	     'NULL'::varchar(10), entry_date
      from paneled_m_new;
    -- paneled_m_new already had the zmap data


    insert into all_m_m_new
      select alnkgmem_member_zdb_id, alnkgmem_member_name, 
	     alnkgmem_member_abbrev, alnkgmem_marker_type, alnkgmem_or_lg,
	     NULL::numeric(8,2), 'NULL'::varchar(20),
	     alnkgmem_linkage_zdb_id, alnkgmem_private, alnkgmem_source_zdb_id,
	     NULL::numeric(8,2), 'NULL'::varchar(5), 'NULL'::varchar(10),
	     NULL::datetime year to fraction
	from all_l_m_new
	where alnkgmem_or_lg <> '0';

    ----------------- all_genes

    if (exists (select *
	          from systables
		  where tabname = "all_g_new")) then
      drop table all_g_new;
    end if

    create table all_g_new (
      gene_name		varchar (80), 
      lg_location		numeric(8,2), 
      metric		varchar(5),
      zdb_id		varchar(50), 
      OR_lg		varchar(2), 
      panel_id		varchar(50),
      panel_abbrev	varchar(10),
      abbrev		varchar(20),
      private		boolean,
      owner		varchar(50),
      entry_date		datetime year to fraction, 
      locus_zdb_id	varchar (50),
      locus_name		varchar (80)

      -- all_genes does not have a primary key
    )
    fragment by round robin in zfindbs_a, zfindbs_b, zfindbs_c;

    -- get genes that are mapped

    insert into all_g_new 
	(gene_name, lg_location, metric, zdb_id, OR_lg, panel_id, panel_abbrev,
	 abbrev, private, owner, entry_date, locus_zdb_id, locus_name ) 
      select mrkr_name, lg_location, mm.metric, mrkr_zdb_id, OR_lg, pn.zdb_id,
	     pn.abbrev, mrkr_abbrev, mm.private, mm.owner, mm.entry_date,
	     locus.zdb_id, locus.locus_name
	from marker, mapped_marker mm, panels_new pn, OUTER locus
	where mm.marker_id = mrkr_zdb_id 
	  and mm.refcross_id = pn.zdb_id 
	  and mrkr_zdb_id = locus.cloned_gene
	  and mrkr_type = 'GENE';

    -- mapped by independent linkages
    insert into all_g_new
	(gene_name,lg_location, metric, zdb_id, or_lg, panel_id, panel_abbrev,
	 abbrev, private, owner, entry_date, locus_zdb_id, locus_name)
      select x0.mrkr_name,
	     NULL::numeric(8,2),
	     'NULL'::varchar(5),
	     x0.mrkr_zdb_id,
	     x1.alnkgmem_or_lg,
	     x1.alnkgmem_linkage_zdb_id,
	     'NULL'::varchar(10),
	     x0.mrkr_abbrev,
	     x1.alnkgmem_private,
	     'NULL'::varchar(50),
	     NULL::datetime year to fraction,
	     x2.zdb_id,
	     x2.locus_name 
	from marker x0,
	     all_l_m_new x1 ,
	     outer locus x2 
	where x1.alnkgmem_member_zdb_id = x0.mrkr_zdb_id
	  AND x0.mrkr_zdb_id = x2.cloned_gene;


    -- get genes that aren't mapped

    insert into all_g_new
      select mrkr_name, 0, '', mrkr_zdb_id,0, 'na'::varchar(50),
	     'na'::varchar(10), mrkr_abbrev, 'f'::boolean, 'na'::varchar(50),
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


    ------------------ mapped_genes

    if (exists (select *
	          from systables
		  where tabname = "mapped_g_new")) then
      drop table mapped_g_new;
    end if

    create table mapped_g_new (
      zdb_id		varchar(50) not null,
      gene_name		varchar(80),
      abbrev		varchar(15),
      or_lg		varchar(2),
      lg_location		decimal(8,2),
      panel_abbrev	varchar(10),
      panel_id		varchar(50),
      private		boolean,
      owner		varchar(50),
      metric		varchar(5),
      entry_date		datetime year to fraction(3),
      locus_zdb_id	varchar(50),
      locus_name		varchar(80)
    )
    fragment by round robin in zfindbs_a , zfindbs_b , zfindbs_c;

    insert into mapped_g_new
	(zdb_id, gene_name, abbrev, or_lg, lg_location, panel_abbrev, panel_id,
	 private, owner, metric, entry_date, locus_zdb_id, locus_name)
      select 
	  mrkr_zdb_id,
	  mrkr_name,
	  mrkr_abbrev,
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
	(zdb_id, gene_name, abbrev, or_lg, lg_location, panel_abbrev, panel_id,
	 private, owner, metric, entry_date, locus_zdb_id, locus_name)
      select 
	  x0.mrkr_zdb_id,
	  x0.mrkr_name,
	  x0.mrkr_abbrev,
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




    ------------------ sources

    if (exists (select *
	          from systables
		  where tabname = "sources_new")) then
      drop table sources_new;
    end if

    create table sources_new (
      zdb_id		varchar(50), 
      name		varchar(150), 
      address		lvarchar,

      primary key (zdb_id)
    )
    fragment by round robin in zfindbs_a, zfindbs_b, zfindbs_c;

    insert into sources_new
      select zdb_id, full_name, address
	from person;

    insert into sources_new
      select zdb_id, name, address
	from lab;

    insert into sources_new
      select zdb_id, name, address
	from company;

    -- To this point, we haven't done anything visible to actual users.
    -- Now we start to make visible changes, so we enclose it all in a
    -- transaction and have an exception handler ready to roll back
    -- if an error occurs.

    begin work;

    -- Delete the old tables.  Some may not exist (if the DB has just
    -- been created), so ignore errors from the drops.

    begin -- local exception handler for dropping of original tables

      on exception in (-206)
	-- ignore any table that doesn't already exist
      end exception with resume;

      -- The following statement also drops the view mapped_anons,
      -- because it depends upon panels.
      drop table panels;
      drop table paneled_markers;
      drop table public_paneled_markers;
      drop table all_map_names;
      drop table all_markers;
      drop table total_links_copy;
      drop table all_linked_members;
      drop table all_mapped_markers;
      drop table all_genes;
      drop table mapped_genes;
      drop table sources;

    end -- local exception handler for dropping of original tables

    -- Now rename our new tables to have the permanent names.

    -- This also requires dropping and recreating any indexes that were created
    -- with the temporary names.  Informix does not support renaming existing 
    -- indexes.  We can't just use the permanent names to begin with because
    -- they conflict with the names of the indexes on the prior version of the
    -- tables.

    -- Note that the exception-handler at the top of this file is still active

    begin -- local exception handler
      define esql, eisam int;

      on exception set esql, eisam
	-- Any error at this point, just rollback.  The rollback will
	-- restore all the old tables and their indices.
	rollback work;
	-- Now pass the error to the master handler to drop the new tables
	raise exception esql, eisam;
      end exception;

      rename table panels_new to panels;

      rename table paneled_m_new to paneled_markers;
      drop index paneled_m_new_zdb_id_index;
      create index paneled_markers_zdb_id_index 
	on paneled_markers (zdb_id);
      create index paneled_m_name_i 
	on paneled_markers (mname);
      create index paneled_m_mtype_i 
	on paneled_markers (mtype);
      create index paneled_m_tid_i 
	on paneled_markers (target_id);

      rename table public_paneled_m_new to public_paneled_markers;
      drop index public_paneled_m_new_zdb_id_index;
      create index public_paneled_markers_zdb_id_index on 
	public_paneled_markers (zdb_id);
      create index public_paneled_m_mtype_i 
	on public_paneled_markers (mtype);
      -- to speed up map generation	
      create index public_paneled_markers_target_abbrev_etc_index
	on public_paneled_markers (target_abbrev, or_lg, mtype, zdb_id);


      rename table all_m_names_new to all_map_names;
      create index allmapnm_zdb_id_index 
	on all_map_names (allmapnm_zdb_id);

      rename table all_m_new to all_markers;

      rename table total_l_new_copy to total_links_copy;

      rename table all_l_m_new to all_linked_members;

      rename table all_m_m_new to all_mapped_markers;
      create index all_mapped_markers_zdb_id_index 
	on all_mapped_markers (zdb_id);
      create index all_map_mark_n_i 
	on all_mapped_markers (mname);
      create index all_map_mark_t_i 
	on all_mapped_markers (mtype);

      rename table all_g_new to all_genes;
      create index all_genes_zdb_id_index 
	on all_genes(zdb_id);

      rename table mapped_g_new to mapped_genes;
      create index mapped_genes_zdb_id_index 
	on mapped_genes (zdb_id);	       

      rename table sources_new to sources;

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


  -- Update statistics on tables that were just created.

  begin work;

  update statistics high for table panels;
  update statistics high for table paneled_markers;
  update statistics high for table public_paneled_markers;
  update statistics high for table all_map_names;
  update statistics high for table all_markers;
  update statistics high for table total_links_copy;
  update statistics high for table all_linked_members;
  update statistics high for table all_mapped_markers;
  update statistics high for table all_genes;
  update statistics high for table sources;

  commit work;

  return 1;

end function;


grant execute on function "informix".regen_genomics () 
  to "public" as "informix";

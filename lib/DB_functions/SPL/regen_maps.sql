drop function regen_maps;
 

create dba function "informix".regen_maps()
  returning integer

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
	  --	   are not the owner.
	end exception with resume;

        let exceptionMessage = 'echo "' || CURRENT ||
			       ' SQL Error: '  || sqlError::varchar(200) || 
			       ' ISAM Error: ' || isamError::varchar(200) ||
			       ' ErrorText: '  || errorText || 
                   ' ErrorHint: '  || errorHint ||
			       '" >> /tmp/regen_maps_exception_<!--|DB_NAME|-->';
	system exceptionMessage;


	-- Change the mode of the regen_maps_exception file.  This is
	-- only needed the first time it is created.  This allows us to 
	-- rerun the function from either the web page (as zfishweb) or 
	-- from dbaccess (as whoever).

	system '/bin/chmod 666 /tmp/regen_maps_exception_<!--|DB_NAME|-->';

	-- If in a transaction, then roll it back.  Otherwise, by default
	-- exiting this exception handler will commit the transaction.
	rollback work;

	-- Don't drop the tables here.  Leave them around in an effort to
	-- figure out what went wrong.

	update zdb_flag set zflag_is_on = 'f'
		where zflag_name = "regen_maps" 
	 	  and zflag_is_on = 't'; 

	return -1;
      end
    end exception;

    update zdb_flag set zflag_is_on = 't'
	where zflag_name = "regen_maps" 
	 and zflag_is_on = 'f';

    let nrows = DBINFO('sqlca.sqlerrd2');

    if (nrows == 0)	then
	return 1;
    end if
 			
    update zdb_flag set zflag_last_modified = CURRENT
	where zflag_name = "regen_maps";
			


    --------------- paneled_markers
    let errorHint = "paneled_markers";
 
    if (exists (select *
	          from systables
		  where tabname = "paneled_m_new")) then
      drop table paneled_m_new;
    end if

    create table paneled_m_new 
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

	-- paneled_markers does not have a primary key.
      )
      fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3  
      extent size 1024 next size 1024 
      lock mode page;
    revoke all on paneled_m_new from "public";

   -- faked mapping information for gene is
   -- taken out of mapped_marker table, they should be 
   -- regenerated into paneled_markers for panel display.
   -- 03/05/08

   -- prepare the genes whose encoded segments has mapping 
   -- information that the gene hasn't got 

   select mrel_mrkr_1_zdb_id as gene_zdb_id, 
	  or_lg, lg_location,refcross_id, metric
     from marker_relationship, mapped_marker m
    where mrel_comments not like "Connects EST %" 
      and mrel_mrkr_2_zdb_id =m.marker_id
      and m.scoring_data is not null
      and mrel_mrkr_1_zdb_id not in (
		select marker_id 
		from mapped_marker mm
		where mm.or_lg = m.or_lg
	  	and mm.lg_location = m.lg_location
	  	and mm.marker_type in (select mtgrpmem_mrkr_type
                                         from marker_type_group_member
					where mtgrpmem_mrkr_type_group='GENEDOM'))
	into temp tmp_gene_id with no log;

   -- do union to be distinct
   -- union gene self mapping info, and those from encoded segments
   -- locus, and mutants.

     select mrkr_zdb_id, mrkr_abbrev, mrkr_type, mm.OR_lg or_lg, 
            mm.lg_location lg_location, mm.metric metric, pn.abbrev panel,
            'f'::boolean frame, mm.refcross_id refcross_id, mm.map_name map_name
       from marker, mapped_marker mm, panels pn
      where mm.marker_id = mrkr_zdb_id
        and mm.refcross_id = pn.zdb_id
    UNION
     select mrkr_zdb_id, mrkr_abbrev, mrkr_type, or_lg,
            lg_location, t.metric as metric, p.abbrev as panel, 
            'f'::boolean as frame, refcross_id, mrkr_abbrev map_name 		 
       from  tmp_gene_id t, 
	     marker m, panels p
      where gene_zdb_id = m.mrkr_zdb_id
        and refcross_id = p.zdb_id 
    UNION
     select mrkr_zdb_id, mrkr_abbrev, mrkr_type, mm.or_lg or_lg,
	    mm.lg_location lg_location, mm.metric metric, p.abbrev panel, 
	   'f'::boolean,mm.refcross_id refcross_id, mrkr_abbrev map_name
       from mapped_marker mm, locus l, marker m, panels p
      where l.zdb_id = mm.marker_id 
	and l.cloned_gene = m.mrkr_zdb_id
	and mm.refcross_id = p.zdb_id 
    UNION
     select mrkr_zdb_id, mrkr_abbrev, mrkr_type, mm.or_lg or_lg,
	    mm.lg_location lg_location, mm.metric metric, p.abbrev panel, 
	   'f'::boolean,mm.refcross_id refcross_id, mrkr_abbrev map_name
      from mapped_marker mm, locus l, fish f, marker m, panels p
     where f.zdb_id = mm.marker_id 
       and f.locus = l.zdb_id 
       and l.cloned_gene = m.mrkr_zdb_id
       and mm.refcross_id = p.zdb_id
    into temp tmp_paneled_markers;

   insert into paneled_m_new
	select * from tmp_paneled_markers;

   drop table tmp_gene_id;
   drop table tmp_paneled_markers;
	  
    
    -- Create a temporary index
    create index paneled_m_new_zdb_id_index 
      on paneled_m_new (zdb_id)
      in idxdbs3;	  

    -- to flag markers that are (publicly) mapped on more than one lg

    select distinct a.zdb_id 
      from paneled_m_new a, paneled_m_new b
      where a.zdb_id = b.zdb_id 
	and a.or_lg <> b.or_lg 
      into temp dup_tmp with no log;


    update paneled_m_new set abbrev = concat(abbrev,'*')
      where zdb_id in ( select * from dup_tmp);

    drop table dup_tmp;

    --  a temporary fix to display all Tuebingen mutants on map_marker search
    --  these will eventually be going in using the linked marker approach 

    insert into paneled_m_new
      select a.zdb_id, a.allele, 'MUTANT', b.OR_lg,
	     b.lg_location, b.metric, c.abbrev, 'f'::boolean, b.refcross_id,b.map_name
	from fish a, mapped_marker b, panels c
	where b.marker_id = a.zdb_id 
	  and b.refcross_id = c.zdb_id
	  ;

    -- Temporary ?? adjustment to get locus records into paneled_markers
    -- as well.  Suggested by Tom, approved by Judy, and implemented by Dave
    -- on 2000/11/10

    -- faked mapping information for locus is
    -- taken out of mapped_marker table, they should be 
    -- regenerated into paneled_markers for panel display.
    -- 03/05/08
    -- union of mapping info from locus itself, as well as
    -- cloned gene, gene encoded segments, and mutants

      select a.zdb_id mut_zdb_id, a.abbrev mut_abbrev, 'MUTANT' mtype, b.or_lg or_lg, b.lg_location lg_location, b.metric metric,
	     c.abbrev panel, 'f'::boolean frame, b.refcross_id refcross_id , b.map_name map_name
	from locus a, mapped_marker b, panels c
	where b.marker_id = a.zdb_id and b.refcross_id = c.zdb_id
     UNION
     select l.zdb_id mut_zdb_id, l.abbrev mut_abbrev, 'MUTANT' mtype, mm.or_lg or_lg,  
	    mm.lg_location lg_location, mm.metric metric, p.abbrev panel,
	   'f'::boolean frame, mm.refcross_id refcross_id, l.abbrev map_name
      from locus l, mapped_marker mm, panels p
     where l.cloned_gene = mm.marker_id 
       and mm.refcross_id = p.zdb_id 
     UNION
     select l.zdb_id mut_zdb_id, l.abbrev mut_abbrev, 'MUTANT' mtype, mm.or_lg or_lg,  
	    mm.lg_location lg_location, mm.metric metric, p.abbrev panel,
	    'f'::boolean frame, mm.refcross_id refcross_id, l.abbrev map_name
       from locus l, mapped_marker mm, panels p, 
	    marker_relationship
      where l.cloned_gene = mrel_mrkr_1_zdb_id 
        and mm.marker_id = mrel_mrkr_2_zdb_id	
        and mm.refcross_id = p.zdb_id
     UNION
      select l.zdb_id mut_zdb_id, l.abbrev mut_abbrev, 'MUTANT' mtype, mm.or_lg or_lg,  
	    mm.lg_location lg_location, mm.metric metric, p.abbrev panel,
	    'f'::boolean frame, mm.refcross_id refcross_id, l.abbrev map_name
       from locus l, fish f, mapped_marker mm, panels p
      where f.locus = l.zdb_id
	and f.zdb_id = mm.marker_id
	and mm.refcross_id = p.zdb_id
      into temp tmp_paneled_markers with no log;

 	let errorHint = "set locus names where no abbrev";
   update tmp_paneled_markers set mut_abbrev = (
        select locus_name from locus
        where mut_zdb_id = locus.zdb_id 
   )
   where mut_zdb_id[1,10] = 'ZDB-LOCUS-' 
   and   mut_abbrev = '';  -- OR mut_abbrev is null ?


   insert into paneled_m_new
	 select * from tmp_paneled_markers;
   drop table tmp_paneled_markers;

    update paneled_m_new 
      set mghframework = 't'::boolean 
      where exists 
	    ( select 'x' 
	from mapped_marker b
		where paneled_m_new.zdb_id = b.marker_id  
		  and b.refcross_id = 'ZDB-REFCROSS-980521-11'
		  and b.marker_type = 'SSLP' );
 
    -- get SNP names onto the map

    insert into paneled_m_new
      select mm.marker_id,mm.map_name, mm.marker_type, mm.OR_lg, mm.lg_location,
             mm.metric, pn.abbrev, 'f'::boolean, mm.refcross_id, mrkr_abbrev
        from marker, mapped_marker mm, panels pn
        where mm.marker_type = 'SNP'
          and mm.refcross_id = pn.zdb_id
	  and mrkr_zdb_id = mm.marker_id;



    -- to add connecting lines to the mapper between genes & ests commom 
    -- to ln54 & t51. 
    update paneled_m_new 
      set mghframework = 't'::boolean 
      where zdb_id in 
	    ( select m1.marker_id 
		from mapped_marker m1, mapped_marker m2 
		where m1.refcross_id = 'ZDB-REFCROSS-990426-6'
		  and m2.refcross_id = 'ZDB-REFCROSS-990707-1'
		  and m1.marker_type in  ('GENE','EST','GENEP')
		  and m1.marker_id = m2.marker_id );


    -- drop temporary index from above
    drop index paneled_m_new_zdb_id_index;

    -- create indexes; constraints that use them are added at the end.
    let errorHint = "creat indexes";

    if (exists (select *
	          from sysindexes
		  where idxname = "paneled_markers_mtype_index_b")) then
      -- use the "a" set of names
      -- other indexes
      create index paneled_markers_mtype_index_a
	on paneled_m_new (mtype)
	fillfactor 100
	in idxdbs3;
      -- to speed up map generation	
      create index paneled_markers_target_abbrev_etc_index_a
	on paneled_m_new (target_abbrev,or_lg,mtype,zdb_id)
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
      -- to speed up map generation	
      create index paneled_markers_target_abbrev_etc_index_b
	on paneled_m_new (target_abbrev,or_lg,mtype,zdb_id)
	fillfactor 100
	in idxdbs3;
      create index paneled_markers_zdb_id_index_b
	on paneled_m_new (zdb_id)
	fillfactor 100
	in idxdbs3;
    end if

    update statistics high for table paneled_m_new;


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



      -- ===== PANELED_MARKERS =====
      let errorHint = "rename PANELED_MARKERS ";

      drop table paneled_markers;
      rename table paneled_m_new to paneled_markers;

      -- define constraints, indexes are defined earlier.
      -- however, there are no constraints on this table.

      grant select on paneled_markers to "public";



     --trace off;
    end -- Local exception handler

    commit work;

    update zdb_flag set zflag_is_on = "f",
	               zflag_last_modified = CURRENT
	where zflag_name = "regen_maps";
	  
  end -- Global exception handler

  return 0;

end function;


grant execute on function "informix".regen_maps () 
  to "public" as "informix";
  
update statistics for function regen_maps;







drop procedure regen_oevdisp();

create dba function "informix".regen_oevdisp()	
  returning integer

  -- regen_oevdisp creates the orthologue_evidence display table.

  -- DEBUGGING:
  -- There are several ways to debug this function.
  --
  -- See the DEBUGGING section of regen_genomics for detailed 
  -- description.

  define geneZdbId		varchar(50);
  define pubZdbId		varchar(50);
  define evidenceCode		char(2);
  define organismList		varchar(100);
  define organismName		varchar(30);

  define preGeneZdbId		varchar(50);
  define prePubZdbId		varchar(50);
  define preEvidenceCode	char(2);

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

	-- An error happened while function was running.

	on exception in (-206, -255, -668)

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
			       '" >> /tmp/regen_oevdisp_exception_<!--|DB_NAME|-->';
	system exceptionMessage;

	-- Change the mode of the regen_oevdisp_exception file.  This is
	-- only needed the first time it is created.  This allows us to 
	-- rerun the function from either the web page (as zfishweb) or 
	-- from dbaccess (as whoever).

	system '/bin/chmod 666 /tmp/regen_oevdisp_exception_<!--|DB_NAME|-->';

	-- If in a transaction, then roll it back.  Otherwise, by default
	-- exiting this exception handler will commit the transaction.

	rollback work;

	-- Don't drop the tables here.  Leave them around in an effort to
	-- figure out what went wrong.
	
	update zdb_flag set zflag_is_on = 'f'
		where zflag_name = "regen_oevdisp" 
	 	  and zflag_is_on = 't'; 

	return -1;
      end
    end exception;

    update zdb_flag set zflag_is_on = 't'
	where zflag_name = "regen_oevdisp" 
	 and zflag_is_on = 'f';

    let nrows = DBINFO('sqlca.sqlerrd2');

    if (nrows == 0)	then
	return 1;
    end if
 			
    update zdb_flag set zflag_last_modified = CURRENT
	where zflag_name = "regen_oevdisp";
			

    let preGeneZdbId = "";
    let prePubZdbId = "";
    let preEvidenceCode = "";
    let organismList = "";
    let organismName = "";
	
    let errorHint = "Creating _temp";	

    if (exists( select *
		  from systables
		  where tabname = "orthologue_evidence_display_temp")) then 
      drop table orthologue_evidence_display_temp;
    end if

    create table orthologue_evidence_display_temp
      (
        gene_zdb_id	varchar(50),
     	pub_zdb_id	varchar(50),  
    	evidence_code	char(2),
    	organism_list	varchar(100)
      	  not null		
      )
      in tbldbs3;

    let errorHint = "Populating _temp";	

    --by looking at both orthologue and orthologue_evidence table,
    --get an organism list for each combination of a gene, a pub and an evidence.
    --These records stored in orthologue_evidence_display_temp

    foreach
      select c_gene_id, oev_pub_zdb_id, oev_evidence_code, organism
	into geneZdbId, pubZdbId, evidenceCode, organismName
	from orthologue, orthologue_evidence
  	where zdb_id = oev_ortho_zdb_id
	order by c_gene_id,oev_evidence_code, oev_pub_zdb_id, organism

      if (geneZdbId = preGeneZdbId) and
	 (pubZdbId = prePubZdbId) and 
         (evidenceCode = preEvidenceCode) then
	let organismList = organismList || ":" || organismName;
      else 
	if (preGeneZdbId <> "") then
	  insert into orthologue_evidence_display_temp
	     values
	       ( preGeneZdbId, prePubZdbId, preEvidenceCode, organismList);
	end if
  	let organismList = organismName;
  	let preGeneZdbId = geneZdbId;
        let prePubZdbId = pubZdbId;
        let preEvidenceCode = evidenceCode;
      end if
    end foreach
	
    insert into orthologue_evidence_display_temp
	values
	  ( preGeneZdbId, prePubZdbId, preEvidenceCode, organismList );

    let errorHint = "Creating pre_";	
    if (exists( select *
		  from systables
		  where tabname = "pre_orthologue_evidence_display")) then 
      drop table pre_orthologue_evidence_display;
    end if

    create table pre_orthologue_evidence_display
      (
  	 oevdisp_zdb_id         varchar(50),
	 oevdisp_gene_zdb_id	varchar(50),
	 oevdisp_evidence_code	char(2),  
	 oevdisp_organism_list	varchar(100)
      )
      in tbldbs2;

    -- To this point we haven't done anything that is visible to the
    -- world.  Wrap everything that is visible in a transaction.

    begin work;

    let errorHint = "Populating pre_";	

    --each 'gene  pub  evidence organism-list' record get a unique
    --OEVDISP zdb_id. The OEVDISP id and the associated pub go to 
    --record attribution. The OEVDISP id with the rest info go to 
    --orthologue_evidence_display. 	

    insert into pre_orthologue_evidence_display
        (oevdisp_gene_zdb_id, oevdisp_evidence_code, 
	  oevdisp_organism_list ) 
      select distinct gene_zdb_id, evidence_code, 
	     organism_list
        from orthologue_evidence_display_temp;

    update pre_orthologue_evidence_display 
		set oevdisp_zdb_id = get_id("OEVDISP");


    let errorHint = "Updating zdb_active_data";	

    delete from	zdb_active_data 
      where zactvd_zdb_id like "ZDB-OEVDISP%" ;

    insert into zdb_active_data
      select oevdisp_zdb_id
   	from pre_orthologue_evidence_display;

    let errorHint = "Populating oevd";	

    insert into orthologue_evidence_display 
	(oevdisp_zdb_id, oevdisp_gene_zdb_id, oevdisp_evidence_code, 
	  oevdisp_organism_list )
      select * 
        from pre_orthologue_evidence_display;

    let errorHint = "Inserting record attributions";	

    insert into record_attribution
        ( recattrib_data_zdb_id, recattrib_source_zdb_id )
      select oevdisp_zdb_id, pub_zdb_id
	from orthologue_evidence_display o,
	     orthologue_evidence_display_temp t
	where o.oevdisp_gene_zdb_id = t.gene_zdb_id
          and o.oevdisp_evidence_code = t.evidence_code
	  and o.oevdisp_organism_list = t.organism_list;

    let errorHint = "Dropping tables";	

    drop table orthologue_evidence_display_temp;   
    drop table pre_orthologue_evidence_display;
    
    commit work;

  end -- global exception handler

  -- update statistics for new table

  begin work;

  update statistics high for table orthologue_evidence_display;
  update statistics high for table record_attribution;

  commit work;
 
  update zdb_flag set zflag_is_on = "f",
	 	      zflag_last_modified = CURRENT
        where zflag_name = "regen_oevdisp";

  return 0;

end function;

grant execute on function "informix".regen_oevdisp () 
  to "public" as "informix";

update statistics for function regen_oevdisp;

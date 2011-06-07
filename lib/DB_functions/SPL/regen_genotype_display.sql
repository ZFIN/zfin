create dba function "informix".regen_genotype_display()	
  returning integer

  begin	-- master exception handler

    define exceptionMessage lvarchar;
    define sqlError integer;
    define isamError integer;
    define errorText varchar(255);
    define errorHint varchar(255);
    define zdbFlagReturn integer;

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
			       '" >> /tmp/regen_genotype_display_exception_<!--|DB_NAME|-->';
	system exceptionMessage;

	-- Change the mode of the regen_oevdisp_exception file.  This is
	-- only needed the first time it is created.  This allows us to 
	-- rerun the function from either the web page (as zfishweb) or 
	-- from dbaccess (as whoever).

	system '/bin/chmod 666 /tmp/regen_genotype_display_exception_<!--|DB_NAME|-->';

	-- If in a transaction, then roll it back.  Otherwise, by default
	-- exiting this exception handler will commit the transaction.

	rollback work;

	-- Don't drop the tables here.  Leave them around in an effort to
	-- figure out what went wrong.
	
        let zdbFlagReturn = release_zdb_flag("regen_genotype_display");
	return -1;
      end
    end exception;

    -- -------------------------------------------------------------------
    --   GRAB ZDB_FLAG
    -- -------------------------------------------------------------------

    let errorHint = "Grab zdb_flag";
    if grab_zdb_flag("regen_genotype_display") <> 0 then
      return 1;
    end if
   let errorHint = "set constraints";
--	set constraints all deferred ;

   let errorHint = "create temp table";
  begin work ;

	create temp table tmp_genotype (genotype_id varchar(255), 
				genotype_handle varchar(255),
				genotype_display varchar(255))
	with no log ;

   let errorHint = "insert into tmp_genotype";

	insert into tmp_genotype
  		select geno_zdb_id, 'test', 'test'
    		from genotype 
    		where geno_is_wildtype = 'f';

	create unique index tg_index
  		on tmp_genotype(genotype_id)
	  using btree in idxdbs3; 

	update statistics high for table tmp_genotype ;

   let errorHint = "update tmp_genotype, set geno_handle";

	update tmp_genotype
  		set genotype_handle = get_genotype_handle(genotype_id) ;

   let errorHint = "update tmp_genotype, set geno_display";

	update tmp_genotype
  		set genotype_display = get_genotype_display(genotype_id);

   let errorHint = "delete from tmp_genotype where handle is null or empty";

	delete from tmp_genotype
  		where genotype_handle is null ;

	delete from tmp_genotype
  		where genotype_handle = '' ;

   let errorHint = "update genotype handle";

	update genotype
  		set geno_handle = (select genotype_handle
					from tmp_genotype
					where genotype_id = geno_zdb_id
                         		and genotype_handle is not null 
			 		and genotype_handle != '')
  		where exists (select 'x'
		  		from tmp_genotype
                  		where geno_zdb_id = genotype_id);

   let errorHint = "update geno_display_name";

	update genotype
  		set geno_display_name = (select genotype_display
						from tmp_genotype
						where genotype_id = geno_zdb_id
                         			and genotype_display is not null 
			 			and genotype_display != '')
  		where exists (select 'x'
		  		from tmp_genotype
                  		where geno_zdb_id = genotype_id
				and genotype_display is not null
				and genotype_display != '');


    commit work;

  end -- global exception handler

  begin work;

  update statistics high for table genotype;

  commit work;
  -- -------------------------------------------------------------------
  --   RELEASE ZDB_FLAG
  -- -------------------------------------------------------------------

  if release_zdb_flag("regen_genotype_display") <> 0 then
    return 1;
  end if

  return 0;

end function;

grant execute on function "informix".regen_genotype_display () 
  to "public" as "informix";

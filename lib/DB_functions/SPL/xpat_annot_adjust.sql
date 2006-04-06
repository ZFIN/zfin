create function xpat_annot_adjust (
		oldAnatitemZdbId	like anatomy_item.anatitem_zdb_id,
		startStgZdbId		like stage.stg_zdb_id,
		endStgZdbId		like stage.stg_zdb_id,
		newAnatitemZdbId	like anatomy_item.anatitem_zdb_id
	) returning integer

 -- ----------------------------------------------------------
 -- The function first verifies the input zdb ids, then
 -- for each xpat annotation record on the old anatomy term
 -- and overlaps the given stage range, creates a new xpat
 -- annotation on the new anatomy term, no duplication though.
 -- Xpat annotation on old term would be deleted if the stage range
 -- no long overlaps the new definition,and history stored in 
 -- zdb_replaced_data.
 --
 -- INPUT:
 --       oldAnatitemZdbId
 --       startStgZdbId
 --       endStgZdbId
 --       newAnatitemZdbId
 -- OUTPUT:
 --       None
 -- RETURN:
 --       success: 0
 --       failure: exception message at STDOUT &
 --                /tmp/xpat_annot_adjust_exception        
 -- ----------------------------------------------------------


  -- -------------------------------------------------------------------
  --   MASTER EXCEPTION HANDLER
  -- -------------------------------------------------------------------
  begin  -- master exception handler

    define exceptionMessage lvarchar;
    define sqlError integer;
    define isamError integer;
    define errorText varchar(255);
    define errorHint varchar(255);
 
    define oldAnatName like anatomy_item.anatitem_name;
    define newAnatName like anatomy_item.anatitem_name;
    define startStgAbbrev like stage.stg_abbrev;
    define endStgAbbrev   like stage.stg_abbrev;
    define xpatResZdbId      like expression_result.xpatres_zdb_id;
    define oldXpatResZdbId   like expression_result.xpatres_zdb_id;

    on exception
      set sqlError, isamError, errorText
      begin

	on exception in (-255) set sqlError, isamError, errorText
      
	  --  255: OK to get a "Not in transaction" here, since
	  --       we might not be in a transaction when the rollback work 
	  --       below is performed.
	end exception with resume

        let exceptionMessage = 'echo "' || CURRENT ||
			       ' SQL Error: '  || sqlError::varchar(200) || 
			       ' ISAM Error: ' || isamError::varchar(200) ||
			       ' ErrorText: '  || errorText || 
		               ' ErrorHint: '  || errorHint ||
			       '" >> /tmp/xpat_annot_adjust_exception';
	system exceptionMessage;

        system '/bin/chmod 666 /tmp/xpat_annot_adjust_exception';
        -- If in a transaction, then roll it back.  Otherwise, by default
        -- exiting this exception handler will commit the transaction.

        rollback work;

        return -1;
       end
     end exception;

    -- -------------------------------------------------------------------
    -- validate the input zdb ids
    -- -------------------------------------------------------------------
     let errorHint = "validate inputs";
    -- oldAnaitemZdbId
     select anatitem_name
       into oldAnatName
       from anatomy_item
      where anatitem_zdb_id = oldAnatitemZdbId;
 
     if (oldAnatName is null) then
       raise exception -746, 0,		-- !!! ERROR EXIT
         'xpat_annot_adjust: ' ||
         'Invalid old anatomy item (' || oldAnatitemZdbId || ')';
     end if

    -- newAnaitemZdbId
     select anatitem_name
       into newAnatName
       from anatomy_item
      where anatitem_zdb_id = newAnatitemZdbId;
 
     if (newAnatName is null) then
       raise exception -746, 0,		-- !!! ERROR EXIT
         'xpat_annot_adjust: ' ||
         'Invalid new anatomy item (' || newAnatitemZdbId || ')';
     end if

    -- startStgZdbId
     select stg_abbrev
       into startStgAbbrev
       from stage
      where stg_zdb_id = startStgZdbId;

     if (startStgAbbrev is null) then
       raise exception -746, 0,		-- !!! ERROR EXIT
         'xpat_annot_adjust: ' ||
         'Invalid start stage (' ||  startStgZdbId || ')';
     end if

     -- endStgZdbId
     select stg_abbrev
       into endStgAbbrev
       from stage
      where stg_zdb_id = endStgZdbId;

     if (endStgAbbrev is null) then
       raise exception -746, 0,		-- !!! ERROR EXIT
         'xpat_annot_adjust: ' ||
         'Invalid end stage (' ||  endStgZdbId || ')';
     end if


    -- -------------------------------------------------------------------
    --   create xpatres_to_be_temp as expression_result
    -- -------------------------------------------------------------------    
     let errorHint = "xpatres_to_be_temp";
     create temp table xpatres_to_be_temp
	(
	  xrtb_old_xpatres_zdb_id	varchar(50) not null,
	  xrtb_xpatres_zdb_id 		varchar(50),
	  xrtb_flag                     varchar(10),
	  xrtb_xpatex_zdb_id		varchar(50) not null,
          xrtb_start_stg_zdb_id		varchar(50) not null,
	  xrtb_end_stg_zdb_id		varchar(50) not null,
          xrtb_anat_item_zdb_id		varchar(50) not null,
	  xrtb_expression_found		boolean not null,
          xrtb_comments			varchar(255)	  
	)with no log;


    -- -------------------------------------------------------------------
    --   create xpatfig_to_be_temp as expression_pattern_figure
    -- -------------------------------------------------------------------    
     let errorHint = "xpatfig_to_be_temp";
     create temp table xpatfig_to_be_temp
	(
	  xftb_old_xpatres_zdb_id	varchar(50) not null,
	  xftb_xpatres_zdb_id	varchar(50),
	  xftb_fig_zdb_id	varchar(50) not null
	) with no log;

    -- -------------------------------------------------------------------
    --  populate  xpatres_to_be_temp  
    -- -------------------------------------------------------------------    

     -- for each xpatresult record on the input old anatomy,
     -- if input stage window, which is the translation stage window,
     -- overlaps the existing stg window of the xpatres record,
     -- create a new xpatresult for the input new anaotmy.

     insert into xpatres_to_be_temp (xrtb_old_xpatres_zdb_id, 
				     xrtb_xpatex_zdb_id,xrtb_start_stg_zdb_id,
				     xrtb_end_stg_zdb_id,xrtb_anat_item_zdb_id,
				     xrtb_expression_found,xrtb_comments)
	     select xpatres_zdb_id, xpatres_xpatex_zdb_id,
		    xpatres_start_stg_zdb_id, xpatres_end_stg_zdb_id, newAnatitemZdbId,
		    xpatres_expression_found, 
		    "Translated from "||oldAnatName||", "||startStgAbbrev||"-"||endStgAbbrev
               from expression_result
	      where xpatres_anat_item_zdb_id = oldAnatitemZdbId
                and stg_windows_overlap(startStgZdbId, endStgZdbId, 
		          xpatres_start_stg_zdb_id, xpatres_end_stg_zdb_id) = "t";
 
    -- -------------------------------------------------------------------
    --  populate  xpatfig_to_be_temp  
    -- -------------------------------------------------------------------    

      -- select out xpatfig records on xpatres that are subject to 
      -- anatomy term change

	insert into xpatfig_to_be_temp (xftb_old_xpatres_zdb_id,xftb_fig_zdb_id)
	     select xrtb_old_xpatres_zdb_id, xpatfig_fig_zdb_id
               from xpatres_to_be_temp, expression_pattern_figure
              where xrtb_old_xpatres_zdb_id = xpatfig_xpatres_zdb_id;
             

    -- -------------------------------------------------------------------
    --   update the xrtb_xpatres_zdb_id with existing  -XPATRES- 
    --   and flag duplication
    --
    --   We do this because we want to capture the old xpatres id and
    --   and the replacing xpatres id (which could be alreadying existing
    --   or newly generated) into zdb_replaced_data
    -- -------------------------------------------------------------------    
     let errorHint = "update existing xrtb_xpatres_zdb_id";

     -- records got updated here would be recognized as duplicates and dropped later
     update xpatres_to_be_temp 
	set xrtb_xpatres_zdb_id = 
		(select xpatres_zdb_id	
		   from expression_result	
		  where xpatres_xpatex_zdb_id = xrtb_xpatex_zdb_id
                    and xpatres_start_stg_zdb_id = xrtb_start_stg_zdb_id
		    and xpatres_end_stg_zdb_id = xrtb_end_stg_zdb_id
	            and xpatres_anat_item_zdb_id = xrtb_anat_item_zdb_id
		    and xpatres_expression_found = xrtb_expression_found);

     update xpatres_to_be_temp 
        set xrtb_flag = "duplicate"
      where xrtb_xpatres_zdb_id is not null;

    -- -------------------------------------------------------------------
    -- for those to get new XPATRES ids, there is slim chance that
    -- duplication might happen. To be safe and correct, we take the trouble
    -- to handle it rather then a simple get_id() call. 
    -- 
    -- records updated here would be add into zdb_active_data,
    -- expression_result, and expression_pattern_figure
    -- -------------------------------------------------------------------

     create temp table xpatres_new_distinct_temp (
  	  xndt_xpatres_zdb_id 		varchar(50),
	  xndt_xpatex_zdb_id		varchar(50) not null,
          xndt_start_stg_zdb_id		varchar(50) not null,
	  xndt_end_stg_zdb_id		varchar(50) not null,
          xndt_anat_item_zdb_id		varchar(50) not null,
	  xndt_expression_found		boolean not null
	)with no log;

     insert into xpatres_new_distinct_temp (
			xndt_xpatex_zdb_id, xndt_start_stg_zdb_id,
			xndt_end_stg_zdb_id, xndt_anat_item_zdb_id,
			xndt_expression_found)
	   select distinct xrtb_xpatex_zdb_id,xrtb_start_stg_zdb_id,
			   xrtb_end_stg_zdb_id,xrtb_anat_item_zdb_id,
		           xrtb_expression_found
             from xpatres_to_be_temp
            where xrtb_xpatres_zdb_id is null;

     update xpatres_new_distinct_temp 	
	set xndt_xpatres_zdb_id = get_id ("XPATRES")
      where xndt_xpatres_zdb_id is null;
		
     update xpatres_to_be_temp 
	set xrtb_flag = "new",
	    xrtb_xpatres_zdb_id = (select xndt_xpatres_zdb_id
                                     from xpatres_new_distinct_temp
                                    where xndt_xpatex_zdb_id = xrtb_xpatex_zdb_id
				      and xndt_start_stg_zdb_id = xrtb_start_stg_zdb_id
                                      and xndt_end_stg_zdb_id = xrtb_end_stg_zdb_id
				      and xndt_anat_item_zdb_id = xrtb_anat_item_zdb_id
                                      and xndt_expression_found = xrtb_expression_found)
      where xrtb_xpatres_zdb_id is null;

    -- -------------------------------------------------------------------
    -- get the new xpatres id for the to-be-added xpatfig records
    -- -------------------------------------------------------------------
     update xpatfig_to_be_temp
	set xftb_xpatres_zdb_id = (select xrtb_xpatres_zdb_id
				     from xpatres_to_be_temp
				    where xrtb_flag = "new"
                                      and xftb_old_xpatres_zdb_id = xrtb_old_xpatres_zdb_id);

     -- we are doing it here to make sure the zdb_replaced_data to be added next step 
     -- would already have a validate reference in zdb_active_data 
     insert into zdb_active_data (zactvd_zdb_id)
	   select xrtb_xpatres_zdb_id 
             from xpatres_to_be_temp
	    where xrtb_flag = "new";

    -- -------------------------------------------------------------------
    --   delete xpatres records that violate stage rule 
    --   and record each in zdb_replaced_data
    -- -------------------------------------------------------------------    
     let errorHint = "record zdb_replaced_data and delete bad xpatres";
   
     foreach 
	  select xrtb_old_xpatres_zdb_id,xrtb_xpatres_zdb_id 
            into oldXpatResZdbId, xpatResZdbId
	    from xpatres_to_be_temp join expression_result
		 on xrtb_old_xpatres_zdb_id = xpatres_zdb_id
           where anatitem_overlaps_stg_window(oldAnatitemZdbId, 
			xpatres_start_stg_zdb_id, xpatres_end_stg_zdb_id) = "f"

          insert into zdb_replaced_data (zrepld_old_zdb_id,zrepld_new_zdb_id)
	       values (oldXpatResZdbId, xpatResZdbId);

	  delete from zdb_active_data
	        where zactvd_zdb_id = oldXpatResZdbId;
     end foreach

    -- -------------------------------------------------------------------
    --   drop the duplicated annotation (due to the translation)
    -- -------------------------------------------------------------------    
     let errorHint = "delete duplicates from xpatres_to_be_temp";
     delete from xpatres_to_be_temp
	   where xrtb_flag = "duplicate";


    -- -------------------------------------------------------------------
    --  insert in the new annotation
    -- -------------------------------------------------------------------    
     let errorHint = "insertion into expression_result";
	
     insert into expression_result (xpatres_zdb_id, xpatres_xpatex_zdb_id,
				    xpatres_start_stg_zdb_id,
				    xpatres_end_stg_zdb_id,xpatres_anat_item_zdb_id,
			            xpatres_expression_found,xpatres_comments)
	   select xrtb_xpatres_zdb_id, xrtb_xpatex_zdb_id,
		  xrtb_start_stg_zdb_id, xrtb_end_stg_zdb_id,
		  xrtb_anat_item_zdb_id, xrtb_expression_found,
		  xrtb_comments
             from xpatres_to_be_temp;

    -- -------------------------------------------------------------------
    --  insert in the added xpatfig records
    -- -------------------------------------------------------------------    
     let errorHint = "insertion into expression_pattern_figure"; 
     insert into expression_pattern_figure (xpatfig_xpatres_zdb_id, xpatfig_fig_zdb_id)
          select distinct xftb_xpatres_zdb_id, xftb_fig_zdb_id
            from xpatfig_to_be_temp
           where xftb_xpatres_zdb_id is not null; 
          
    -- -------------------------------------------------------------------
    -- temp table clean up
    -- -------------------------------------------------------------------    
     drop table xpatres_to_be_temp;
     drop table xpatfig_to_be_temp;

    end  -- Global exception handler

    return 0;

end function;

update statistics for function xpat_annot_adjust;
				






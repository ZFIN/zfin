drop function batch_xpat_annot_adjust;

create function batch_xpat_annot_adjust () returning integer

 -- ----------------------------------------------------------
 -- This function executes with the assumption or requirment
 -- that anatitem_stg_change_tmp table exists with the following
 -- required columns:
 --    t_oldanat_zdb_id
 --    t_start_stg_zdb_id
 --    t_end_stg_zdb_id
 --    t_newanat_zdb_id
 -- This function passes each row of these columns into another
 -- function xpat_annot_adjust() to do the real work on applying 
 -- translation rule on the expression annotation. 
 -- 
 -- INPUT VARS:  none
 -- OUTPUT VARS: none
 -- RETURNS:
 --         on success: 0 
 --         on failure: -1 
 -- EFFECTS:
 --    The execution of xpat_annot_adjust() function would add
 --    new record to the expression_result table with possible
 --    deletion. 
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

    define oldAnatZdbId  like anatomy_item.anatitem_zdb_id;
    define newAnatZdbId  like anatomy_item.anatitem_zdb_id;
    define startStgZdbId like stage.stg_zdb_id;
    define endStgZdbId   like stage.stg_zdb_id;
    define rValue   integer;
    

    on exception
      set sqlError, isamError, errorText
      begin

	on exception in (-255)
	  --  255: OK to get a "Not in transaction" here, since
	  --       we might not be in a transaction when the rollback work 
	  --       below is performed.
	end exception with resume;

        let exceptionMessage = 'echo "' || CURRENT ||
			       ' SQL Error: '  || sqlError::varchar(200) || 
			       ' ISAM Error: ' || isamError::varchar(200) ||
			       ' ErrorText: '  || errorText || 
		               ' ErrorHint: '  || errorHint ||
			       '" >> /tmp/batch_xpat_annot_adjust_exception';
	system exceptionMessage;

        system '/bin/chmod 666 /tmp/batch_xpat_annot_adjust_exception';
        -- If in a transaction, then roll it back.  Otherwise, by default
        -- exiting this exception handler will commit the transaction.

        rollback work;

        return -1;
       end
     end exception;

     let rValue = 0;

     let errorHint = "foreach execution";
     -- pass each row into function xpat_annot_adjust()
     foreach 
	select t_oldanat_zdb_id,t_start_stg_zdb_id,
		t_end_stg_zdb_id,t_newanat_zdb_id 	
          into oldAnatZdbId, startStgZdbId, endStgZdbId, newAnatZdbId
          from anatitem_stg_change_tmp

	execute function xpat_annot_adjust(oldAnatZdbId, startStgZdbId, 
					   endStgZdbId, newAnatZdbId)
        into rValue;

	if (rValue < 0) then
	  raise exception -746, 0, 
           "batch_xpat_annot_adjust: " 
	   || rValue || " is returned from xapt_annot_adjust";
        end if 

     end foreach;

    end  -- Global exception handler

    return 0;

end function;

update statistics for function batch_xpat_annot_adjust;
			
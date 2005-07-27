--drop function populate_stg_obo_id();

create function populate_stg_obo_id()
	returning integer;

  -- populate stage stg_obo_id column
  -- with ZFS:#######(7 digits). We order it
  -- by the stage time order with Unknown goes first.

  define numCounter int;
  define stageId like stage.stg_zdb_id;

  let numCounter = 0;

  foreach 
      select stg_zdb_id
        into stageId
        from stage
    order by stg_hours_start, stg_name

      update stage 
         set stg_obo_id = "ZFS:"|| zero_pad_int(numCounter, 7)
       where stg_zdb_id = stageId;

      let numCounter = numCounter + 1;

   end foreach

   return 0;

end function;

update statistics for function populate_stg_obo_id;

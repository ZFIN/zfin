--drop function populate_anatitem_obo_id();

create function populate_anatitem_obo_id()
	returning integer;

  -- populate anatomy_item anatitem_obo_id column
  -- with ZFA:#######(7 digits). We don't really 
  -- care the number ordering, we will just make a 
  -- little effort to order it by creation date.
  -- The earliest entry is from 2001.

  define numCounter int;
  define anatomyId like anatomy_item.anatitem_zdb_id;

  let numCounter = 0;

  foreach 
      select anatitem_zdb_id
        into anatomyId
        from anatomy_item
       where anatitem_zdb_id[1,10] = "ZDB-ANAT-0" 
    order by anatitem_zdb_id

      update anatomy_item 
         set anatitem_obo_id = "ZFA:"|| zero_pad_int(numCounter, 7)
       where anatitem_zdb_id = anatomyId;

      let numCounter = numCounter + 1;

   end foreach

   return 0;

end function;

update statistics for function populate_anatitem_obo_id;



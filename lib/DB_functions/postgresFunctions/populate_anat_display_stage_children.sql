create or replace function populate_anat_display_stage_children(stageId text, 
						     parentId text,
			 			     parentName text,
						     indent int,
						     seqNum int)
  returns int as $seqNum$;

  -- It is initiated by regen_anatomy, and it calls itself recursively
  -- to populate anatomy_display_new table by using the intermediate table
  -- stage_item_contained and stage_item_child_list.

  declare childIndent int;
   childId  term.term_zdb_id%TYPE;
   childName  term.term_name%TYPE;
   childNameOrder  term.term_name%TYPE;
  
  begin 

  -- insert record into anatomy_display from passed in values
  insert into anatomy_display_new 
    	values(stageId,seqNum,parentId,parentName,indent);

  if (indent = 1) then
    -- this is a root item for the stage, it needs to be deleted from sic.
    delete from stage_items_contained where sic_anatitem_zdb_id = parentId;
  end if; -- else it was called by this function -implying it was deleted.

  -- increment instance variables
  seqNum = seqNum + 1;
  childIndent = indent + 1;

  -- Save the direct descendant in stage_item_child_list table, and 
  -- delete it from stage_item_contained table. Thus, each anatomy item
  -- would only have one display with it highest level at a particular stage.
  for childId, childName, childNameOrder in
     select sic_anatitem_zdb_id, term_name, term_name
       from term_relationship, stage_items_contained, term
      where parentId = termrel_term_1_zdb_id
        and sic_anatitem_zdb_id = termrel_term_2_zdb_id
	and sic_anatitem_zdb_id = term_zdb_id
    loop
    insert into stage_item_child_list 
         values(parentId,childId,childName, childNameOrder);

    delete from stage_items_contained where sic_anatitem_zdb_id = childId;

   end loop; 
  
   -- For each direct descendant saved in stage_item_child_list, recursively
   -- call the function to populate further descendant.
   for childId, childName, childNameOrder in
     select stimchilis_child_zdb_id, stimchilis_child_name, stimchilis_child_name_order
       from stage_item_child_list
      where stimchilis_item_zdb_id = parentId
      order by stimchilis_child_name_order
      loop
      seqNum = select populate_anat_display_stage_children( 
        		stageId, childId, childName, childIndent,seqNum) 

    end loop

  return seqNum;

end;

$seqNum$ LANGUAGE plpglsql;

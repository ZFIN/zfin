
create procedure create_anatomy_relationship_list()
 ------------------------------------------------------
 -- This procedure walks through each anatomy term, finding
 -- other anatomy terms that has contained_by, contains, 
 -- develops_from or develops_into relationship with it,
 -- and groups the relationship terms into four lists. It
 -- creates a temporary table anatomy_relationship_list_temp 
 -- to hold each term and its four list in each row. 
 --
 -- This procedure is created for and used in AO update 
 -- process to help identify if any relationship is changed.
 --
 -- INPUT VARS:
 --   none.
 --
 -- OUTPUT VARS:
 --   none 
 -- 
 -- EFFECT:
 --    anatomy_relationship_list_temp table would stick 
 --    around when this procedure is called from a 
 --    transaction. This is the effect we want.
-----------------------------------------------------

    define anatomyItemId      like anatomy_item.anatitem_zdb_id;
    define relationItemName   like anatomy_item.anatitem_name;
    define containedbyList    lvarchar;
    define containsList       lvarchar;
    define developedfromList  lvarchar;
    define developsintoList   lvarchar;

    -----------------------------------------------
    -- create temp table to hold relationship list
    -----------------------------------------------
    begin
      on exception in (-958, -316)
       -- Ignore these errors:
       --  -958: Temp table already exists.
       --  -316: Index name already exists.
      end exception with resume;
	
     create temp table anatomy_relationship_list_temp (
	arlt_anatitem_zdb_id	varchar(50),
	arlt_contained_by	varchar(255),
	arlt_contains 		lvarchar,
	arlt_develops_from	varchar(255),
	arlt_develops_into	varchar(255)
     )with no log;
 
   end

   -- Paranoid code to delete records from the newly created tables.  Why?
   -- 
   -- The tables are not necessarily newly created.  They may have existed
   -- before this routine was called, left over from a previous invocation  
   -- by the same session.  In this case:
   --  o the create table statement will fail, then
   --  o the exception handler will be called
   --  o the exception handler will say "it's OK to fail to create the table"
   --    and resume execution.
   --
   -- Thus, we can we end up here with tables that already existed.  This routine
   -- says it returns the tables emtpy.  Therefore, we delete anything in them.

    delete from anatomy_relationship_list_temp;   

    foreach 
	select anatitem_zdb_id
	  into anatomyItemId
          from anatomy_item 
      order by anatitem_zdb_id

       let containedbyList = "";
       let containsList = "";
       let developedfromList = "";
       let developsintoList = "";

       -----------------------------------------------
       -- get contained_by list
       -----------------------------------------------

       foreach
	  select anatitem_name
	    into relationItemName
            from anatomy_relationship join anatomy_item
                 on anatrel_anatitem_1_zdb_id = anatitem_zdb_id
           where anatrel_anatitem_2_zdb_id = anatomyItemId
             and anatrel_dagedit_id in ("is_a", "part_of")
	order by anatitem_name

	   if containedbyList <> "" then  
		let containedbyList = containedbyList || ", ";
           end if

 	   let containedbyList = containedbyList || relationItemName ;

       end foreach	

       -----------------------------------------------
       -- get contains list
       -----------------------------------------------

       foreach
	  select anatitem_name
	    into relationItemName
            from anatomy_relationship join anatomy_item
                 on anatrel_anatitem_2_zdb_id = anatitem_zdb_id
           where anatrel_anatitem_1_zdb_id = anatomyItemId
             and anatrel_dagedit_id in ("is_a", "part_of")
	order by anatitem_name

	   if  containsList <> "" then
		let containsList = containsList || ", ";
           end if

 	   let containsList = containsList || relationItemName ;

       end foreach	
	
       -----------------------------------------------
       -- get develops_from list
       -----------------------------------------------

       foreach
	  select anatitem_name
	    into relationItemName
            from anatomy_relationship join anatomy_item
                 on anatrel_anatitem_1_zdb_id = anatitem_zdb_id
           where anatrel_anatitem_2_zdb_id = anatomyItemId
             and anatrel_dagedit_id = "develops_from"
	order by anatitem_name

	   if developedfromList <> "" then 
		let developedfromList = developedfromList || ", ";
	   end if 

 	   let developedfromList = developedfromList || relationItemName ;

       end foreach	

       -----------------------------------------------
       -- get develops_into list
       -----------------------------------------------

       foreach
	  select anatitem_name
	    into relationItemName
            from anatomy_relationship join anatomy_item
                 on anatrel_anatitem_2_zdb_id = anatitem_zdb_id
           where anatrel_anatitem_1_zdb_id = anatomyItemId
             and anatrel_dagedit_id = "develops_from"
	order by anatitem_name

	   if developsintoList <> "" then
		let developsintoList = developsintoList || ", ";
           end if

 	   let developsintoList = developsintoList || relationItemName ;

       end foreach	

       insert into anatomy_relationship_list_temp
	     values (anatomyItemId, containedbyList,containsList, 
		     developedfromList, developsintoList);

    end foreach
   
end procedure;

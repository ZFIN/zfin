
create or replace function  create_anatomy_relationship_list()
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
returns void as $$

    declare anatomyItemId       term.term_zdb_id%TYPE;
     relationItemName    term.term_name%TYPE;
     containedbyList    text;
     containsList       text;
     developedfromList  text;
     developsintoList   text;

    -----------------------------------------------
    -- create temp table to hold relationship list
    -----------------------------------------------
    begin
     drop table if exists anatomy_relationship_list_temp;
     create temp table anatomy_relationship_list_temp (
	arlt_term_zdb_id	text,
	arlt_contained_by	varchar(255),
	arlt_contains 		text,
	arlt_develops_from	varchar(255),
	arlt_develops_into	varchar(255)
     )with no log;
 

    for anatomyItemId in 
	select term_zdb_id
          from term
          where term_ont_id[1,3] = 'ZFA'
      order by term_zdb_id;
      loop
        containedbyList = '';
        containsList = '';
        developedfromList = '';
        developsintoList = '';

       -----------------------------------------------
       -- get contained_by list
       -----------------------------------------------

       for relationItemName in
	  select term_name
            from term_relationship join term
                 on termrel_term_1_zdb_id = term_zdb_id
           where termrel_term_2_zdb_id = anatomyItemId
             and termrel_type in ('is_a', 'part_of')
	order by term_name
	loop
	   if containedbyList <> '' then  
		let containedbyList = containedbyList || ', ';
           end if;

 	    containedbyList = containedbyList || relationItemName ;

       end loop;	

       -----------------------------------------------
       -- get contains list
       -----------------------------------------------

       for relationItemName in
	  select term_name
            from term_relationship join term
                 on termrel_term_2_zdb_id = term_zdb_id
           where termrel_term_1_zdb_id = anatomyItemId
             and termrel_type in ('is_a', 'part_of')
	order by term_name;
	loop

	   if  containsList <> '' then
		let containsList = containsList || ', ';
           end if;

 	    containsList = containsList || relationItemName ;

       end loop;	
	
       -----------------------------------------------
       -- get develops_from list
       -----------------------------------------------

       for relationItemName in:
	  select term_name
	    into relationItemName
            from term_relationship join term
                 on termrel_term_1_zdb_id = term_zdb_id
           where termrel_term_2_zdb_id = anatomyItemId
             and termrel_type = 'develops_from'
	order by term_name;
	loop

	   if developedfromList <> '' then 
		let developedfromList = developedfromList || ', ';
	   end if ;

 	    developedfromList = developedfromList || relationItemName ;

       end loop	;

       -----------------------------------------------
       -- get develops_into list
       -----------------------------------------------

       for relationItemName in
	  select term_name
            from term_relationship join term
                 on termrel_term_2_zdb_id = term_zdb_id
           where termrel_term_1_zdb_id = anatomyItemId
             and termrel_type = 'develops_from'
	order by term_name;
	loop

	   if developsintoList <> '' then
		 developsintoList = developsintoList || ', ';
           end if;

 	    developsintoList = developsintoList || relationItemName ;

       end loop;	

       insert into anatomy_relationship_list_temp
	     values (anatomyItemId, containedbyList,containsList, 
		     developedfromList, developsintoList);

    end loop;
   
end
$$ LANGUAGE plpgsql

create or replace function update_construct_name (constructId text, componentId text)
returns void as $$

   declare name varchar(255);
    	   addName  construct_component.cc_component%TYPE := '';

 begin  
   for addName in
	select cc_component 
           from construct_component
	   where cc_construct_zdb_id = constructId
	   order by cc_order asc
	loop
	name = name||addName;
	addName = '';
        
   end loop;
   
   select trim(name) into name from single;
   update construct
     set construct_name = name
     where construct_zdb_id = constructId;

end ;

$$ LANGUAGE plpgsql;

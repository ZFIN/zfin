create procedure update_construct_name (constructId varchar(50), componentId varchar(50))

   define name varchar(255);
   define addName like construct_component.cc_component;
   let name = "";

   
   foreach
	select cc_component into addName
           from construct_component
	   where cc_construct_zdb_id = constructId
	   order by cc_order asc
	
	let name = name||addName;
	let addName = "";
        

   end foreach
   
   let name = trim(name);
   update construct
     set construct_name = name
     where construct_zdb_id = constructId;

end procedure;

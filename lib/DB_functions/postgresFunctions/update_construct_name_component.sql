
create or replace function update_construct_name_component (vMrkrZdbId varchar(50), vMrkrNewName varchar(255))
returns void as $$

begin 

  update construct_component
  	 set cc_component = vMrkrNewName
 	 where cc_component_zdb_id = vMrkrZdbId;

end
$$ LANGUAGE plpgsql;

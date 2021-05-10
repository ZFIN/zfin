create or replace function regen_construct()
returns void as $$

  declare vConstruct construct.construct_zdb_id%TYPE;

  begin
  
  for vConstruct in
	select cc_construct_zdb_id 
	       from construct_component, marker
	       where cc_component != mrkr_abbrev
	       and cc_component_zdb_id = mrkr_Zdb_id
	loop
        update construct_component
   	       set cc_component = (Select mrkr_abbrev
       		      	      from marker
			      where mrkr_zdb_id = cc_component_zdb_id)
   	       where exists (Select 'x' from marker
   	 		    where mrkr_Zdb_id = cc_component_zdb_id)
	       and cc_construct_zdb_id = vConstruct;

	perform regen_construct_marker(vConstruct);

  end loop;

  for vConstruct in 
  	  select construct_zdb_id  
	  	 from construct, marker
		 where construct_zdb_id = mrkr_Zdb_id
		 and mrkr_name != construct_name
          loop
	  perform regen_construct_marker(vConstruct);
		 

  end loop;

end ;

$$ LANGUAGE plpgsql;

create procedure regen_construct()

  define vConstruct like construct.construct_zdb_id;

  begin
    on exception in (-958, -316)
      -- Ignore these errors:
      --  -958: Temp table already exists.
      --  -316: Index name already exists.
    end exception with resume;

  end

  foreach 
	select distinct cc_construct_zdb_id into vConstruct
	       from construct_component, marker
	       where cc_component != mrkr_abbrev
	       and cc_component_zdb_id = mrkr_Zdb_id

        update construct_component
   	       set cc_component = (Select mrkr_abbrev
       		      	      from marker
			      where mrkr_zdb_id = cc_component_zdb_id)
   	       where exists (Select 'x' from marker
   	 		    where mrkr_Zdb_id = cc_component_zdb_id)
	       and cc_construct_zdb_id = vConstruct;

	execute procedure regen_construct_marker(vConstruct);

  end foreach;

  foreach 

  	  select construct_zdb_id into vConstruct
	  	 from construct, marker
		 where construct_zdb_id = mrkr_Zdb_id
		 and mrkr_name != construct_name

	  execute procedure regen_construct_marker(vConstruct);
		 

  end foreach

end procedure;
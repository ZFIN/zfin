create procedure p_update_related_fish_name ()

define vFishHandle like fish.fish_handle;
define vFishName like fish.fish_name;
define vFishCount int;
define vFish like fish.fish_zdb_id;

	foreach 
	     select distinct fish_zdb_id
	     	    into vFish
		    from fish
		    where fish_modified = 't'
		    
	        execute function get_fish_handle(vFish) into vFishHandle;
		execute function get_fish_name(vFish) into vFishName;
       		
		update fish
            	   set fish_name = vFishName
	      	   where fish_zdb_id = vFish;
       		
		update fish
       	      	   set fish_handle = vFishHandle
	           where fish_zdb_id = vFish;
           end foreach

update fish 
  set fish_modified = 'f';
end procedure;
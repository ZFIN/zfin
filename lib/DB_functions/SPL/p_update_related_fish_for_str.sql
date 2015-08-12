create procedure p_update_related_fish_for_str (vZdbId varchar(50))

define vFishName like fish.fish_name;
define vFishCount int;
define vFish like fish.fish_zdb_id;

let vFishCount = 0;

if (get_obj_type(vZdbId) in ('TALEN','CRISPR','MRPHLNO'))
 then 
      let vFishCount = (Select count(*) from fish_str
      	  	       	       where fishstr_str_zdb_id = vZdbId);
      if (vFishCount > 0)
        then
	  foreach 
	     select distinct fishstr_fish_zdb_id
	     	    into vFish
		    from fish_str
		    where fishstr_str_zdb_id = vZdbId
		    
		execute function get_fish_name(vFish) into vFishName;
       		
		update fish
            	   set fish_name = vFishName
	      	   where fish_zdb_id = vFish;

           end foreach
         end if;

end if;

end procedure;
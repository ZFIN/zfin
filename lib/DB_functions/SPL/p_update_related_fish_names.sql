create procedure p_update_related_fish_names (vZdbId varchar(50))

define vFishHandle like fish.fish_handle;
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
		    
	        execute function get_fish_handle(vFish) into vFishHandle;
		execute function get_fish_name(vFish) into vFishName;
       		
		update fish
            	   set fish_name = vFishName
	      	   where fish_zdb_id = vFish;
       		
		update fish
       	      	   set fish_handle = vFishHandle
	           where fish_zdb_id = vFish;
           end foreach
         end if;
elif (get_obj_type(vZdbId) = 'GENO')
  then
	foreach 
	     select distinct fish_zdb_id
	     	    into vFish
		    from fish
		    where fish_genotype_zdb_id = vZdbId
		    
	        execute function get_fish_handle(vFish) into vFishHandle;
		execute function get_fish_name(vFish) into vFishName;
       		
		update fish
            	   set fish_name = vFishName
	      	   where fish_zdb_id = vFish;
       		
		update fish
       	      	   set fish_handle = vFishHandle
	           where fish_zdb_id = vFish;
           end foreach
end if;

end procedure;
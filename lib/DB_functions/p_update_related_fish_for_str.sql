create or replace function p_update_related_fish_for_str (vZdbId text)
returns void as $$

declare vFishName fish.fish_name%TYPE;
 vFishCount int :=0 ;
 vFish fish.fish_zdb_id%TYPE;

begin

if (get_obj_type(vZdbId) in ('TALEN','CRISPR','MRPHLNO'))
 then 
      vFishCount = (Select count(*) from fish_str
      	  	       	       where fishstr_str_zdb_id = vZdbId);
      if (vFishCount > 0)
        then
	  for vFish in
	      select distinct fishstr_fish_zdb_id
		    from fish_str
		    where fishstr_str_zdb_id = vZdbId
	     loop	    
		select get_fish_name(vFish) into vFishName;
       		
		update fish
            	   set fish_name = vFishName
	      	   where fish_zdb_id = vFish;

           end loop; 
      end if;

end if;

end
$$ LANGUAGE plpgsql

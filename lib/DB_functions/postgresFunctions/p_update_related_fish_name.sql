create or replace function p_update_related_fish_name ()
returns void as $$

declare vFishHandle  fish.fish_handle%TYPE;
 vFishName  fish.fish_name%TYPE;
 vFishCount int;
 vFish  fish.fish_zdb_id%TYPE;
begin
	for vFish in
	     select distinct fish_zdb_id
	     	    into vFish
		    from fish
		    where fish_modified = 't'
	loop 
	        select get_fish_handle(vFish) into vFishHandle;
		select get_fish_name(vFish) into vFishName;
       		
		update fish
            	   set fish_name = vFishName
	      	   where fish_zdb_id = vFish;
       		
		update fish
       	      	   set fish_handle = vFishHandle
	           where fish_zdb_id = vFish;
        end loop;

update fish 
  set fish_modified = 'f';

end
$$ LANGUAGE plpgsql

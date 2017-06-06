create or replace function p_update_related_fish_names (vZdbId varchar(50))
returns void as $$

declare vFishHandle fish.fish_handle%TYPE;
 vFishName fish.fish_name%TYPE;
 vFishCount int :=0;
 vFish fish.fish_zdb_id%TyPE;

begin
if (get_obj_type(vZdbId) in ('TALEN','CRISPR','MRPHLNO'))
 then 
      vFishCount := (Select count(*) from fish_str
      	  	       	       where fishstr_str_zdb_id = vZdbId);
      if (vFishCount > 0)
        then
	  for vFish in
	     select distinct fishstr_fish_zdb_id
		    from fish_str
		    where fishstr_str_zdb_id = vZdbId
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
      end if;
else if (get_obj_type(vZdbId) = 'GENO')
  then
	for vFish in
	     select distinct fish_zdb_id
		    from fish
		    where fish_genotype_zdb_id = vZdbId
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
end if;
end

$$ LANGUAGE plpgsql;

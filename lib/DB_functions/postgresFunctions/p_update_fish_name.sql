create or replace function p_update_fish_name (vZdbId text)
returns void as $$

declare fishName  fish.fish_name%TYPE;
 fishHandle  fish.fish_handle%TYPE;
 vDataType varchar(30) := get_obj_type(vZdbId);
 fishId  fish.fish_zdb_id%TYPE;

begin

if (vDataType = 'MRPHLNO' or vDataType = 'CRISPR' or vDataType = 'TALEN') then

for fishId in  

	select distinct fishstr_fish_zdb_id 
	   from fish_str 
           where fishstr_str_zdb_id = vZdbId
loop
	    fishName = get_fish_name(fishId);
	    fishHandle = get_fish_handle(fishId);

	   update fish
	      set fish_name = fishName
	      where fish_zdb_id = fishId;
 	   update fish
	      set fish_handle = fishHandle
	      where fish_zdb_id = fishId;


end loop;

elif (vDataType = 'GENO') then

 for fishId in 

	select distinct fish_zdb_id
	   from fish
           where fish_genotype_zdb_id = vZdbId
 loop
	   fishName = get_fish_name(fishId);
	   fishHandle = get_fish_handle(fishId);

	   update fish
	      set fish_name = fishName
	      where fish_zdb_id = fishId;
	   update fish
	      set fish_handle = fishHandle
	      where fish_zdb_id = fishId;

 end loop;

end if;

end

$$ LANGUAGE plpgsql




  


end procedure;

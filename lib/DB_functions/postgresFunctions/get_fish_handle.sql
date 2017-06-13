
create or replace function get_fish_handle (vFishZdbId varchar) returns varchar as $fishHandle$

declare fishHandle  fish.fish_handle%TYPE := (Select geno_handle 
		    			        from genotype, fish
    	       	 	 			where fish_genotype_zdb_id = geno_Zdb_id
			 			and fish_zdb_id = vFishZdbId);
 mrkrAbbrev  marker.mrkr_abbrev%TYPE;

begin
if exists (select 'x' from marker, fish_str
	 where mrkr_Zdb_id = fishstr_str_zdb_id
	 and fishstr_fish_Zdb_id = vFishZdbId)
then
	for mrkrAbbrev in 
		select distinct mrkr_abbrev 
  	 	       from marker, fish_str
	 	       where mrkr_Zdb_id = fishstr_str_zdb_id
		       and fishstr_fish_zdb_id = vFishZdbId
		       order by mrkr_abbrev
        loop
          fishHandle := fishHandle || '+' ||mrkrAbbrev;

	end loop;
end if;
	 
return fishHandle;
end

$fishHandle$ LANGUAGE plpgsql;

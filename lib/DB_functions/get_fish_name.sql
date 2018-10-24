create or replace function get_fish_name (vFishZdbId varchar) returns varchar as $fishName$

declare fishName  fish.fish_name%TYPE := '';
 mrkrAbbrev  marker.mrkr_abbrev%TYPE;
 genoWT  genotype.geno_is_wildtype%TYPE := (Select geno_is_wildtype 
 	 				      from fish,genotype
    	     	     			      where geno_zdb_id = fish_genotype_zdb_id
		     			      and fish_zdb_id = vFishZdbId);

begin
if (genoWT = 't')
then
  fishName := (Select geno_handle from genotype, fish
    	       	 	 where fish_genotype_zdb_id = geno_Zdb_id
			 and fish_zdb_id = vFishZdbId);
else

  fishName := (Select geno_display_name from genotype, fish
    	       	 	 where fish_genotype_zdb_id = geno_Zdb_id
			 and fish_zdb_id = vFishZdbId);

end if;

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
          fishName := fishName || ' + ' ||mrkrAbbrev;

        end loop;
end if;
	 
return fishName;
end

$fishName$ LANGUAGE plpgsql;

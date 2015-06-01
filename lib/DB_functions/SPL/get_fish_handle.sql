
create function get_fish_handle (vFishZdbId varchar(50))
returning varchar(255);

define fishHandle like fish.fish_handle;
define mrkrAbbrev like marker.mrkr_abbrev;
let fishHandle = '';
let fishHandle = (Select geno_handle from genotype, fish
    	       	 	 where fish_genotype_zdb_id = geno_Zdb_id
			 and fish_zdb_id = vFishZdbId);

if exists (select 'x' from marker, fish_str
	 where mrkr_Zdb_id = fishstr_str_zdb_id
	 and fishstr_fish_Zdb_id = vFishZdbId)
then
	foreach
		select distinct mrkr_abbrev into mrkrAbbrev
  	 	       from marker, fish_str
	 	       where mrkr_Zdb_id = fishstr_str_zdb_id
		       and fishstr_fish_zdb_id = vFishZdbId
		       order by mrkr_abbrev
    let fishHandle = fishHandle || "+" ||mrkrAbbrev;

end foreach
end if;
	 
return fishHandle;

end function;
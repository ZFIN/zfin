create function get_fish_name (vFishZdbId varchar(50))
returning lvarchar(255);

define fishName like fish.fish_name;
define mrkrAbbrev like marker.mrkr_abbrev;
define genoWT like genotype.geno_is_wildtype;

let fishName = '';


let genoWT = (Select geno_is_wildtype from fish,genotype
    	     	     where geno_zdb_id = fish_genotype_zdb_id
		     and fish_zdb_id = vFishZdbId);

if (genoWT = "t")
then
let fishName = (Select geno_handle from genotype, fish
    	       	 	 where fish_genotype_zdb_id = geno_Zdb_id
			 and fish_zdb_id = vFishZdbId);
else

let fishName = (Select geno_display_name||" "||backgroundList from genotype, fish
    	       	 	 where fish_genotype_zdb_id = geno_Zdb_id
			 and fish_zdb_id = vFishZdbId);

end if;

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
    let fishName = fishName || " + " ||mrkrAbbrev;

end foreach
end if;
	 
return fishName;

end function;

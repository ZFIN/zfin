CREATE OR REPLACE FUNCTION "public"."get_fish_name"("vfishzdbid" varchar, "vfishgenozdbid" varchar)
  RETURNS "pg_catalog"."varchar" AS $fishName$

declare

 backgroundList varchar := get_genotype_backgrounds(vFishGenoZdbId);
 fishName  fish.fish_name%TYPE;
 mrkrAbbrev  marker.mrkr_abbrev%TYPE;
 genoWT  genotype.geno_is_wildtype%TYPE := (Select geno_is_wildtype
 	 				      from fish,genotype
    	     	     			      where geno_zdb_id = fish_genotype_zdb_id
		     			      and fish_zdb_id = vFishZdbId);
begin

raise notice 'fishName0 vFishGenoZdbId %', vFishGenoZdbId;

if (genoWT = 't')
then
  Select geno_handle into fishName from genotype, fish
    	       	 	 where fish_genotype_zdb_id = geno_Zdb_id
			 and fish_zdb_id = vFishZdbId;
else
 
  Select geno_display_name into fishName from genotype, fish
    	       	 	 where fish_genotype_zdb_id = geno_Zdb_id
			 and fish_zdb_id = vFishZdbId;
  raise notice 'fishName1 %', fishName;

  if (fishName = '')
  then
    Select get_genotype_display(geno_zdb_id) into fishName from genotype, fish
                       where fish_genotype_zdb_id = geno_Zdb_id
                       and fish_zdb_id = vFishZdbId;
    raise notice 'fishName calculated from current genotype name is blank, recalculating genotype name for new fish name: "%"', fishName;
  end if;
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

raise notice 'fishName2 %', fishName; 

IF (backgroundList IS NOT NULL AND backgroundList != '')
  THEN
    fishName := fishName || ' (' || backgroundList || ')';
ELSE
    fishName := fishName;
END IF;

return fishName;
end

$fishName$ LANGUAGE plpgsql;

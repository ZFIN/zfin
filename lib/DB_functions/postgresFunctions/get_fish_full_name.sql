create or replace function get_fish_full_name (vFishZdbId text,
       		 		    vFishGenoZdbId text,
				    vFishName varchar) returns varchar as $fullName$

declare backgroundList varchar := get_genotype_backgrounds(vFishGenoZdbId);
        fullName varchar;

begin
if (backgroundList is not null and backgroundList != '')
   then 
   	fullName := vFishName||"("||backgroundList||")"; 
    else
	fullName := vFishName;
end if;

return fullName;
end
$fullName$ LANGUAGE plpgsql;


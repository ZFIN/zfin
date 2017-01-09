create function get_fish_full_name (vFishZdbId varchar(50),
       		 		    vFishGenoZdbId varchar(50),
				    vFishName varchar(255))
returning varchar(255);

define backgroundList varchar(30);
define fullName varchar(255);

let backgroundList = get_genotype_backgrounds(vFishGenoZdbId);


if (backgroundList is not null and backgroundList != '')
   then 
   	let fullName = vFishName||"("||backgroundList||")"; 
    else
	let fullName = vFishName;
end if

return fullName;

end function;

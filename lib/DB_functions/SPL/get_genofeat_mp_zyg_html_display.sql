
drop function get_zygocity_symbol;
drop function get_genofeat_mp_zyg_html_display;

-----------------------------------------------------------------
-- Given a zygocity name, generate zygocity display in symbol format
-- 
-- INPUT VARS:
--     zygocity name
--
-- OUTPUT VARS:
--     none
--
-- RETURNS:
--
--      unknown:      one space
--      complex:      c
--	hemizygous:   +/0
--	heterozygous: +/-
--	homozygous:   -/-
--	wild type:    +/+
--
----------------------------------------------------------------

create function get_zygocity_symbol ( zygName    varchar(30) )
	returning varchar(10);

	if (zygName == "homozygous") then
		return "-/-";

	elif (zygName == "heterozygous") then
		return "+/-";

	elif (zygName == "hemizygous") then
		return "+/0";

	elif (zygName == "complex") then
		return "c";

	elif (zygName == "wild type") then
		return "+/+";

	elif (zygName == "unknown") then
		return " " ;

	end if 

end function;


	
-----------------------------------------------------------------
-- Given a -GENOFEAT- zdb id, generate parental zygocity display 
-- in html format 
-- 
-- INPUT VARS:
--     genofeatZdbId
--
-- OUTPUT VARS:
--     none
--
-- RETURNS:
--
--    parental zygocity html display, e.g. 
--            &#9792; -/- &#9794; +/- 
--
----------------------------------------------------------------

create function get_genofeat_mp_zyg_html_display ( genofeatZdbId  varchar(50) )
	returning varchar (50);

	define momZygName	like zygocity.zyg_name;
	define dadZygName	like zygocity.zyg_name;
	define featureZdbId	like feature.feature_zdb_id;

	define momZygSym	varchar(10);
	define dadZygSym	varchar(10);
	define zygDisplay	varchar(50);
	define hidePrtZyg	boolean;

	let zygDisplay = "";
	let hidePrtZyg = "f";

	select mz.zyg_name, pz.zyg_name, genofeat_feature_zdb_id
          into momZygName, dadZygName, featureZdbId
          from genotype_feature, zygocity mz, zygocity pz
         where genofeat_mom_zygocity = mz.zyg_zdb_id
           and genofeat_dad_zygocity = pz.zyg_zdb_id
           and genofeat_zdb_id = genofeatZdbId;

	-- we suppress parental zygosity for Tgs that do not
        -- have an "is allele of" relationship
	select 't'
          into hidePrtZyg
          from feature
         where feature_zdb_id = featureZdbId
           and feature_type = "TRANSGENIC_INSERTION"
           and not exists 
		 (select 't' 
                    from feature_marker_relationship
           	   where fmrel_ftr_zdb_id = featureZdbId
                     and fmrel_type = "is allele of"
	         );

        -- first tried to use if (not hidePrtZyg) then 
        -- but the value of hidePrtZyg seems never stay "f"
        -- after the above sql, I haven't figure out what that
        -- value is yet...
 
 	if ( hidePrtZyg ) then
	else
	  let momZygSym = get_zygocity_symbol(momZygName);

	  if (momZygSym <> " ") then

	    let zygDisplay = "&#9792;" || momZygSym|| "&nbsp;&nbsp;" ;

	  end if 

	  let dadZygSym = get_zygocity_symbol(dadZygName);
	
	  if (dadZygSym <> " ") then

	    let zygDisplay = zygDisplay || "&#9794;" || dadZygSym;

	  end if
 
	end if
	
	return zygDisplay;

end function;

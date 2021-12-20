create or replace function get_fish_name (vFishZdbId varchar) returns varchar as $fishName$

declare

vFishGenoZdbId varchar := (Select fish_genotype_zdb_id
 	 				      from fish
                          where fish_zdb_id = vFishZdbId);
begin

return get_fish_name(vFishZdbId, vFishGenoZdbId);
end

$fishName$ LANGUAGE plpgsql;

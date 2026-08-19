create or replace function create_color_info ()
returns void as $$

declare

begin

    -- excitation color is bucketed from the EXCITATION length (was mistakenly the
    -- emission length); emission from the emission length. BETWEEN matches the
    -- bucketing used by the construct/fish/expression/feature DIH sub-entities.
    update fluorescent_protein set fp_excitation_color = (
        select fc_color from fluorescent_color
        where fp_excitation_length between fc_lower_bound and fc_upper_bound);

    update fluorescent_protein set fp_emission_color = (
        select fc_color from fluorescent_color
        where fp_emission_length between fc_lower_bound and fc_upper_bound);

    -- NB: the fluorescent_marker table was retired (ZFIN-10352); nothing to update there.

end

$$ LANGUAGE plpgsql;

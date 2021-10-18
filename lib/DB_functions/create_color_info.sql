create or replace function create_color_info ()
returns void as $$

declare  

begin

    update fluorescent_protein set fp_excitation_color = (
        select fc_color from fluorescent_color
        where fc_lower_bound < fp_emission_length AND fc_upper_bound > fp_emission_length);

    update fluorescent_protein set fp_emission_color = (
        select fc_color from fluorescent_color
        where fc_lower_bound < fp_emission_length AND fc_upper_bound > fp_emission_length);

    update efg_fluorescence set ef_excitation_color = (
        select fc_color from fluorescent_color
        where fc_lower_bound < ef_excitation_length AND fc_upper_bound > ef_excitation_length);

    update efg_fluorescence set ef_emission_color = (
        select fc_color from fluorescent_color
        where fc_lower_bound < ef_emission_length AND fc_upper_bound > ef_emission_length);

end

$$ LANGUAGE plpgsql;

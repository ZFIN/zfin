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

    update fluorescence_marker set fm_excitation_color = (
        select fc_color from fluorescent_color
        where fc_lower_bound < fm_excitation_length AND fc_upper_bound > fm_excitation_length);

    update fluorescence_marker set fm_emission_color = (
        select fc_color from fluorescent_color
        where fc_lower_bound < fm_emission_length AND fc_upper_bound > fm_emission_length);

end

$$ LANGUAGE plpgsql;

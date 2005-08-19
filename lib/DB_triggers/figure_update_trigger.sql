create trigger figure_update_trigger update of fig_source_zdb_id, fig_label 
    on figure
    referencing new as new_fig
    for each row
        (
	execute procedure p_insert_into_record_attribution_tablezdbids (
			new_fig.fig_zdb_id,
			new_fig.fig_source_zdb_id),
	execute function zero_pad (new_fig.fig_label) into fig_full_label
) ;

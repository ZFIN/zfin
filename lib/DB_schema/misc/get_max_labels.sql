begin work ;

create procedure get_max_labels_thisse ()

define sum_probe_counter int;
define indv_probe_counter int;
define current_xpat_id varchar(50);
define current_start varchar(50);
define current_end varchar(50);
define current_pub varchar(50);
define current_fig varchar(50);
define current_stg_hour decimal(7,2);

let indv_probe_counter = '0' ;
let sum_probe_counter = '0'; 

execute procedure set_session_params();

	foreach 
  		select distinct xpatstg_xpat_zdb_id, xpatstg_start_stg_zdb_id,
			xpatstg_end_stg_zdb_id, 
			xp.xpat_source_zdb_id,
			fig_zdb_id, a.stg_hours_start
		   into current_xpat_id, current_start, current_end, 
			current_pub, current_fig, current_stg_hour
			from expression_pattern_stage, stage a, stage b, 
				expression_pattern xp, tmp_figs
			where xp.xpat_source_zdb_id = 
				tmp_figs.xpat_source_zdb_id
			and xpat_zdb_id = xpatstg_xpat_zdb_id 
			and xpatstg_xpat_zdb_id = xpat_id
			and xpatstg_start_stg_zdb_id = xpat_start
			and xpatstg_start_stg_zdb_id = a.stg_zdb_id
			and xpatstg_end_stg_zdb_id = b.stg_zdb_id
			and xpatstg_end_stg_zdb_id = xpat_end
			and exists (select 'x' 
					from expression_pattern_image
					where xpatstg_xpat_zdb_id = 
						xpatfimg_xpat_zdb_id
					and xpatstg_start_stg_zdb_id = 
						xpatfimg_xpat_start_stg_zdb_id
					and xpatstg_end_stg_zdb_id = 
						xpatfimg_xpat_end_stg_zdb_id)
					order by xp.xpat_source_zdb_id,
				 			xpatstg_xpat_zdb_id,
				 			a.stg_hours_start

		let sum_probe_counter = (select count(*)
					  from tmp_figs
					  where xpat_id = current_xpat_id);

		let indv_probe_counter = (select count(*)
					   from tmp_fig_foo
					   where xpat_id = current_xpat_id);

		if indv_probe_counter < sum_probe_counter then
			let indv_probe_counter = indv_probe_counter +1 ;

			insert into tmp_fig_foo (fig_zdb_id,
							xpat_id,
							maxlabel,
							stg_start,
							stg_end,
							xpat_pub)
				values (current_fig,
					current_xpat_id,
					indv_probe_counter,
					current_start,
					current_end, current_pub);
				   
		elif indv_probe_counter = sum_probe_counter then
			insert into tmp_fig_foo (fig_zdb_id,
							xpat_id,
							maxlabel,
							stg_start,
							stg_end,
							xpat_pub)
				values (current_fig,
					current_xpat_id,
					sum_probe_counter,
					current_start,
					current_end, current_pub);

		else 
			insert into tmp_fig_foo (fig_zdb_id,
						 xpat_id,
						 maxlabel,
						 stg_start,
						 stg_end, 
						 xpat_pub)
			values (current_fig,
					current_xpat_id,
					'0',
					current_start,
					current_end, 
					current_pub);
		end if ;

	end foreach

end procedure ;

commit work ;
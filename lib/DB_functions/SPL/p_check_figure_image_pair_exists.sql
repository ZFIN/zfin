create procedure p_check_figure_image_pair_exists (vImgZdbId varchar(50), 
						   vFigZdbId varchar(50))
  define vOk	integer ;

  if vImgZdbId is not null 

  then

	if vImgZdbId != 'not specified'

  	then

    	let vOk = (select count(*) 
			from FX_fish_image_private
			where fimgp_fig_zdb_id = vFigZdbId
			and fimgp_zdb_id = vImgZdbId) ;

    		if vOk > 0 then

		let vOk = 1;

    		else 
		raise exception -746,0,'FAIL!: image must be associated with a figure before an xpat' ;

    		end if ;

  	else let vOk = 1 ;

	end if ;

  end if ;

end procedure ;
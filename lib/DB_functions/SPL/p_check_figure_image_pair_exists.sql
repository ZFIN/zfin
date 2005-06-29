create procedure p_check_figure_image_pair_exists (vImgZdbId varchar(50), 
						   vFigZdbId varchar(50))
  define vOk	integer ;
  define vImgComments varchar(50) ;

  if vImgZdbId is not null 

  then 

      let vImgComments = (select fimg_comments 
	    		    from fish_image
			    where vImgZdbID = fimgp_zdb_id
			    and fimgp_owner_zdb_id = 'ZDB-PERS-030520-1') ;

	if vImgComments != 'not specified'

  	then

    	let vOk = (select count(*) 
			from fish_image
			where fimg_fig_zdb_id = vFigZdbId
			and fimg_zdb_id = vImgZdbId) ;

    		if vOk > 0 then

		let vOk = 1;

    		else 
		raise exception -746,0,'FAIL!: image must be associated with a figure before an xpat' ;

    		end if ;

  	else let vOk = 1 ;

	end if ;

  end if ;

end procedure ;
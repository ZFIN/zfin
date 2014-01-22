create procedure p_check_tginsertion_has_construct_relationship (vGenofeat_feature_zdb_id varchar(50))

       define vOk boolean;
       define vFeatureType like feature_type.ftrtype_name;

       let vOk = 't';
       let vFeatureType = get_feature_type(vGenofeat_feature_zdb_id);
       
       if (vFeatureType = "TRANSGENIC_INSERTION")
       then
       
		if exists (select 'x' from feature_marker_relationship
       	  	   	  	   	 where fmrel_ftr_zdb_id = vGenofeat_feature_zdb_id
					 and fmrel_mrkr_zdb_id like '%CONSTR%')		 
		then
			let vOk = 't';
 		else 
			let vOk = 'f';
			
		end if;
       end if ;

       if (vOk = 'f')
       then
		raise exception -746,0,'FAIL!: no construct associated with this insertion';
       end if ;

end procedure;
create or replace function p_check_tginsertion_has_construct_relationship (vGenofeat_feature_zdb_id varchar(50))

returns void as $$

       declare vOk boolean := 't';
       	       vFeatureType feature_type.ftrtype_name%TYPE := get_feature_type(vGenofeat_feature_zdb_id);

begin 
       
       if (vFeatureType = 'TRANSGENIC_INSERTION')
       then
       
		if exists (select 'x' from feature_marker_relationship
       	  	   	  	   	 where fmrel_ftr_zdb_id = vGenofeat_feature_zdb_id
					 and fmrel_mrkr_zdb_id like '%CONSTR%')		 
		then
			vOk = 't';
 		else 
			vOk = 'f';
			
		end if;
       end if ;

       if (vOk = 'f')
       then
		raise exception 'FAIL!: no construct associated with this insertion';
       end if ;

end
$$ LANGUAGE plpgsql

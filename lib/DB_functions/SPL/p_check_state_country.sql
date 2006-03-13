create procedure p_check_state_country (vStateCode varchar(4), 
					vCountryCode varchar(4))

  if (
	(vStateCode is not null 
		and vStateCode != '') 
	   and vCountryCode != 'USA'
      )

  then 

   raise exception -746,0,"FAIL!:'states' are only in USA: check country_code";

  end if;


end procedure ;
create or replace function p_check_state_country (vStateCode varchar(4), 
					vCountryCode varchar(4))
returns void as $$
begin

  if (
	(vStateCode is not null 
		and vStateCode != '') 
	   and vCountryCode != 'USA'
      )

  then 

   raise exception 'FAIL!:states are only in USA: check country_code';

  end if;
end

$$ LANGUAGE plpgsql

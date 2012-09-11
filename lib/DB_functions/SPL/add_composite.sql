
CREATE FUNCTION add_composite(add_id int8)
	RETURNING lvarchar ; 
	
	define add_comp like address.add_composite;
	define add_inst like address.add_institution;
	define add_st1 like address.add_street1;
	define add_st2 like address.add_street2;
	define add_cty like address.add_city;
	define add_stt like address.add_state_code;
	define add_cntry like address.add_country_code;
	define add_zip like address.add_postal_code;

	let add_comp = "";
	
	select add_institution, 
	       add_street1, 
	       add_street2,
	       add_city,
	       add_state_code,
	       add_country_code,
	       add_postal_code 
	into add_inst,
	     add_st1,
	     add_st2,
	     add_cty,
	     add_stt,
	     add_cntry,
	     add_zip
	from address 
	where add_pk_id = add_id;
	
	if (add_inst is not null) then
	      let add_comp = add_inst;
	end if
	if (add_st1 is not null) then
	      let add_comp = add_comp || add_st1;
	end if
	if (add_st2 is not null) then
	      let add_comp = add_comp || add_st2;
	end if
	if (add_cty is not null) then
	      let add_comp = add_comp || add_cty;
	end if
	if (add_stt is not null) then
	      let add_comp = add_comp || add_stt;
	end if
	if (add_zip is not null) then
	      let add_comp = add_comp || add_zip;
	end if
	if (add_cntry is not null) then
	      let add_comp = add_comp || add_cntry;
	end if
	
	
	
	return add_comp;
END FUNCTION ;

create procedure p_check_mrkr_abbrev (vMrkrName   like marker.mrkr_name,
				      vMrkrAbbrev like marker.mrkr_abbrev,
				      vMrkrType   like marker.mrkr_type)


  define vOK boolean ;

  if vMrkrType in (select mtgrpmem_mrkr_type
		     from marker_type_group_member
		     where mtgrpmem_mrkr_type_group = "ABBREV_EQ_NAME") then
    if vMrkrName <> vMrkrAbbrev then
      raise exception -746, 0, 
	'FAIL!! abbreviation must exactly equal name for marker type ' || vMrkrType ;
    end if;
  else
    if exists (select 'x'
		from marker_Type_group_member
		where mtgrpmem_mrkr_type_group = 'CONSTRUCT'
		and mtgrpmem_mrkr_Type = vMrkrType)
	 then 
		let vOK = 't' ;
    else if
	(vMrkrAbbrev != lower(vMrkrAbbrev)  AND
		     vMrkrAbbrev[1,10] <> 'WITHDRAWN:' )
     then

      raise exception -746, 0,
	'FAIL!! abbreviation must be lower case for marker type ' || vMrkrType ;
     end if ;
    end if;
  end if;

end procedure;

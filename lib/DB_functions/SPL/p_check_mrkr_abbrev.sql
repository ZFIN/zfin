create procedure p_check_mrkr_abbrev (vMrkrName   like marker.mrkr_name,
                                      vMrkrAbbrev like marker.mrkr_abbrev,
                                      vMrkrType   like marker.mrkr_type)


  define vOK boolean ;

  if vMrkrAbbrev in (select cv_term_name from controlled_vocabulary) then
   raise exception -746, 0, 
        'FAIL!! abbreviation can not be a foreign species designation ' ;
  end if;


  if vMrkrType in (select mtgrpmem_mrkr_type
                     from marker_type_group_member
                     where mtgrpmem_mrkr_type_group = "ABBREV_EQ_NAME")
		     and vMrkrType not in ('TGCONSTRCT','PTCONSTRCT','ETCONSTRCT','GTCONSTRCT') then
    if vMrkrName <> vMrkrAbbrev then
      raise exception -746, 0, 
        'FAIL!! abbreviation must exactly equal name for marker type ' || vMrkrType ;
    end if;
    if exists (select 'x'
                from marker_Type_group_member
                where mtgrpmem_mrkr_type_group = 'ABBREV_EQ_NAME_UPPER'
                and mtgrpmem_mrkr_Type = vMrkrType
                and UPPER(vMrkrName) != vMrkrAbbrev) then     
        raise exception -746, 0, 
          'FAIL!! abbreviation must be upper case for marker type ' || vMrkrType ;
    end if;
  elif
        (vMrkrAbbrev != lower(vMrkrAbbrev)  AND
                     vMrkrAbbrev[1,10] <> 'WITHDRAWN:'  AND
                     vMrkrType not in ('EFG','REGION','ATB','TGCONSTRCT','PTCONSTRCT','ETCONSTRCT','GTCONSTRCT'))
     then

      raise exception -746, 0,
        'FAIL!! abbreviation must be lower case for marker type ' || vMrkrType ;
    elif
        (vMrkrAbbrev != upper(vMrkrAbbrev)  AND
         vMrkrName != upper(vMrkrName) AND
                     vMrkrType = 'REGION')
     then
      
      raise exception -746, 0,
        'FAIL!! name and symbol must be upper case for marker type ' || vMrkrType ;
    else 
                let vOK = 't' ; 
    end if;



  if (vMrkrName[1,11] == 'WITHDRAWN: '  OR  vMrkrAbbrev[1,11] == 'WITHDRAWN: ' )
     then

      raise exception -746, 0,
        'FAIL!! No space following : for WITHDRAWN ' || vMrkrName ;
  end if ;

end procedure;

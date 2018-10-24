create or replace function p_check_mrkr_abbrev (vMrkrName  varchar(255),
                                      vMrkrAbbrev varchar(255),
                                      vMrkrType   varchar(80))

returns void as $$
  declare vOK boolean ;

begin

  if vMrkrAbbrev in (select cv_term_name from controlled_vocabulary) then
   raise exception 'FAIL!! abbreviation can not be a foreign species designation ' ;
  end if;


  if vMrkrType in (select mtgrpmem_mrkr_type
                     from marker_type_group_member
                     where mtgrpmem_mrkr_type_group = 'ABBREV_EQ_NAME')
		     and vMrkrType not in ('TGCONSTRCT','PTCONSTRCT','ETCONSTRCT','GTCONSTRCT') then
    if vMrkrName <> vMrkrAbbrev then
      raise exception
        'FAIL!! abbreviation must exactly equal name for marker type' ;
    end if;
    if exists (select 'x'
                from marker_Type_group_member
                where mtgrpmem_mrkr_type_group = 'ABBREV_EQ_NAME_UPPER'
                and mtgrpmem_mrkr_Type = vMrkrType
                and UPPER(vMrkrName) != vMrkrAbbrev) then     
        raise exception 'FAIL!! abbreviation must be upper case for marker type';
    end if;
  elsif
        (vMrkrAbbrev != lower(vMrkrAbbrev)  AND
                     substring(vMrkrAbbrev,1,10) <> 'WITHDRAWN:'  AND
                     vMrkrType not in ('EFG','EREGION','ATB','TGCONSTRCT','PTCONSTRCT','ETCONSTRCT','GTCONSTRCT'))
     then

      raise exception 'FAIL!! abbreviation must be lower case for marker type';
    elsif
        (vMrkrAbbrev != upper(vMrkrAbbrev)  AND
         vMrkrName != upper(vMrkrName) AND
                     vMrkrType = 'EREGION')
     then
      
      raise exception 'FAIL!! name and symbol must be upper case for marker type';
    else 
                 vOK = 't' ; 
    end if;



  if (substring(vMrkrName,1,11) = 'WITHDRAWN: '  OR  substring(vMrkrAbbrev,1,11) = 'WITHDRAWN: ' )
     then

      raise exception 'FAIL!! No space following : for WITHDRAWN';
  end if ;
end

$$ LANGUAGE plpgsql

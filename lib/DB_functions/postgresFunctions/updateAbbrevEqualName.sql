
create or replace function updateAbbrevEqualName (vMrkrZdbId text, vMrkrName varchar(255), vMrkrType varchar(10), vMrkrAbbrev varchar(255))
returns varchar(255) as $mrkrAbbrev$

declare mrkrAbbrev  marker.mrkr_abbrev%TYPE;
begin 
if vMrkrType in ('EREGION','EFG','CRISPR','TALEN','MRPHLNO','ATB')
   then
    mrkrAbbrev := vMrkrName;
 else
     mrkrAbbrev := vMrkrAbbrev;
end if;

return mrkrAbbrev; 
end 

$mrkrAbbrev$ LANGUAGE plpgsql;

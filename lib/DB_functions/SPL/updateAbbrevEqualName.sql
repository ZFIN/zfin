
create function updateAbbrevEqualName (vMrkrZdbId varchar(50), vMrkrName varchar(255), vMrkrType varchar(10), vMrkrAbbrev varchar(255))
returning varchar(255)
define mrkrAbbrev like marker.mrkr_abbrev;
if vMrkrType in ('REGION','EFG','CRISPR','TALEN','MRPHLNO')
   then
   let mrkrAbbrev = vMrkrName;
 else
    let mrkrAbbrev = vMrkrAbbrev;
end if;

return mrkrAbbrev; 

end function;

create or replace function updateConstructAbbrev (vMrkrZdbId varchar(50), vMrkrName varchar(255), vMrkrType varchar(10))

returns void as $$

begin
if vMrkrType in ('TGCONSTRCT','PTCONSTRCT','GTCONSTRCT','ETCONSTRCT')
   then

	update marker
    	   set mrkr_abbrev = vMrkrName
  	   where mrkr_zdb_id = vMrkrZdbId;

end if;
end
$$ LANGUAGE plpgsql;

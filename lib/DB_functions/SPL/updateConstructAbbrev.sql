create procedure updateConstructAbbrev (vMrkrZdbId varchar(50), vMrkrName varchar(60), vMrkrType varchar(10))

if vMrkrType in ('TGCONSTRCT','PTCONSTRCT','GTCONSTRCT','ETCONSTRCT')
   then

	update marker
    	   set mrkr_abbrev = vMrkrName
  	   where mrkr_zdb_id = vMrkrZdbId;

end if;

end procedure;

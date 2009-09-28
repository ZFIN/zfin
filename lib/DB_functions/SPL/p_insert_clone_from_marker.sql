create procedure p_insert_clone_from_marker (vMrkrZdbId varchar(50),
       		 			     vMrkrType varchar(30))

if vMrkrType in ('EST','CDNA','BAC','PAC','FOSMID')
and not exists (Select 'x' from clone where clone_mrkr_zdb_id = vMrkrZdbId)

then 
  insert into clone (clone_mrkr_zdb_id)
   values (vMrkrZdbID) ;

end if;

end procedure;
		  
create or replace function p_insert_clone_from_marker (vMrkrZdbId text,
       		 			     vMrkrType varchar(30))
returns void as $$

begin
if vMrkrType in ('EST','CDNA','BAC','PAC','FOSMID')
and not exists (Select 'x' from clone where clone_mrkr_zdb_id = vMrkrZdbId)

then 
  insert into clone (clone_mrkr_zdb_id)
   values (vMrkrZdbID) ;

end if;
end

$$ LANGUAGE plpgsql		  

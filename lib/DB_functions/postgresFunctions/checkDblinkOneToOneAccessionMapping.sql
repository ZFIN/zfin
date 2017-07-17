create or replace function checkDblinkOneToOneAccessionMapping (vDblinkZdbId varchar(50),
       		 				      vFeatureZdbId text,
						      vDblinkAccNum text
)
returns void as $$
begin
	if (not exists (Select 'x' from db_link
	   	       	       where vDblinkZdbId = dblink_zdb_id
			       and vFeatureZdbId = dblink_linked_recid
			       and vDblinkAccNum = dblink_acc_num))
        then
	   raise exception 'FAIL!: dblink does not match one-to-one-accession.';
	end if ;

end
$$ LANGUAGE plpgsql

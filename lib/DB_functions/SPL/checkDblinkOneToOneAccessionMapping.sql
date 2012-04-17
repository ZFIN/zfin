create procedure checkDblinkOneToOneAccessionMapping (vDblinkZdbId varchar(50),
       		 				      vFeatureZdbId varchar(50),
						      vDblinkAccNum varchar(50)
)
	if (not exists (Select 'x' from db_link
	   	       	       where vDblinkZdbId = dblink_zdb_id
			       and vFeatureZdbId = dblink_linked_recid
			       and vDblinkAccNum = dblink_acc_num))
        then
	   raise exception -746,0,"FAIL!: dblink does not match one-to-one-accession.";
	end if ;

end procedure;

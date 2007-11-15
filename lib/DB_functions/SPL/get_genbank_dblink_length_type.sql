-------------------------------------------------------------------------
--This function checks on insert or update of db_link, the length of the
--sequence and the type (represented by the dblink_fdbcont_zdb_id) are
--consistant with those values in the table accession_bank (accession_bank
--holds sequence records updated daily from NCBI/GenBank).  In fact,
--it replaces dblink_fdbcont_zdb_id, and dblink_length if the accession_number
--is from GenBank.
-------------------------------------------------------------------------

  create function get_genbank_dblink_length_type (vDblinkAccNum varchar(30),
						  vDblinkLength integer,
					 	  vDblinkFdbcontZdbId varchar(50))
  returning varchar(50), integer ;

    define vAccbkLength 	like accession_bank.accbk_length;
    define vFdbcontType 	like foreign_db_contains.fdbcont_fdbdt_data_type;
    define vFdbcontZdbID	like db_link.dblink_fdbcont_zdb_id;
    define vUpdateLength	like db_link.dblink_length ;

      if exists (select *
        	       	from accession_bank, foreign_db_contains
  		       	where accbk_acc_num = vDblinkAccNum
                       	and accbk_fdbcont_zdb_id = fdbcont_zdb_id
			and fdbcont_fdb_db_name = 'GenBank')

      then
		if vDblinkFdbcontZdbId is not null then

        		select fdbcont_fdbdt_data_type
	  		  into vFdbcontType
          		  from foreign_db_contains
          		  where fdbcont_zdb_id = vDblinkFdbcontZdbId ;

			if vFdbcontType = 'other' then

 	 			return vDblinkFdbcontZdbId, vDblinkLength ;

			end if ;
		end if ;

             	select accbk_length, accbk_fdbcont_zdb_id
    	       	  into vAccbkLength, vFdbcontZdbId
    	       	  from accession_bank, foreign_db_contains
    	       	  where accbk_acc_num = vDblinkAccNum
		            and accbk_fdbcont_zdb_id = fdbcont_zdb_id
      	       	    and fdbcont_fdbdt_super_type = 'sequence'
      	       	    and fdbcont_organism_common_name = 'Zebrafish'
		            and fdbcont_fdb_db_name = 'GenBank' ;

	        return vFdbcontZdbId, vAccbkLength ;

      else
      
          let vUpdateLength = (select accbk_length
        	       	      	 from accession_bank, foreign_db_contains
  		       		      where accbk_acc_num = vDblinkAccNum
                       		      and accbk_fdbcont_zdb_id = fdbcont_zdb_id
				      and fdbcont_fdb_db_name != 'GenBank'
				      and accbk_length is not null);

          if vDbLinkLength is null and vUpdateLength is not null

          then 

               let vDbLinkLength = vUpdateLength;
	       return vDblinkFdbcontZdbId, vDblinkLength ;

	  else 

	      return vDblinkFdbcontZdbId, vDblinkLength ;

	  end if ;

      end if ;

end function ;
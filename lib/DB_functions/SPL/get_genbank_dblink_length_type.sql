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

    define vAccbkLength 	integer ;
    define vAccbkType   	varchar(40) ;
    define vFdbcontType 	varchar(20) ;
    define vFdbcontZdbID	varchar(50) ;

      if vDblinkFdbcontZdbId is not null then

        select fdbcont_fdbdt_data_type 
	  into vFdbcontType
          from foreign_db_contains
          where fdbcont_zdb_id = vDblinkFdbcontZdbId ;

	if vFdbcontType = 'other' then
              
 	 return vDblinkFdbcontZdbId, vDblinkLength ;

	elif exists (select * 
        	       	from accession_bank 
  		       	where accbk_acc_num = vDblinkAccNum 
                       	and accbk_db_name = 'GenBank')

          then 
        
             select accbk_length, accbk_data_type
	       into vAccbkLength, vAccbkType
    	       from accession_bank
     	       where accbk_acc_num = vDblinkAccNum 
    	       and accbk_db_name = 'GenBank' ;

	     if vAccbkType = 'mRNA'
      		    then 
		      let vAccbkType = 'cDNA' ;
   	     end if ;
   
   	     if vAccbkType = 'DNA'
      		  then 
		    let vAccbkType = 'Genomic' ;

             end if ;
       
             select fdbcont_zdb_id
    	       into vFdbcontZdbId
    	       from foreign_db_contains
    	       where fdbcont_fdbdt_data_type = vAccbkType
      	       and fdbcont_fdbdt_super_type = 'sequence'
      	       and fdbcont_organism_common_name = 'Zebrafish'
               and fdbcont_fdb_db_name =  'GenBank' ;

	end if ;
            return vFdbcontZdbId, vAccbkLength ;

      else  
          
	  return vDblinkFdbcontZdbId, vDblinkLength ;

      end if ;

end function ;

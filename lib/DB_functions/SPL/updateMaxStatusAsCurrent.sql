create procedure updateMaxStatusAsCurrent ()

  define workingPub varchar(50);
  define maxDate datetime year to second;

     foreach 

       select pth_pub_zdb_id into workingPub
         from pub_tracking_history

	 let maxDate = (select max(pth_status_insert_date)
	     	       from pub_tracking_history
		       where workingPub = pth_pub_zdb_id);

         update pub_tracking_history
           set pth_status_is_current = 't'
	   where maxDate = pth_status_insert_date
	   and pth_pub_zdb_id = workingPub;

     end foreach;

end procedure;

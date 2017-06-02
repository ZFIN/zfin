create trigger dblink_acc_num_update_trigger
  before update of acc_num on db_link
   for each row   
     execute procedure
      get_dblink_acc_num_display(db_name, acc_num)
      into dblink_acc_num_display    
 ;

create procedure p_update_dblink_length (zAccBkAccNum varchar(50), zAccBkFdbcontZdbId varchar(50), vAccBkLength int)

update db_link
  set dblink_length = vAccBkLength
  where dblink_acc_num = zAccBkAccNum
  and dblink_fdbcont_zdb_id = zAccBkFdbcontZdbId ;

end procedure;
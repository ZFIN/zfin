create or replace function p_update_dblink_length (zAccBkAccNum varchar(50), zAccBkFdbcontZdbId text, vAccBkLength int)
returns void as $$

begin
update db_link
  set dblink_length = vAccBkLength
  where dblink_acc_num = zAccBkAccNum
  and dblink_fdbcont_zdb_id = zAccBkFdbcontZdbId ;
end

$$ LANGUAGE plpgsql

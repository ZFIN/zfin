create procedure p_check_caps_acc_num (vFDBcontZdbID varchar(50),
   				       vDblinkAccNum varchar(50))

define vDbName varchar(50);

select distinct fdb_db_name 
  into vDbName
  from foreign_db_contains, foreign_db
  where fdbcont_zdb_id = vFDBcontZdbID 
    and fdbcont_fdb_db_id = fdb_db_pk_id;

if vDbName in ('BLAST', 'GenBank') then
  if vDblinkAccNum <> upper(vDblinkAccNum) then
    raise exception -746,0,'FAIL!!: Acc_num from GenBank,Blast must be uppercase' ;
  end if ;

end if ;

end procedure ;

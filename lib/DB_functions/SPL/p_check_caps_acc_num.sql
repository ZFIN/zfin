create procedure p_check_caps_acc_num (vFDBcontZdbID varchar(50),
   				       vDblinkAccNum varchar(50))

define vDbName varchar(50);

select distinct fdbcont_fdb_db_name 
  into vDbName
  from foreign_db_contains
  where fdbcont_zdb_id = vFDBcontZdbID ;

if vDbName in ('BLAST', 'Genbank') then
  if vDblinkAccNum <> upper(vDblinkAccNum) then
    raise exception -746,0,'FAIL!!: Acc_num from Genbank,Blast must be uppercase' ;
  end if ;

end if ;

end procedure ;

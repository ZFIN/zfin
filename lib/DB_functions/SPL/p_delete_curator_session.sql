create procedure p_delete_curator_session (
      vPubZdbId varchar(50), vClosed datetime year to day)
 if vClosed is not null
 then
  delete from curator_Session 
    where cs_data_zdb_id = vPubZdbId ;
 end if;

end procedure ;
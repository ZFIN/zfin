create or replace function p_delete_curator_session (
      vPubZdbId text, vClosed timestamp )
returns void as $$
begin
 if vClosed is not null
 then
  delete from curator_Session 
    where cs_data_zdb_id = vPubZdbId ;
 end if;
end
$$ LANGUAGE plpgsql

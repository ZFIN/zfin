create or replace function p_set_fish_to_modified(vZdbId text)
returns void as $$

begin
update fish
  set fish_modified = 't'
 where fish_zdb_id = vZdbId;

end 
$$ LANGUAGE plpgsql

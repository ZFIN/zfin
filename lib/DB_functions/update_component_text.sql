create or replace function update_component_text (cc_componentId text)
returns varchar(30) as $mrkrAbbrev$

declare mrkrAbbrev  marker.mrkr_abbrev%TYPE;
begin 
mrkrAbbrev := (select mrkr_abbrev from marker where mrkr_zdb_id = cc_componentId);

return mrkrAbbrev;
end
$mrkrAbbrev$ LANGUAGE plpgsql;

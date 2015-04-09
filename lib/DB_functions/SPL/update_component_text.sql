create function update_component_text (cc_componentId varchar(50))
returning varchar(30);

define mrkrAbbrev like marker.mrkr_abbrev;

let mrkrAbbrev = (select mrkr_abbrev from marker where mrkr_zdb_id = cc_componentId);

return mrkrAbbrev;
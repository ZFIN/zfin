create function get_entity_name_html ( entityZdbId varchar(50) )

  returning lvarchar;

  -- --------------------------------------------------------------------- 
  -- Given the ZDB ID of an entity from anatomy_item or go_term, 
  -- this returns the name of the entity and for anatomy, a link
  -- to the anatomy page.

  --
  -- INPUT VARS: 
  --   entityZdbId   Get the name of this entity
  -- 
  -- OUTPUT VARS: 
  --   none
  -- 
  -- RETURNS: 
  --   Name of entity with proper HTML formatting.
  --   NULL if entityZdbId does not exist in anatomy_item or go_term tables.
  --
  -- EFFECTS:
  --   None
  -- --------------------------------------------------------------------- 

  define entityNameHtml lvarchar;
  define entityName lvarchar; 
  define objType like zdb_object_type.zobjtype_name;
  define objUrl like foreign_db.fdb_db_query;
  define goId like go_term.goterm_go_id;

  let entityName = get_obj_name(entityZdbId);
  let entityNameHtml = NULL;
  let objType = get_obj_type(entityZdbId);

  let entityNameHtml = entityName;

  if (objType = "ANAT") then
    let entityNameHtml = '<a href="/action/anatomy/term-detail?anatomyItem.zdbID=' || entityZdbId || '">' || entityName || '</a>';

  elif (objType = "GOTERM") then
    
    select fdb_db_query into objUrl 
    from foreign_db 
    where fdb_db_name = 'QuickGO';

    select goterm_go_id into goId from go_term where goterm_zdb_id = entityZdbId;

    let entityNameHtml = '<a href="' || objUrl || goId || '">' || entityName || '</a>';

  end if

  return entityNameHtml;

end function;
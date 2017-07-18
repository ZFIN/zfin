-- --------------------------------------------------------------------
-- get_pub_default_permissions is used to look up the journal
-- permissions for a particular pub as it is being inserted
-- into the pub table.  If a journal gives blanket permission
-- to show images, then all the publications in that journal 
-- should also be made to show images.  However, a pub should be 
-- allowed to have image reproduction permission while its parent
-- journal does not, as permissions can be given on a per-pub basis.
-- Therefore, this routine is *NOT* executed on update of pub table.
--
--
-- INPUT VARS: 
--     vPubJrnlZdbId      publication.pub_jrnl_zdb_id 
--
-- OUTPUT VARS:
--     vJrnlPerms	  journal.jrnl_is_nice value for a particular
--                        publication. journal.jrnl_is_nice is a boolean
--			  column that defines journal permissions.
--
-- RETURNS:
--	the journal permissions for the journal to which the pub 
--      of interest belongs.
--
-- EFFECTS: 
--	overwrites the value of publication.pub_can_show_images on insert, 
--	basically sets the default value of publication.pub_can_show_images
--	based on the journal image reproduction priviledges.
--
------------------------------------------------------------------------

create or replace function get_pub_default_permissions (vPubJrnlZdbId text)

  returns boolean as $true$

  declare vJrnlPerms journal.jrnl_is_nice%TYPE ;

  begin

  select jrnl_is_nice
    into vJrnlPerms
    from journal
    where jrnl_zdb_id = vPubJrnlZdbId ;

  return vJrnlPerms ;
  end 

$true$ LANGUAGE plpgsql

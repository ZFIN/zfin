drop function get_obj_name;

create function
get_obj_name(zdbId varchar(50))

  returning varchar(255);  -- longest name in DB is 255 characters long.

  -- Given a ZDB ID, gets the name of the object associated with that ZDB ID.
  -- If the object does not have a name per se, then its ZDB ID is returned
  --   as the name.
  -- Returns NULL if ZDB ID does not point to a record.

  define objType	like zdb_object_type.zobjtype_name;
  define objName	varchar(255);

  let objName = NULL;
  let objType = get_obj_type (zdbId);

  -- list the most likely types first.

  if (objType in (select marker_type
		    from marker_types)) then
    select mrkr_name 
      into objName
      from marker
      where mrkr_zdb_id = zdbId;
  elif (objType = "FISH") then
    let objName = get_fish_full_name(zdbId);
  elif (objType = "MRKRGOEV") then
    select mrkrgoev_zdb_id
      into objName
      from marker_go_term_evidence
      where mrkrgoev_zdb_id = zdbId;	
  elif (objType = "LOCUS") then
    select locus_name 
      into objName
      from locus
      where zdb_id = zdbId;
  elif (objType = "ALT") then
    select allele
      into objName
      from alteration
      where zdb_id = zdbId;
  elif (objType = "LAB") then
    select name 
      into objName
      from lab
      where zdb_id = zdbId;
  elif (objType = "PERS") then
    select name 
      into objName
      from person
      where zdb_id = zdbId;
  elif (objType = "PUB") then
    select title
      into objName
      from publication
      where zdb_id = zdbId;
  elif (objType = "IMAGE") then
    select fimg_zdb_id		-- don't have names, return ZDB ID
      into objName
      from fish_image
      where fimg_zdb_id = zdbId;

  -- Now, list the less frequently hit types in alphabetical order

  elif (objType = "ANAT") then
    select anatitem_name 
      into objName
      from anatomy_item
      where anatitem_zdb_id = zdbId;
  elif (objType = "CHROMO") then
    select print_name
      into objName
      from chromosome
      where zdb_id = zdbId;
  elif (objType = "COMPANY") then
    select name 
      into objName
      from company
      where zdb_id = zdbId;
  elif (objType = "CUR") then
    select cur_zdb_id
      into objName
      from curation
      where cur_zdb_id = zdbId;
  elif (objType = "DALIAS") then
    select dalias_alias
      into objName
      from data_alias
      where dalias_zdb_id = zdbId;
  elif (objType = "DBLINK") then
    select fdbcont_fdb_db_name || ":" || dblink_acc_num
      into objName
      from db_link, foreign_db_contains
      where dblink_zdb_id = zdbId
        and dblink_fdbcont_zdb_id = fdbcont_zdb_id;
  elif (objType = "EXTNOTE") then
    select extnote_zdb_id
      into objName
      from external_note
      where extnote_zdb_id = zdbId;
  elif (objType = "FDBCONT") then
    select fdbcont_zdb_id 
      into objName
      from foreign_db_contains
      where fdbcont_zdb_id = zdbId;
  elif (objType = "GOTERM") then
    select goterm_name
      into objName
      from go_term
      where goterm_zdb_id = zdbId;
  elif (objType = "INFGRP") then
    select infgrp_zdb_id
      into objName
      from inference_group
      where infgrp_zdb_id = zdbId;  --don't have names, return ZDB ID.
  elif (objType = "LABEL") then
    select lbl_name 
      into objName
      from label
      where lbl_zdb_id = zdbId;
  elif (objType = "LINK") then
    select lnkg_zdb_id		-- don't have names, return ZDB ID.
      into objName
      from linkage
      where lnkg_zdb_id = zdbId;
  elif (objType = "LNKGPAIR") then
    select lnkgpair_zdb_id      -- don't have names, use ZDB ID 
      into objName
      from linkage_pair
      where lnkgpair_zdb_id = zdbId;
  elif (objType = "MAPDEL") then
    select allele
      into objName
      from mapped_deletion
      where mapdel_zdb_id = zdbId;
  elif (objType = "MM") then
    select zdb_id		-- don't have names, use ZDB ID
      into objName
      from mapped_marker
      where zdb_id = zdbId;
  elif (objType = "MREL") then  -- Doesn't have name, could use mrel_type
    select mrel_zdb_id
      into objName
      from marker_relationship
      where mrel_zdb_id = zdbId;
  elif (objType = "MRKRGO") then
    select mrkrgo_zdb_id
      into objName
      from marker_go_term
      where mrkrgo_zdb_id = zdbId;
  elif (objType = "NOMEN") then
    select mhist_zdb_id
      into objName
      from marker_history
      where mhist_zdb_id = zdbId;
  elif (objType = "OEVDISP") then
    select oevdisp_zdb_id
      into objName
      from orthologue_evidence_display
      where oevdisp_zdb_id = zdbId;
  elif (objType = "ORTHO") then
    select ortho_name 
      into objName
      from orthologue
      where zdb_id = zdbId;
  elif (objType = "PNOTE") then
    select pnote_zdb_id
      into objName
      from publication_note
      where pnote_zdb_id = zdbId;
  elif (objType = "PRIMER") then
    select zdb_id		-- don't have names, use ZDB ID
      into objName
      from primer_set
      where zdb_id = zdbId;
  elif (objType = "PROBELIB") then
    select probelib_name
      into objName
      from probe_library
      where probelib_zdb_id = zdbId;
  elif (objType = "REFCROSS") then
    select name 
      into objName
      from panels		-- Note: this is a fast search table.
      where zdb_id = zdbId;
  elif (objType = "STAGE") then
    select stg_name		-- other choices exist here.  this is simplest
      into objName
      from stage
      where stg_zdb_id = zdbId;
  elif (objType = "URLREF") then --don't have names, use ZDB ID
    select urlref_zdb_id
      into objName
      from url_ref
      where urlref_zdb_id = zdbId ;
  elif (objType = "XPAT") then
    select xpat_zdb_id		-- don't have names, use ZDB ID
      into objName
      from expression_pattern
      where xpat_zdb_id = zdbId;

  -- and finally 2 oddball cases

  elif (objType = "TEMP") then
    select zdb_id		-- don't return a fish name.  We want to flag 
      into objName		-- this as odd.
      from temp_fish
      where zdb_id = zdbId;
  elif (objType = "sys") then
    select zdb_id		-- not sure why we would ever do this
      into objName
      from return_recs
      where zdb_id = zdbId;				
  end if

  return objName;

end function;

update statistics for function get_obj_name;
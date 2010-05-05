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
  elif (objType = "GENO") then
    select geno_display_name
      into objName
      from genotype
     where geno_zdb_id = zdbId;
  elif (objType = "MRKRGOEV") then
    select get_obj_abbrev(mrkrgoev_mrkr_zdb_id) || ", " ||
           get_obj_name(mrkrgoev_go_term_zdb_id) || ", " || 
	   mrkrgoev_evidence_code || ", " ||
	   get_obj_name(mrkrgoev_source_zdb_id)
      into objName
      from marker_go_term_evidence
      where mrkrgoev_zdb_id = zdbId;
  elif (objType = "EXP") then
    select exp_name 
      into objName
      from experiment
      where exp_zdb_id = zdbId ;
  elif (objType = "FIG") then
    select get_obj_name(fig_source_zdb_id) || " " || fig_label
      into objName
      from figure
      where fig_zdb_id = zdbId ;
  elif (objType = "XPATRES") then
    select get_obj_name(xpatres_xpatex_zdb_id) || ", " || 
           get_obj_name(xpatres_anat_item_zdb_id) || ", " ||
           get_obj_name(xpatres_start_stg_zdb_id) || ", " ||
           get_obj_name(xpatres_end_stg_zdb_id)
      into objName
      from expression_result
      where xpatres_zdb_id = zdbId ;
  elif (objType = "XPATINF") then
    select xpatinf_zdb_id
      into objName
      from expression_pattern_infrastructure
      where xpatinf_zdb_id = zdbId ;	
  elif (objType = "ALT") then
    select feature_name
      into objName
      from feature
      where feature_zdb_id = zdbId;
  elif (objType = "LAB") then
    select name 
      into objName
      from lab
      where zdb_id = zdbId;
  elif (objType = "PERS") then
    select full_name 
      into objName
      from person
      where zdb_id = zdbId;
  elif (objType = "PUB") then
    select title
      into objName
      from publication
      where zdb_id = zdbId;
  elif (objType = "IMAGE") then
    select img_zdb_id		-- don't have names, return ZDB ID
      into objName
      from image
     where img_zdb_id = zdbId;

  -- Now, list the less frequently hit types in alphabetical order

  elif (objType = "ANAT") then
    select anatitem_name 
      into objName
      from anatomy_item
      where anatitem_zdb_id = zdbId;
  elif (objType = "ANATCMK") then
    select anatcmrkr_zdb_id 	-- don't have names, return ZDB ID
      into objName
      from anatomy_canonical_marker
      where anatcmrkr_zdb_id = zdbId;
  elif (objType = "ANATHLOG") then
    select anathlog_anat_item_zdb_id || ":" || anathlog_organism
      into objName
      from anatomy_item_homolog
      where anathlog_zdb_id = zdbId;
   elif (objType = "CDT") then
    select cdt_name
      into objName
      from condition_data_type
      where cdt_zdb_id = zdbId ;
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
  elif (objType = "DNOTE") then
    -- danger, recursive call.  Not sure if it is the right thing to return
    -- the name of the object the note is for, or just the data note's ZDB ID.
    select get_obj_name(dnote_data_zdb_id)
      into objName
      from data_note
      where dnote_zdb_id = zdbId;
  elif (objType = "DBLINK") then
    select fdb_db_name || ":" || dblink_acc_num
      into objName
      from db_link, foreign_db_contains, foreign_db
      where dblink_zdb_id = zdbId
        and fdb_db_pk_id = fdbcont_fdb_db_id
        and dblink_fdbcont_zdb_id = fdbcont_zdb_id;
   elif (objType = "EXPCOND") then    --no name so return zdb_id
    select expcond_zdb_id
      into objName
      from experiment_condition
      where expcond_zdb_id = zdbId;
   elif (objType = "EXPUNIT") then    --no name so return zdb_id
    select expunit_name
      into objName
      from experiment_unit
      where expunit_zdb_id = zdbId;
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
 elif (objType = "JRNL") then
    select jrnl_name
      into objName
      from journal
      where jrnl_zdb_id = zdbId;
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
  elif (objType = "MM") then
    select zdb_id		-- don't have names, use ZDB ID
      into objName
      from mapped_marker
      where zdb_id = zdbId;
  elif (objType = "MREL") then
    select get_obj_abbrev(mrel_mrkr_1_zdb_id) || " " ||
           mreltype_1_to_2_comments || " " ||
           get_obj_abbrev(mrel_mrkr_2_zdb_id)           
      into objName
      from marker_relationship, marker_relationship_type
      where mrel_zdb_id = zdbId
        and mrel_type = mreltype_name;
  elif (objType = "MRKRGO") then
    select mrkrgo_zdb_id
      into objName
      from marker_go_term
      where mrkrgo_zdb_id = zdbId;
  elif (objType = "MRKRSEQ") then
    select mrkrseq_zdb_id
      into objName
      from marker_sequence
      where mrkrseq_zdb_id = zdbId;
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
  elif (objType = "XPAT") then --don't have names, use ZDB ID
    select xpatex_zdb_id
      into objName
      from expression_experiment
      where xpatex_zdb_id = zdbId ;
  elif (objType = "XPATRES") then
    select xpatres_zdb_id		-- don't have names, use ZDB ID
      into objName
      from expression_results
      where xpatres_zdb_id = zdbId;
  elif (objType = "APATO") then
    select apato_zdb_id		-- don't have names, use ZDB ID
      into objName
      from atomic_phenotype
      where apato_zdb_id = zdbId;
  elif (objType = "API") then
    select api_zdb_id		-- don't have names, use ZDB ID
      into objName
      from apato_infrastructure
      where api_zdb_id = zdbId;
  elif (objType = "FHIST") then
    select fhist_zdb_id		-- don't have names, use ZDB ID
      into objName
      from feature_history
      where fhist_zdb_id = zdbId;
  elif (objType = "FMREL") then
    select fmrel_zdb_id		-- don't have names, use ZDB ID
      into objName
      from feature_marker_relationship
      where fmrel_zdb_id = zdbId;
  elif (objType = "GENOFEAT") then
    select genofeat_zdb_id		-- don't have names, use ZDB ID
      into objName
      from genotype_feature
      where genofeat_zdb_id = zdbId;
  elif (objType = "GENOX") then
    select genox_zdb_id		-- don't have names, use ZDB ID
      into objName
      from genotype_experiment
      where genox_zdb_id = zdbId;
  elif (objType = "TERM") then
    select term_name		
      into objName
      from term
      where term_zdb_id = zdbId;
  elif (objType = "TERMDEF") then
    select termdef_zdb_id		-- don't have names, use ZDB ID
      into objName
      from term_definition
      where termdef_zdb_id = zdbId;
  elif (objType = "TERMREL") then
    select termrel_zdb_id		-- don't have names, use ZDB ID
      into objName
      from term_relationship
      where termrel_zdb_id = zdbId;
  elif (objType = "ZYG") then
    select zyg_name		-- don't have names, use ZDB ID
      into objName
      from zygocity
      where zyg_zdb_id = zdbId;

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

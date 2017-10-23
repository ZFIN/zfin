create or replace function regen_names_marker_list()
returns void as $$

  -- ---------------------------------------------------------------------
  -- regen_names_marker_list generates all names for the markers identified by 
  -- the ZDB IDs in the regen_zdb_id_temp table and adds the names to the
  -- regen_all_names_temp table.
  --
  -- PRECONDITIONS:
  --   regen_zdb_id_temp table exists and contains a list of marker ZDB IDs
  --     to get names for.
  --   regen_all_names_temp table exists.  It may contain data, but it does
  --     not contain any data for markers.
  --
  -- INPUT VARS:
  --   none
  --
  -- OUTPUT VARS:
  --   none
  --
  -- RETURNS:
  --   Success: Nothing
  --   Failure: Throws whatever exception happened.
  --
  -- EFFECTS:
  --   Success:
  --     regen_all_names_temp contains all names for markers that were 
  --       identified in regen_zdb_id_temp table.
  --   Error:
  --     regen_all_names_temp may or may not have new data in it.
  --     transaction is not committed or rolled back.
  -- ---------------------------------------------------------------------

  declare namePrecedence  name_precedence.nmprec_precedence%TYPE := 'Accession number';
  	  nameSignificance  name_precedence.nmprec_significance%TYPE ;

  -- -------------------------------------------------------------------
  -- -------------------------------------------------------------------
  --   Get Marker ACCESSION NUMBERS into all_m_names_new.
  -- -------------------------------------------------------------------
  -- -------------------------------------------------------------------

  -- Extract out accession numbers for other databases from db_links
  -- for markers.  

  -- The "distinct" below is needed because many acc_num/linked_recid
  --   combinations have an entry for GenBank and an entry for BLAST.

begin
  select nmprec_significance 
    into nameSignificance
    from name_precedence
    where nmprec_precedence = namePrecedence;

  insert into regen_all_names_temp
      ( rgnallnm_name, rgnallnm_zdb_id, rgnallnm_significance,
        rgnallnm_precedence, rgnallnm_name_lower )
    select distinct dblink_acc_num, dblink_linked_recid, nameSignificance, 
                    namePrecedence, lower(dblink_acc_num)
      from db_link, regen_zdb_id_temp
      where dblink_linked_recid = rgnz_zdb_id;


  insert into regen_all_names_temp
      ( rgnallnm_name, rgnallnm_zdb_id, rgnallnm_significance,
        rgnallnm_precedence, rgnallnm_name_lower )
    select distinct dalias_alias, dblink_linked_recid, nameSignificance, 
           namePrecedence, dalias_alias_lower
   from db_link, data_alias, regen_zdb_id_temp
  where dalias_data_zdb_id = dblink_zdb_id 
    and dblink_linked_recid = rgnz_zdb_id;


  insert into regen_all_names_temp
      ( rgnallnm_name, rgnallnm_zdb_id, rgnallnm_significance,
        rgnallnm_precedence, rgnallnm_name_lower )
    select distinct oef_accession_number, ortho_zebrafish_gene_zdb_id, nameSignificance, 
                    namePrecedence, lower(oef_accession_number)
      from ortholog_external_reference, ortholog, regen_zdb_id_temp
      where oef_ortho_zdb_id = ortho_zdb_id
        and ortho_zebrafish_gene_zdb_id = rgnz_zdb_id;



  -- -------------------------------------------------------------------
  -- -------------------------------------------------------------------
  --   Get MARKER NAMES into all_m_names_new.
  -- -------------------------------------------------------------------
  -- -------------------------------------------------------------------

  -- -------------------------------------------------------------------
  --   Get ortholog names for markers into all_m_names_new.
  -- -------------------------------------------------------------------

  namePrecedence = 'Ortholog';
  select nmprec_significance 
    into nameSignificance
    from name_precedence 
    where nmprec_precedence = namePrecedence;

  insert into regen_all_names_temp
      ( rgnallnm_name, rgnallnm_zdb_id, rgnallnm_significance,
	rgnallnm_precedence, rgnallnm_name_lower )
    select distinct ortho_other_species_symbol, ortho_zebrafish_gene_zdb_id, nameSignificance, namePrecedence,
	     lower(ortho_other_species_symbol)
      from ortholog, regen_zdb_id_temp
      where ortho_other_species_symbol is not null
        and ortho_zebrafish_gene_zdb_id = rgnz_zdb_id
	;


  insert into regen_all_names_temp
      ( rgnallnm_name, rgnallnm_zdb_id, rgnallnm_significance,
	rgnallnm_precedence, rgnallnm_name_lower )
    select distinct ortho_other_species_name, ortho_zebrafish_gene_zdb_id allmapnm_zdb_id, nameSignificance,
                    namePrecedence, lower(ortho_other_species_name)
      from ortholog,regen_zdb_id_temp
      where ortho_other_species_name is not null
        and ortho_zebrafish_gene_zdb_id = rgnz_zdb_id
         and ortho_other_species_name != ortho_other_species_symbol;


  -- -------------------------------------------------------------------
  --   Get aliases for markers into all_m_names_new.
  -- -------------------------------------------------------------------

  namePrecedence = 'Previous name';
  select nmprec_significance 
    into nameSignificance
    from name_precedence 
    where nmprec_precedence = namePrecedence;

  insert into regen_all_names_temp
      ( rgnallnm_name, rgnallnm_zdb_id, rgnallnm_significance,
	rgnallnm_precedence, rgnallnm_name_lower )
    select distinct dalias_alias, dalias_data_zdb_id, nameSignificance,
                    namePrecedence, dalias_alias_lower
      from data_alias, alias_group, regen_zdb_id_temp
      where rgnz_zdb_id = dalias_data_zdb_id
  	and dalias_group_id = aliasgrp_pk_id
  	and aliasgrp_name = 'alias';


  namePrecedence = 'Sequence similarity';
  select nmprec_significance 
    into nameSignificance
    from name_precedence 
    where nmprec_precedence = namePrecedence;

  insert into regen_all_names_temp
      ( rgnallnm_name, rgnallnm_zdb_id, rgnallnm_significance,
	rgnallnm_precedence, rgnallnm_name_lower )
    select distinct dalias_alias, dalias_data_zdb_id, nameSignificance,
                    namePrecedence, dalias_alias_lower
      from data_alias, alias_group, regen_zdb_id_temp
      where rgnz_zdb_id = dalias_data_zdb_id
 	and dalias_group_id = aliasgrp_pk_id
  	and aliasgrp_name = 'sequence similarity';

	
  -- -------------------------------------------------------------------
  --   Get MARKER NAMES and SYMBOLS into all_m_names_new.
  -- -------------------------------------------------------------------

  namePrecedence = 'Current symbol';
  select nmprec_significance 
    into nameSignificance
    from name_precedence 
    where nmprec_precedence = namePrecedence;

  insert into regen_all_names_temp
      ( rgnallnm_name, rgnallnm_zdb_id, rgnallnm_significance,
	rgnallnm_precedence, rgnallnm_name_lower )
    select mrkr_abbrev, mrkr_zdb_id, nameSignificance, namePrecedence, 
           lower(mrkr_abbrev)
      from marker, regen_zdb_id_temp
      where mrkr_zdb_id = rgnz_zdb_id;


  namePrecedence = 'Current name';
  select nmprec_significance 
    into nameSignificance
    from name_precedence 
    where nmprec_precedence = namePrecedence;

  insert into regen_all_names_temp
      ( rgnallnm_name, rgnallnm_zdb_id, rgnallnm_significance,
	rgnallnm_precedence, rgnallnm_name_lower )
    select mrkr_name, mrkr_zdb_id, nameSignificance, namePrecedence, 
           lower(mrkr_name)
      from marker, regen_zdb_id_temp
      where lower(mrkr_abbrev) <> lower(mrkr_name)
        and mrkr_zdb_id = rgnz_zdb_id
        and not exists
            ( select 'x'
		     from marker_type_group_member
		     where mtgrpmem_mrkr_type_group = 'SEARCH_SEG'
                     and mrkr_type = mtgrpmem_mrkr_type );


  namePrecedence = 'Clone name';
  select nmprec_significance 
    into nameSignificance
    from name_precedence 
    where nmprec_precedence = namePrecedence;

  insert into regen_all_names_temp
      ( rgnallnm_name, rgnallnm_zdb_id, rgnallnm_significance,
	rgnallnm_precedence, rgnallnm_name_lower )
    select mrkr_name, mrkr_zdb_id, nameSignificance, namePrecedence,
           lower(mrkr_name)
      from marker, regen_zdb_id_temp
      where lower(mrkr_abbrev) <> lower(mrkr_name)
        and rgnz_zdb_id = mrkr_zdb_id
        and exists
            ( select 'x'
		     from marker_type_group_member
		     where mtgrpmem_mrkr_type_group = 'SEARCH_SEG'
                     and mrkr_type = mtgrpmem_mrkr_type );



  -- remove less significant dups

  perform regen_names_drop_dups();

  -- generate all name ends.  Takes regen_zdb_id_temp, regen_all_names_temp
  -- as input, adds recs to regen_all_name_ends_temp

  perform regen_name_ends_list();

end;

$$ LANGUAGE plpgsql;

create procedure 
mhist_event( active_marker varchar(50), 
	     event varchar(40), 
	     new_value varchar(255), 
             old_value varchar(255) )

-- This procedure is triggered by updates to Marker.mrkr_name and 
-- Marker.mrkr_abbrev. The procedure is only concerned about GENEs
-- and ignores other marker types. 

-- Expected values:
-- active_marker: the mrkr_zdb_id
-- event: the history event
-- new_value: the value being inserted
-- old_value: the value being replaced 
-- (exception: old_value is mrkr_abbrev for assigned event)

-- The active marker is the marker being updated. Updates to the marker name
-- and abbrev are stored as aliases in Data_Alias and the event is logged in  
-- in Marker_History.


  --define global variables  
  DEFINE marker_type varchar(50);	
  DEFINE nomen_zdb_id varchar(50);
  DEFINE data_zdb_id  varchar(50);
  DEFINE count	integer;
  DEFINE temp date;

IF (new_value != old_value or event = "assigned") THEN


  --Get the active_marker marker-type 
  SELECT mrkr_type 
  INTO marker_type
  FROM marker
  WHERE mrkr_zdb_id = active_marker
    AND mrkr_type in (select mtgrpmem_mrkr_type from marker_type_group_member
                       where mtgrpmem_mrkr_type_group = "GENEDOM"
			or mtgrpmem_mrkr_type_group = "CONSTRUCT"
			or mtgrpmem_mrkr_type_group = "TRANSCRIPT" );



  IF (marker_type <> '') THEN  
  --------------------------------------------------------------------
      --Get MARKER_HISTORY zdb_id
      LET nomen_zdb_id = get_id('NOMEN');
      INSERT INTO zdb_active_data VALUES(nomen_zdb_id);

      IF (event = "assigned")  THEN
      ----------------------------------------------------------------  
          INSERT INTO marker_history ( mhist_zdb_id, 
                     mhist_mrkr_zdb_id, 
                     mhist_event,
                     mhist_reason, 
                     mhist_date,
                     mhist_mrkr_name_on_mhist_date, 
                     mhist_mrkr_abbrev_on_mhist_date, 
                     mhist_comments )
          VALUES ( nomen_zdb_id, 
                     active_marker,
                     'assigned',
                     'Not Specified', 
                     CURRENT,
                     new_value, 
                     old_value,
                     '' );
      END IF  -------(event = assigned)--------
      ------------------------------------------------------------------

      IF (event = "reassigned")  THEN
      ----------------------------------------------------------------
          --Get DALIAS zdb_id if alias doesn't exist
          ------------------------------------------
          LET count = 0;
 
          SELECT count(*)::INTEGER
          INTO count
          FROM data_alias
          WHERE dalias_data_zdb_id = active_marker
            AND dalias_alias = old_value;

          IF (count = 0) THEN
            LET data_zdb_id  = get_id('DALIAS');
            INSERT INTO zdb_active_data VALUES(data_zdb_id);
            INSERT INTO data_alias (dalias_zdb_id, dalias_data_zdb_id,
					dalias_alias,dalias_group_id,
					dalias_alias_lower)
                   VALUES(data_zdb_id,active_marker,old_value,(select aliasgrp_pk_id from alias_group where aliasgrp_name ='alias'),
				lower(old_value));
          ELSE
            SELECT dalias_zdb_id
            INTO data_zdb_id
            FROM data_alias
            WHERE dalias_data_zdb_id = active_marker
              AND dalias_alias = old_value;
          END IF
          -------------------------------------------

          INSERT INTO marker_history ( mhist_zdb_id, mhist_mrkr_zdb_id, 
                     mhist_event, mhist_reason, mhist_date,
                     mhist_mrkr_name_on_mhist_date,
                     mhist_mrkr_abbrev_on_mhist_date,
                     mhist_comments,
                     mhist_dalias_zdb_id )
          SELECT nomen_zdb_id, 
                     active_marker,
                     'reassigned', 
                     'Not Specified', 
                     CURRENT, 
                     mrkr_name, 
                     new_value,
                     'none',
                     data_zdb_id
          FROM marker
          WHERE mrkr_zdb_id = active_marker;

      END IF -------(marker event = reassigned)--------
      ------------------------------------------------------------------  

      IF (event = "renamed") THEN
      ------------------------------------------------------------------
          INSERT INTO marker_history ( mhist_zdb_id, mhist_mrkr_zdb_id, 
                      mhist_event, mhist_reason, mhist_date,
                      mhist_mrkr_name_on_mhist_date,
                      mhist_mrkr_abbrev_on_mhist_date,
                      mhist_comments,
                      mhist_mrkr_prev_name )
          SELECT nomen_zdb_id, 
                      active_marker,
                      'renamed', 
                      'Not Specified', 
                      CURRENT, 
                      new_value, 
                      mrkr_abbrev,
                      'none',
                      old_value
          FROM marker
          WHERE mrkr_zdb_id = active_marker;
      END IF  -------(event = renamed)--------
      ------------------------------------------------------------------

  END IF  -------(marker type in group GENEDOM or CONSTRUCT)--------

END IF  -------(new != old or assigned)--------

end procedure;

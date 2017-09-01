create or replace function 
fhist_event( active_feature text, 
	     event text, 
	     new_value text, 
             old_value text )
returns void as $$



-- This procedure is triggered by updates to Feature.ftr_name and 
-- Feature.ftr_abbrev.

-- Expected values:
-- active_feature: the ftr_zdb_id
-- event: the history event
-- new_value: the value being inserted
-- old_value: the value being replaced 
-- (exception: old_value is ftr_abbrev for assigned event)

-- The active feature is the feature being updated. Updates to the feature name
-- and abbrev are stored as aliases in Data_Alias and the event is logged in  
-- in Feature_History.


  --define global variables  
  DECLARE vfeature_type text;	
   nomen_zdb_id text;
   data_zdb_id  text;
   count	integer;
   temp date;
  BEGIN 

IF (new_value != old_value or event = 'assigned') THEN


  --Get the active_feature feature-type 
  SELECT feature_type 
  INTO vfeature_type
  FROM feature
  WHERE feature_zdb_id = active_feature
    AND feature_type in (select ftrgrpmem_ftr_type 
			   from feature_type_group_member
                           where ftrgrpmem_ftr_type_group = 'MUTANT');



  IF (vfeature_type <> '') THEN  
  --------------------------------------------------------------------
      --Get FEATURE_HISTORY zdb_id
      nomen_zdb_id = get_id('FHIST');
      INSERT INTO zdb_active_data VALUES(nomen_zdb_id);

      IF (event = 'assigned')  THEN
      ----------------------------------------------------------------  
          INSERT INTO feature_history ( fhist_zdb_id, 
                     fhist_ftr_zdb_id, 
                     fhist_event,
                     fhist_reason, 
                     fhist_date,
                     fhist_ftr_name_on_fhist_date, 
                     fhist_ftr_abbrev_on_fhist_date, 
                     fhist_comments )
          VALUES ( nomen_zdb_id, 
                     active_feature,
                     'assigned',
                     'Not Specified', 
                     CURRENT,
                     new_value, 
                     old_value,
                     '' );
      END IF;  -------(event = assigned)--------
      ------------------------------------------------------------------

      IF (event = 'reassigned')  THEN
      ----------------------------------------------------------------
          --Get DALIAS zdb_id if alias doesn't exist
          ------------------------------------------
          count = 0;
 
          SELECT count(*)::INTEGER
          INTO count
          FROM data_alias
          WHERE dalias_data_zdb_id = active_feature
            AND dalias_alias = old_value;

          IF (count = 0) THEN
            data_zdb_id  = get_id('DALIAS');
            INSERT INTO zdb_active_data VALUES(data_zdb_id);
            INSERT INTO data_alias (dalias_zdb_id, dalias_data_zdb_id,
					dalias_alias,dalias_group_id,
					dalias_alias_lower)
                   VALUES(data_zdb_id,active_feature,old_value,(select aliasgrp_pk_id from alias_group where aliasgrp_name='alias'),
				lower(old_value));
	 	
          ELSE
            SELECT dalias_zdb_id
            INTO data_zdb_id
            FROM data_alias
            WHERE dalias_data_zdb_id = active_feature
              AND dalias_alias = old_value;

	   
          END IF;
          -------------------------------------------

          INSERT INTO feature_history ( fhist_zdb_id, fhist_ftr_zdb_id, 
                     fhist_event, fhist_reason, fhist_date,
                     fhist_ftr_name_on_fhist_date,
                     fhist_ftr_abbrev_on_fhist_date,
                     fhist_comments,
                     fhist_dalias_zdb_id )
          SELECT nomen_zdb_id, 
                     active_feature,
                     'reassigned', 
                     'Not Specified', 
                     CURRENT, 
                     feature_name, 
                     new_value,
                     'none',
                     data_zdb_id
          FROM feature
          WHERE feature_zdb_id = active_feature;

      END IF ;-------(feature event = reassigned)--------
      ------------------------------------------------------------------  

      IF (event = 'renamed') THEN
      ------------------------------------------------------------------
          INSERT INTO feature_history ( fhist_zdb_id, fhist_ftr_zdb_id, 
                      fhist_event, fhist_reason, fhist_date,
                      fhist_ftr_name_on_fhist_date,
                      fhist_ftr_abbrev_on_fhist_date,
                      fhist_comments,
                      fhist_ftr_prev_name )
          SELECT nomen_zdb_id, 
                      active_feature,
                      'renamed', 
                      'Not Specified', 
                      CURRENT, 
                      new_value, 
                      feature_abbrev,
                      'none',
                      old_value
          FROM feature
          WHERE feature_zdb_id = active_feature;
      END IF;  -------(event = renamed)--------
      ------------------------------------------------------------------

  END IF ; -------(feature type in group GENEDOM)--------

END IF;  -------(new != old or assigned)--------

END;

$$ LANGUAGE plpgsql;

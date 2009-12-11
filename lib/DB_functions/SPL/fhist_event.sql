create procedure 
fhist_event( active_feature varchar(50), 
	     event varchar(40), 
	     new_value varchar(255), 
             old_value varchar(255) )

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
  DEFINE vfeature_type varchar(50);	
  DEFINE nomen_zdb_id varchar(50);
  DEFINE data_zdb_id  varchar(50);
  DEFINE count	integer;
  DEFINE temp date;

IF (new_value != old_value or event = "assigned") THEN


  --Get the active_feature feature-type 
  SELECT feature_type 
  INTO vfeature_type
  FROM feature
  WHERE feature_zdb_id = active_feature
    AND feature_type in (select ftrgrpmem_ftr_type 
			   from feature_type_group_member
                           where ftrgrpmem_ftr_type_group = "MUTANT");



  IF (vfeature_type <> '') THEN  
  --------------------------------------------------------------------
      --Get FEATURE_HISTORY zdb_id
      LET nomen_zdb_id = get_id('FHIST');
      INSERT INTO zdb_active_data VALUES(nomen_zdb_id);

      IF (event = "assigned")  THEN
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
          WHERE dalias_data_zdb_id = active_feature
            AND dalias_alias = old_value;

          IF (count = 0) THEN
            LET data_zdb_id  = get_id('DALIAS');
            INSERT INTO zdb_active_data VALUES(data_zdb_id);
            INSERT INTO data_alias (dalias_zdb_id, dalias_data_zdb_id,
					dalias_alias,dalias_group_id,
					dalias_alias_lower)
                   VALUES(data_zdb_id,active_feature,old_value,(select aliasgrp_pk_id from alias_group where aliasgrp_name='alias'),
				lower(old_value));
	 	
	    if not exists (select 'x'
				from record_attribution
				where recattrib_data_zdb_id = 
					data_zdb_id
				and recattrib_source_zdb_id = 
					'ZDB-PUB-030508-1'
				and recattrib_source_type = 'standard')
	    then 
	      insert into record_attribution (recattrib_data_zdb_id,
						recattrib_source_zdb_id,
						recattrib_source_type)
                 values (data_zdb_id,'ZDB-PUB-030508-1','standard');
	    end if ;
          ELSE
            SELECT dalias_zdb_id
            INTO data_zdb_id
            FROM data_alias
            WHERE dalias_data_zdb_id = active_feature
              AND dalias_alias = old_value;

	    if not exists (select 'x'
				from record_attribution
				where recattrib_data_zdb_id = 
					data_zdb_id
				and recattrib_source_zdb_id = 
					'ZDB-PUB-030508-1'
				and recattrib_source_type = 'standard')
	    then 
	      insert into record_attribution (recattrib_data_zdb_id,
						recattrib_source_zdb_id,
						recattrib_source_type)
                 values (data_zdb_id,'ZDB-PUB-030508-1','standard');
	    end if ;
	   
          END IF
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

      END IF -------(feature event = reassigned)--------
      ------------------------------------------------------------------  

      IF (event = "renamed") THEN
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
      END IF  -------(event = renamed)--------
      ------------------------------------------------------------------

  END IF  -------(feature type in group GENEDOM)--------

END IF  -------(new != old or assigned)--------

end procedure;

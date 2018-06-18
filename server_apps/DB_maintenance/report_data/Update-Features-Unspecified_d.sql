SELECT feature_zdb_id,
       feature_name,
       mrkr_abbrev
FROM   feature,
       marker,
       feature_marker_relationship
WHERE  feature_zdb_id = fmrel_ftr_zdb_id
       AND mrkr_zdb_id = fmrel_mrkr_zdb_id
       AND feature_name LIKE '%\_unspecified'
       AND feature_type = 'UNSPECIFIED'
       AND feature_name != mrkr_abbrev
           || '_unspecified'
UNION
SELECT feature_zdb_id,
       feature_name,
       mrkr_abbrev
FROM   feature,
       marker,
       feature_marker_relationship
WHERE  feature_zdb_id = fmrel_ftr_zdb_id
       AND mrkr_zdb_id = fmrel_mrkr_zdb_id
       AND feature_name LIKE '%\_unrecovered'
       AND feature_name != mrkr_abbrev
           || '_unrecovered' ;

UPDATE feature
SET    feature_abbrev = (SELECT mrkr_abbrev||'_unspecified'

                       FROM   marker,
                              feature_marker_relationship
                       WHERE  mrkr_zdb_id = fmrel_mrkr_zdb_id
                              AND feature_zdb_id = fmrel_ftr_zdb_id
                              AND (feature_abbrev LIKE '%\_unspecified' or feature_name like '%\_unspecified')
			      and get_obj_type(fmrel_mrkr_zdb_id) not in ('CRISPR','TALEN','MRPHLNO'))
 where (feature_name like '%\_unspecified' or feature_abbrev like '%\_unspecified')
 and feature_abbrev != (SELECT mrkr_abbrev||'_unspecified'

                       FROM   marker,
                              feature_marker_relationship
                       WHERE  mrkr_zdb_id = fmrel_mrkr_zdb_id
                              AND feature_zdb_id = fmrel_ftr_zdb_id
			     and get_obj_type(fmrel_mrkr_zdb_id) not in ('CRISPR','TALEN','MRPHLNO') );

update feature
 set feature_name = feature_abbrev
 where feature_abbrev like '%\_unspecified'
 and feature_name != feature_abbrev;


UPDATE feature
SET    feature_abbrev = (SELECT mrkr_abbrev||'_unrecovered'
                       FROM   marker,
                              feature_marker_relationship
                       WHERE  mrkr_zdb_id = fmrel_mrkr_zdb_id
                              AND feature_zdb_id = fmrel_ftr_zdb_id
                              AND (feature_abbrev LIKE '%\_unrecovered' or feature_name like '%\_unrecovered')
			      and get_obj_type(fmrel_mrkr_zdb_id) not in ('CRISPR','TALEN','MRPHLNO'))
 where (feature_name like '%\_unrecovered' or feature_abbrev like '%\_unrecovered')
 and feature_abbrev != (SELECT mrkr_abbrev||'_unrecovered'
                       FROM   marker,
                              feature_marker_relationship
                       WHERE  mrkr_zdb_id = fmrel_mrkr_zdb_id
                              AND feature_zdb_id = fmrel_ftr_zdb_id
			     and get_obj_type(fmrel_mrkr_zdb_id) not in ('CRISPR','TALEN','MRPHLNO') );


update feature
 set feature_name = feature_abbrev
 where feature_abbrev like '%\_unrecovered'
 and feature_name != feature_abbrev;

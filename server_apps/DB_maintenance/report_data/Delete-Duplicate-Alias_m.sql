-- delete redundant alias which cannot contribute to history
-- because they are un-attributed and those where the
-- redundant alias attribution echoes the marker attribution

CREATE temp table tmp_dup_alias as
SELECT dalias_zdb_id aliasID,
       dalias_alias,
       dalias_data_zdb_id,
       mrkr_abbrev,
       dalias_group_id,
       aliasgrp_name,
       dalias_scope_id,
       aliasscope_scope
FROM   data_alias
       LEFT OUTER JOIN alias_scope
              ON aliasscope_pk_id = dalias_scope_id
       JOIN marker
              ON dalias_data_zdb_id = mrkr_zdb_id
       JOIN alias_group
              ON dalias_group_id = aliasgrp_pk_id
WHERE  dalias_alias = mrkr_abbrev
       AND dalias_group_id = 1
       AND NOT EXISTS (SELECT 't'
                       FROM   record_attribution
                       WHERE  recattrib_data_zdb_id = dalias_zdb_id)
UNION
SELECT dalias_zdb_id AS aliasID,
       dalias_alias,
       dalias_data_zdb_id,
       mrkr_abbrev,
       dalias_group_id,
       aliasgrp_name,
       dalias_scope_id,
       aliasscope_scope
FROM   data_alias
       LEFT OUTER JOIN alias_scope
              ON aliasscope_pk_id = dalias_scope_id
       JOIN marker
              ON dalias_data_zdb_id = mrkr_zdb_id
       JOIN alias_group
              ON dalias_group_id = aliasgrp_pk_id
       JOIN record_attribution redundant
              ON redundant.recattrib_data_zdb_id = dalias_zdb_id
       JOIN record_attribution existant
              ON mrkr_zdb_id = existant.recattrib_data_zdb_id
WHERE  dalias_group_id = 1
       AND dalias_alias = mrkr_abbrev
       AND redundant.recattrib_source_zdb_id = existant.recattrib_source_zdb_id
       AND aliasscope_pk_id = dalias_scope_id
       AND dalias_group_id = aliasgrp_pk_id
       AND dalias_data_zdb_id = mrkr_zdb_id;

--! echo "dump the records of redundant alias into report file"
unload to 'Delete-Duplicate-Aliases'
SELECT *
FROM   tmp_dup_alias
ORDER  BY 3;

--! echo "disconnect alias history from alias"
CREATE temp table tmp_dup_mhist as
SELECT mhist_zdb_id aliasID
FROM   marker_history
WHERE  EXISTS
       (
              SELECT 't'
              FROM   tmp_dup_alias
              WHERE  aliasID = mhist_dalias_zdb_id );

UPDATE marker_history
SET    mhist_dalias_zdb_id = NULL
WHERE  mhist_zdb_id IN (SELECT aliasID
                        FROM   tmp_dup_mhist);

--! echo "delete alias"
DELETE
FROM   zdb_active_data
WHERE  EXISTS
       (
              SELECT 't'
              FROM   tmp_dup_alias
              WHERE  aliasID = zactvd_zdb_id);

commit work;

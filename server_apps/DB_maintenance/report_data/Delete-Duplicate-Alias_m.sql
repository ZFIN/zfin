-- delete redundant alias which cannot contribute to history
-- because they are un-attributed and those where the
-- redundant alias attribution echoes the marker attribution

begin work;
! echo "find redundant alias without attribution"
! echo " and "
! echo "find redundant alias with redundant attribution"
SELECT dalias_zdb_id aliasID,
       dalias_alias,
       dalias_data_zdb_id,
       mrkr_abbrev,
       dalias_group_id,
       aliasgrp_name,
       dalias_scope_id,
       aliasscope_scope
FROM   data_alias,
       OUTER(alias_scope),
       marker,
       alias_group
WHERE  dalias_data_zdb_id == mrkr_zdb_id
       AND dalias_group_id == aliasgrp_pk_id
       AND dalias_alias == mrkr_abbrev
       AND dalias_group_id == 1
       AND aliasscope_pk_id = dalias_scope_id
       AND NOT EXISTS (SELECT 't'
                       FROM   record_attribution
                       WHERE  recattrib_data_zdb_id == dalias_zdb_id)
UNION
SELECT dalias_zdb_id AS aliasID,
       dalias_alias,
       dalias_data_zdb_id,
       mrkr_abbrev,
       dalias_group_id,
       aliasgrp_name,
       dalias_scope_id,
       aliasscope_scope
FROM   data_alias,
       OUTER(alias_scope),
       marker,
       alias_group,
       record_attribution AS redundant,
       record_attribution AS existant
WHERE  dalias_group_id == 1
       AND dalias_alias == mrkr_abbrev
       AND redundant.recattrib_data_zdb_id == dalias_zdb_id
       AND mrkr_zdb_id == existant.recattrib_data_zdb_id
       AND redundant.recattrib_source_zdb_id == existant.recattrib_source_zdb_id
       AND aliasscope_pk_id = dalias_scope_id
       AND dalias_group_id == aliasgrp_pk_id
       AND dalias_data_zdb_id == mrkr_zdb_id
INTO   temp tmp_dup_alias WITH no log;

! echo "dump the records of redundant alias into report file"
unload to 'Delete-Duplicate-Aliases'
SELECT *
FROM   tmp_dup_alias
ORDER  BY 3;

! echo "disconnect alias history from alias"
SELECT mhist_zdb_id aliasID
FROM   marker_history
WHERE  EXISTS
       (
              SELECT 't'
              FROM   tmp_dup_alias
              WHERE  aliasID == mhist_dalias_zdb_id )
INTO   temp tmp_dup_mhist WITH no log;

UPDATE marker_history
SET    mhist_dalias_zdb_id = NULL
WHERE  mhist_zdb_id IN (SELECT aliasID
                        FROM   tmp_dup_mhist);

! echo "delete alias"
DELETE
FROM   zdb_active_data
WHERE  EXISTS
       (
              SELECT 't'
              FROM   tmp_dup_alias
              WHERE  aliasID == zactvd_zdb_id);

drop table tmp_dup_alias;
drop table tmp_dup_mhist;

commit work;
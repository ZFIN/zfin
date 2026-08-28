--liquibase formatted sql
--changeset rtaylor:ZFIN-10025-dedupe-annotation-extension-groups

-- Remove duplicated marker_go_term_annotation_extension_group rows.
--
-- Cause (fixed separately in HibernateMarkerGoTermEvidenceRepository.updateEvidence):
-- mgtaeg_annotation_extension_group_id is IDENTITY-generated, so calling Hibernate save() on an
-- already-persisted group INSERTED a second copy instead of updating it. The GAF/GPAD update path
-- passed the annotation loaded FROM the database, whose collection holds every group already
-- stored, so each run that saw a newer date doubled the set: N -> 2N -> 4N.
--
-- Observed on the 2026.07.05.1 snapshot after a load: syn2b at exactly 2^16 = 65,536 groups and
-- syn1 at ~2^18 = 262,140, each carrying a SINGLE distinct extension value; 327,686 of 328,983
-- extension rows (99.6%) were duplicates. It also made the load's details report unreadable,
-- printing one annotation's with/from-and-extensions across thousands of lines.
--
-- What this keeps: one group per (annotation, distinct extension-set). Groups are compared by
-- their CONTENTS -- the sorted set of (relationship term, identifier term, term text, dblink) of
-- their member extensions -- so genuinely different extension groups on the same annotation are
-- all preserved, and only exact content duplicates collapse to the lowest-id survivor.
--
-- marker_go_term_annotation_extension.mgtae_extension_group_id is ON DELETE CASCADE, so deleting
-- the redundant groups removes their member extensions too.
--
-- Idempotent: re-running finds no duplicates and deletes nothing.

drop table if exists tmp_mgtaeg_dupes;

create temp table tmp_mgtaeg_dupes as
with signature as (
    select g.mgtaeg_annotation_extension_group_id as group_id,
           g.mgtaeg_mrkrgoev_zdb_id               as evidence_id,
           -- content fingerprint; '(no extensions)' keeps childless groups comparable
           coalesce(
               string_agg(
                   e.mgtae_relationship_term_zdb_id
                   || '~' || coalesce(e.mgtae_identifier_term_zdb_id, '')
                   || '~' || coalesce(e.mgtae_term_text, '')
                   || '~' || coalesce(e.mgtae_dblink_zdb_id, ''),
                   ',' order by e.mgtae_relationship_term_zdb_id,
                                coalesce(e.mgtae_identifier_term_zdb_id, ''),
                                coalesce(e.mgtae_term_text, ''),
                                coalesce(e.mgtae_dblink_zdb_id, '')
               ),
               '(no extensions)')                 as contents
      from marker_go_term_annotation_extension_group g
      left join marker_go_term_annotation_extension e
             on e.mgtae_extension_group_id = g.mgtaeg_annotation_extension_group_id
     group by 1, 2
)
-- Rank within (annotation, contents) and drop everything after the first. A correlated
-- "group_id > (select min(...) ...)" subquery is the obvious formulation but is O(n^2) against
-- an unindexed CTE -- on 328,727 groups it ran >15 minutes without finishing. The window
-- function is a single sort.
select group_id
  from (select group_id,
               row_number() over (partition by evidence_id, contents order by group_id) as rn
          from signature) ranked
 where rn > 1;

select 'Duplicate annotation-extension groups to remove: ' || count(*) from tmp_mgtaeg_dupes;

delete from marker_go_term_annotation_extension_group
 where mgtaeg_annotation_extension_group_id in (select group_id from tmp_mgtaeg_dupes);

drop table if exists tmp_mgtaeg_dupes;

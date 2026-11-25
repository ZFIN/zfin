-- ZFIN-10365: indexing priority validation report for the ABC.
--
-- One row per publication whose indexing priority was last predicted by the ABC classifier
-- ("ABC-Indexing Priority Classifier") and that has since been marked indexed. A curator either
-- left that priority alone (confirmed) or set a different one afterwards (corrected); the latest
-- curator priority after the ABC's is the validated one.
--
-- The output is the input of PriorityValidationSubmitTask, which sends the validated priorities
-- back to the ABC (it reads reference_curie, validated_priority and outcome):
--
--   psql -d $DBNAME -A -F $'\t' -P footer=off -f indexingPriorityValidationReport.sql -o indexing-priority-validation.tsv
--   gradle submitPriorityValidation --args="indexing-priority-validation.tsv"           (dry run)
--   gradle submitPriorityValidation --args="indexing-priority-validation.tsv --apply"   (send)
--
-- Every run returns all such publications, including ones already sent; resending a paper
-- overwrites its curator priority with the same value.
with pi as (
  select h.pth_pub_zdb_id pub, h.pth_status_insert_date dt, right(l.ptl_location, 1) prio, h.pth_status_set_by who, p.full_name who_name
  from pub_tracking_history h
  join pub_tracking_location l on l.ptl_pk_id = h.pth_location_id
  join person p on p.zdb_id = h.pth_status_set_by
  where l.ptl_location like 'PUB_INDEXER_%'
), abc as (
  select distinct on (pub) pub, dt, prio from pi
  where who_name = 'ABC-Indexing Priority Classifier'
  order by pub, dt desc
), cur as (
  select distinct on (pi.pub) pi.pub, pi.dt, pi.prio, pi.who_name from pi join abc on abc.pub = pi.pub
  where pi.who_name <> 'ABC-Indexing Priority Classifier' and pi.dt > abc.dt
  order by pi.pub, pi.dt desc
)
select 'ZFIN:' || abc.pub as reference_curie,
       abc.pub as pub_zdb_id,
       abc.prio as abc_predicted_priority,
       to_char(abc.dt, 'YYYY-MM-DD') as abc_assigned_date,
       coalesce(cur.prio, abc.prio) as validated_priority,
       case when cur.prio is not null and cur.prio <> abc.prio then 'corrected' else 'confirmed' end as outcome,
       coalesce(cur.who_name, '') as priority_changed_by,
       coalesce(to_char(cur.dt, 'YYYY-MM-DD'), '') as priority_changed_date,
       ip.full_name as indexed_by,
       to_char(pub.pub_indexed_date, 'YYYY-MM-DD') as indexed_date
from abc
join publication pub on pub.zdb_id = abc.pub and pub.pub_is_indexed
left join cur on cur.pub = abc.pub
left join person ip on ip.zdb_id = pub.pub_indexed_by
order by outcome desc, abc.pub;

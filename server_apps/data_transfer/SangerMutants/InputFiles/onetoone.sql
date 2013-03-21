begin work;

create temp table uniqueEnsdargs (unq_ensdarg varchar(50)) with no log;
load from  uniqueEnsdargs.txt insert into uniqueEnsdargs;

unload to 'onetoonegtot'
select unq_ensdarg  from  uniqueEnsdargs, ensdar_mapping
where trim(unq_ensdarg)=trim(ensm_ensdarg_id)
group by ensm_ensdarg_id, unq_ensdarg
having count(ensm_ensdarg_id) = 1;

unload to 'onetomanygtot'
select unq_ensdarg  from  uniqueEnsdargs, ensdar_mapping
where trim(unq_ensdarg)=trim(ensm_ensdarg_id)
group by ensm_ensdarg_id, unq_ensdarg
having count(ensm_ensdarg_id) > 1;

unload to 'notintable'
select distinct unq_ensdarg  from  uniqueEnsdargs
where trim(unq_ensdarg) not in (select trim(ensm_ensdarg_id) from ensdar_mapping);

rollback work;

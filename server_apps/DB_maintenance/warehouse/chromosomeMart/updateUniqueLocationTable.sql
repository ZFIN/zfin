delete from unique_location;

select count(*) from unique_location;

!echo ' get all the unique marker / feature into table';

insert into unique_location
select sfcl_data_zdb_id, '-1'
from sequence_feature_chromosome_location
group by sfcl_data_zdb_id
having count(distinct sfcl_chromosome) = 1;

select count(*) from unique_location;

!echo 'update the chromosome number';

update unique_location as t set ul_chromosome = (
select distinct ss.sfcl_chromosome from sequence_feature_chromosome_location as ss
where ss.sfcl_data_zdb_id = t.ul_data_zdb_id
);

select count(*) from unique_location;

!echo 'insert genes that have unique PM data';

insert into unique_location
select ss.sfcl_data_zdb_id, '-2'
from sequence_feature_chromosome_location as ss
where ss.sfcl_location_source not in ('General Load','other map location')
and not exists(select 'x' from  unique_location as t where ss.sfcl_data_zdb_id = t.ul_data_zdb_id)
group by sfcl_data_zdb_id
having count(distinct sfcl_chromosome) = 1;

select count(*) from unique_location;

!echo 'update chromosome number of inserted markers';

update unique_location as t set ul_chromosome =
(
select distinct ss.sfcl_chromosome from sequence_feature_chromosome_location as ss
where ss.sfcl_data_zdb_id = t.ul_data_zdb_id
and ss.sfcl_location_source not in ('General Load','other map location')
)
where t.ul_chromosome = '-2';

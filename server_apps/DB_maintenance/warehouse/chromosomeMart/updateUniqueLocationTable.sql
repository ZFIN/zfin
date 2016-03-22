delete from unique_location;

select count(*) from unique_location;

!echo ' get all the unique marker / feature into table';

insert into unique_location
select sfclg_data_zdb_id, '-1'
from sequence_feature_chromosome_location_generated
group by sfclg_data_zdb_id
having count(distinct sfclg_chromosome) = 1;

select count(*) from unique_location;

!echo 'update the chromosome number';

update unique_location as t set ul_chromosome = (
select distinct ss.sfclg_chromosome from sequence_feature_chromosome_location_generated as ss
where ss.sfclg_data_zdb_id = t.ul_data_zdb_id
);

select count(*) from unique_location;

!echo 'insert genes that have unique PM data';

insert into unique_location
select ss.sfclg_data_zdb_id, '-2'
from sequence_feature_chromosome_location_generated as ss
where ss.sfclg_location_source not in ('General Load','other map location')
and not exists(select 'x' from  unique_location as t where ss.sfclg_data_zdb_id = t.ul_data_zdb_id)
group by sfclg_data_zdb_id
having count(distinct sfclg_chromosome) = 1;

select count(*) from unique_location;

!echo 'update chromosome number of inserted markers';

update unique_location as t set ul_chromosome =
(
select distinct ss.sfclg_chromosome from sequence_feature_chromosome_location_generated as ss
where ss.sfclg_data_zdb_id = t.ul_data_zdb_id
and ss.sfclg_location_source not in ('General Load','other map location')
)
where t.ul_chromosome = '-2';

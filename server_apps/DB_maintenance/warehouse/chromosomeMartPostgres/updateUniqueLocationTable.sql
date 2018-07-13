delete from unique_location;

select count(*) from unique_location;

insert into unique_location
select sfclg_data_zdb_id, '-1'
from sequence_feature_chromosome_location_generated
group by sfclg_data_zdb_id
having count(distinct sfclg_chromosome) = 1;

select * from unique_location where ul_data_zdb_id = 'ZDB-GENE-060503-630';

update unique_location as t set ul_chromosome = (
select distinct ss.sfclg_chromosome from sequence_feature_chromosome_location_generated as ss
where ss.sfclg_data_zdb_id = t.ul_data_zdb_id
);

select count(*) from unique_location;

select * from unique_location where ul_data_zdb_id = 'ZDB-GENE-060503-630';

insert into unique_location
select ss.sfclg_data_zdb_id, '-2'
from sequence_feature_chromosome_location_generated as ss
where trim(ss.sfclg_location_source) <> 'General Load'
and trim(ss.sfclg_location_source) <> 'other map location' 
and not exists(select 'x' from  unique_location as t where ss.sfclg_data_zdb_id = t.ul_data_zdb_id)
group by sfclg_data_zdb_id
having count(distinct sfclg_chromosome) = 1;

select count(*) from unique_location;

select * from unique_location where ul_data_zdb_id = 'ZDB-GENE-060503-630';

update unique_location as t set ul_chromosome =
(
select distinct ss.sfclg_chromosome from sequence_feature_chromosome_location_generated as ss
where ss.sfclg_data_zdb_id = t.ul_data_zdb_id
and trim(ss.sfclg_location_source) not in ('General Load','other map location')
)
where t.ul_chromosome = '-2';


select * from unique_location where ul_data_zdb_id = 'ZDB-GENE-060503-630';

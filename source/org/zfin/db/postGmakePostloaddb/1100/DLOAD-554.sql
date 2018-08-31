--liquibase formatted sql
--changeset pm:DLOAD-554



create table  sanger_location_post (
 allele1 text not null,
        assembly1 text not null,
           chromosome1 text not null,
            location1 integer) ;

insert into sanger_location_post (allele1, assembly1,chromosome1,location1) select distinct  allele,assembly,chromosome,location from sanger_location;
delete from sanger_location_post where chromosome1 like '%KN%';
delete from sanger_location_post where chromosome1 like '%Zv%';
delete from sanger_location_post where chromosome1 like '%KZ%';

alter table sanger_location_post add sfclid varchar(50);

update sanger_location_post set sfclid = get_id('SFCL');




insert into zdb_active_data select sfclid from sanger_location_post;


delete from sequence_feature_chromosome_location where sfcl_feature_zdb_id like 'ZDB-ALT%' and sfcl_Assembly like '%11%';
insert into sequence_feature_chromosome_location (sfcl_zdb_id, sfcl_feature_zdb_id,sfcl_start_position,sfcl_end_position,sfcl_assembly,sfcl_chromosome)
select distinct sfclid, feature_zdb_id, location1,location1,assembly1,chromosome1 from sanger_location_post,feature where (chromosome1 not like 'Zv%' or chromosome1 not like 'KN%')  and allele1=feature_abbrev and assembly1 not like '%10%' and assembly1 not in (select sfcl_assembly from sequence_feature_chromosome_location, feature where sfcl_feature_zdb_id=feature_zdb_id)



insert into record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id,recattrib_source_type) select sfclid,'ZDB-PUB-130425-4' ,'standard' from sanger_location_post;


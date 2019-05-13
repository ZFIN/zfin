--liquibase formatted sql
--changeset pm:DLOAD-627chr


drop table if exists sanger_location_new;
create table  sanger_location_new (
 allele1 text not null,
        assembly1 text not null,
           chromosome1 text not null,
            location1 integer) ;

 insert into sanger_location_new (allele1, assembly1,chromosome1,location1)
 select distinct  allele,assembly,chromosome,location from sanger_location , feature where  allele=feature_Abbrev and feature_zdb_id like 'ZDB-ALT-19%' and allele like 'sa%';
delete from sanger_location_new where chromosome1 like 'Zv%';
delete from sanger_location_new where chromosome1 like 'KN%';
delete from sanger_location_new where chromosome1 like 'KZ%';
delete from sanger_location_new where allele1 in (select feature_abbrev from feature,sequence_feature_chromosome_location where feature_zdb_id=sfcl_feature_zdb_id);

alter table sanger_location_new add sfclid varchar(50);

update sanger_location_new set sfclid = get_id('SFCL');




insert into zdb_active_data select sfclid from sanger_location_new;



insert into sequence_feature_chromosome_location (sfcl_zdb_id, sfcl_feature_zdb_id,sfcl_start_position,sfcl_end_position,sfcl_assembly,sfcl_chromosome,sfcl_evidence_code)
select distinct sfclid, feature_zdb_id, location1,location1,assembly1,chromosome1,'ZDB-TERM-170419-250' from sanger_location_new,feature where  allele1=feature_abbrev;



insert into record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id,recattrib_source_type) select sfclid,'ZDB-PUB-130425-4' ,'standard' from sanger_location_new;

drop table sanger_location_new;
--liquibase formatted sql
--changeset pm:14306

create temp table sfclg (zdb varchar(50),ftr varchar(50),st int, end int,assembly varchar(50),chr int) with no log;

insert into sfclg values ('ZDB-ALT-150325-5','ZDB-ALT-150325-5',48958292,48958292,'Zv9',17);
update sfclg set zdb=get_id('SFCL');
insert into zdb_active_Data select zdb from sfclg;
insert into sequence_feature_chromosome_location select * from sfclg;
insert into record_attribution (recattrib_Data_zdb_id,recattrib_source_zdb_id) select zdb,'ZDB-PUB-141007-7' from sfclg;



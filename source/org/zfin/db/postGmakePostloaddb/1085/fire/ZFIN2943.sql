--liquibase formatted sql
--changeset pm:ZFIN2943

create temp table sfclg (zdb varchar(50),ftr varchar(50),st int, end int,assembly varchar(50),chr int) with no log;

insert into sfclg values ('ZDB-ALT-110111-1','ZDB-ALT-110111-1',8611002,8611002,'Zv9',4);
insert into sfclg values ('ZDB-ALT-170119-17','ZDB-ALT-170119-17',35730415,35730415,'Zv9',21);
insert into sfclg values ('ZDB-ALT-170119-16','ZDB-ALT-170119-16',27053807,27053807,'Zv9',1);
insert into sfclg values ('ZDB-ALT-170119-18','ZDB-ALT-170119-18',12656091,12656091,'Zv9',4);
update sfclg set zdb=get_id('SFCL');
insert into zdb_active_Data select zdb from sfclg;
insert into sequence_feature_chromosome_location select * from sfclg;
insert into record_attribution (recattrib_Data_zdb_id,recattrib_source_zdb_id) select zdb,'ZDB-PUB-161124-3' from sfclg;



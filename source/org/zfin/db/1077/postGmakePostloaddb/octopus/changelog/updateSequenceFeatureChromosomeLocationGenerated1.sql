--liquibase formatted sql

alter table sequence_feature_chromosome_location_generated
 add (sfclg_assembly varchar(10) default 'GRCv10');

update sequence_feature_chromosome_location_generated
 set sfclg_assembly = 'GRCv10'
 where sfclg_data_zdb_id not like 'ZDB-ALT%';


update sequence_feature_chromosome_location_generated
 set sfclg_assembly = 'Zv9'
 where sfclg_data_zdb_id like 'ZDB-ALT%'
 and exists (Select 'x' from feature
     	    	    where feature_zdb_id = sfclg_data_zdb_id
		    and feature_name like 'la%' or feature_name like 'sa%');



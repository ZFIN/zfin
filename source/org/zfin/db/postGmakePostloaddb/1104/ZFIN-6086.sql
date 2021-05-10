--liquibase formatted sql
--changeset pm:ZFIN-6086


update sequence_feature_chromosome_location_generated
set sfclg_pub_zdb_id='ZDB-PUB-121121-1'
from feature
where sfclg_Data_zdb_id=feature_zdb_id and feature_abbrev like 'la%';



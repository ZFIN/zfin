--liquibase formatted sql
--changeset sierra:addAttributions

insert into record_attribution
  (recattrib_Data_zdb_id, recattrib_source_zdb_id)
 select expcond_zdb_id, "ZDB-PUB-160715-9"
  from experiment_condition
 ;


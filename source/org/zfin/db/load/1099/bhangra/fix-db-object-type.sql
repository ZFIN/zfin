--liquibase formatted sql
--changeset sierra:fix-db-object-type.sql

update zdb_object_type
set zobjtype_home_table = 'expression_experiment2'
 where zobjtype_home_table = 'expression_experiment';


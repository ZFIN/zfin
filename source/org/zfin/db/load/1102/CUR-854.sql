--liquibase formatted sql
--changeset sierra:CUR-854.sql

delete from experiment_condition
 where expcond_zdb_id = 'ZDB-EXPCOND-181108-9';

delete from experiment_condition
 where expcond_zdb_id = 'ZDB-EXPCOND-181106-6';

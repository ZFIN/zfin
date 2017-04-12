--liquibase formatted sql
--changeset prita:CUR-670

update feature_prefix
  set fp_institute_display = 'The Bateson Centre'
 where fp_prefix = 'sh';

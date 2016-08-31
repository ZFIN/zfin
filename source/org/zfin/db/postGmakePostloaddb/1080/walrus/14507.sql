--liquibase formatted sql
--changeset sierra:14507

update feature_prefix
  set fp_institute_display = 'ETH Zürich/University of Zürich'
 where fp_prefix = 'zh';

--liquibase formated sql
--changeset pm:updateDailyIndexedMetric

alter table marker_type_group
add (dim_number_orthology_bin int default 0 not null constraint dim_number_orthology_bin_not_null);



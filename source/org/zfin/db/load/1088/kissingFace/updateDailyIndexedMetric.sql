--liquibase formated sql
--changeset pm:updateDailyIndexedMetric

alter table daily_indexed_metric
add (dim_number_orthology_bin int default 0 not null constraint dim_number_orthology_bin_not_null);



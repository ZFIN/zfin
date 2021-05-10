--liquibase formatted sql
--changeset pkalita:CUR-808

INSERT INTO pub_tracking_status (
  pts_status,
  pts_terminal_status,
  pts_status_display,
  pts_status_qualifier,
  pts_pipeline_pull_down_order,
  pts_definition
) VALUES (
  'CLOSED',
  TRUE,
  'Closed, Partially curated',
  'partially curated',
  15,
  'This means that only a subset of data available in a paper has been curated. Topics will not be closed when this status is selected.'
);

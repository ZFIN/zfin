--liquibase formatted sql
--changeset pkalita:update-pub-statuses

update pub_tracking_status
set pts_status = 'READY_FOR_PROCESSING'
where pts_status_display = 'Ready for Processing';

update pub_tracking_status
set pts_status = 'MANUAL_PDF',
    pts_status_display = 'Manual PDF Acquisition Needed'
where pts_status_display = 'Manual PDF';

update pub_tracking_status set pts_pipeline_pull_down_order = 10 where pts_status_display = 'Ready for Processing';
update pub_tracking_status set pts_pipeline_pull_down_order = 11 where pts_status_display = 'Processing';
update pub_tracking_status set pts_pipeline_pull_down_order = 12 where pts_status_display = 'Manual PDF Acquisition Needed';

update pub_tracking_status set pts_pipeline_pull_down_order = 20 where pts_status_display = 'Ready for Indexing';
update pub_tracking_status set pts_pipeline_pull_down_order = 21 where pts_status_display = 'Indexing';

update pub_tracking_status set pts_pipeline_pull_down_order = 30 where pts_status_display = 'Ready for Curation';
update pub_tracking_status set pts_pipeline_pull_down_order = 31 where pts_status_display = 'Curating';

update pub_tracking_status set pts_pipeline_pull_down_order = 40 where pts_status_display = 'Waiting for Author';
update pub_tracking_status set pts_pipeline_pull_down_order = 41 where pts_status_display = 'Waiting for Curator Review';
update pub_tracking_status set pts_pipeline_pull_down_order = 42 where pts_status_display = 'Waiting for Nomenclature';
update pub_tracking_status set pts_pipeline_pull_down_order = 43 where pts_status_display = 'Waiting for Software Fix';
update pub_tracking_status set pts_pipeline_pull_down_order = 44 where pts_status_display = 'Waiting for Ontology';
update pub_tracking_status set pts_pipeline_pull_down_order = 45 where pts_status_display = 'Waiting for Activation';
update pub_tracking_status set pts_pipeline_pull_down_order = 46 where pts_status_display = 'Waiting for PDF';

update pub_tracking_status set pts_pipeline_pull_down_order = 50 where pts_status_display = 'Closed, Curated';
update pub_tracking_status set pts_pipeline_pull_down_order = 51 where pts_status_display = 'Closed, Archived';
update pub_tracking_status set pts_pipeline_pull_down_order = 52 where pts_status_display = 'Closed, No data';
update pub_tracking_status set pts_pipeline_pull_down_order = 53 where pts_status_display = 'Closed, Not a zebrafish paper';
update pub_tracking_status set pts_pipeline_pull_down_order = 54 where pts_status_display = 'Closed, No PDF';
update pub_tracking_status set pts_pipeline_pull_down_order = 55 where pts_status_display = 'Closed, Partially curated';

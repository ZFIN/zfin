--liquibase formatted sql
--changeset rtaylor:ZFIN-9207.sql
-- Add a column to the pub_tracking_status table to allow for custom ordering of statuses on the dashboard
-- Put the 'Waiting for Author' status at the bottom of the list

ALTER table pub_tracking_status add column pts_dashboard_order int;
UPDATE pub_tracking_status set pts_dashboard_order = pts_pk_id;
UPDATE pub_tracking_status set pts_dashboard_order = 30 where pts_status_display = 'Waiting for Author';

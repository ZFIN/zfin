--liquibase formatted sql
--changeset pkalita:updateHiddenStatus

UPDATE pub_tracking_status
SET pts_hidden_status = 'f';

UPDATE pub_tracking_status
SET pts_hidden_status = 't'
WHERE pts_status = 'INDEXED';

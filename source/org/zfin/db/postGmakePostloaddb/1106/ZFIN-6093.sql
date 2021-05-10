--liquibase formatted sql
--changeset pkalita:ZFIN-6093

update pub_tracking_location
set ptl_location_display = 'ZebraShare',
    ptl_location_definition = 'Papers from the ZebraShare direct submission pipeline.'
where ptl_location = 'ZEBRASHARE';

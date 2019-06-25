--liquibase formatted sql
--changeset pkalita:PUB-556

-- give student locations distinct orders
update pub_tracking_location set ptl_display_order = 201 where ptl_location = 'Write_1x';
update pub_tracking_location set ptl_display_order = 202 where ptl_location = 'Write_2x';
update pub_tracking_location set ptl_display_order = 203 where ptl_location = 'Write_lots';

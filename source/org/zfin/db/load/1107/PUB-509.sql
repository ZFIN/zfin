--liquibase formatted sql
--changeset pm:PUB-509




insert into pub_tracking_status (pts_status, pts_status_display, pts_terminal_status, pts_status_qualifier,pts_pipeline_pull_down_order)
 values ('WAIT','Waiting for Activation','f','activation',13);

insert into pub_tracking_status (pts_status, pts_status_display, pts_terminal_status, pts_status_qualifier,pts_pipeline_pull_down_order)
 values ('WAIT','Waiting for PDF','f','pdf',13);

insert into pub_tracking_status (pts_status, pts_status_display, pts_terminal_status,pts_pipeline_pull_down_order)
 values ('READY','Ready for Processing','f',13);

insert into pub_tracking_status (pts_status, pts_status_display, pts_terminal_status,pts_pipeline_pull_down_order)
 values ('PROCESSING','Processing','f',13);

insert into pub_tracking_status (pts_status, pts_status_display, pts_terminal_status,pts_pipeline_pull_down_order)
 values ('MANUAL','Manual PDF','f',13);


insert into pub_tracking_location (ptl_location,
       ptl_location_display,
       ptl_role,
       ptl_location_definition,
       ptl_display_order)
values ('Write_1x' , 'Write 1x' , 'student' , 'Write 1x are PDFs that have the lowest priority for the student once the student writes the authors the papers should be marked "Closed, No PDF"', 9);

insert into pub_tracking_location (ptl_location,
       ptl_location_display,
       ptl_role,
       ptl_location_definition,
       ptl_display_order)
values ('Write_2x' , 'Write 2x' , 'student' , 'Write 2x are PDFs that have the low priority for the student once the student writes the authors twice, the papers should be marked "Closed, No PDF"', 9);

insert into pub_tracking_location (ptl_location,
       ptl_location_display,
       ptl_role,
       ptl_location_definition,
       ptl_display_order)
values ('Write_lots' , 'Write lots' , 'student' , 'Write lots are PDFs that have the highest priority for the student once the student writes the authors many times, the papers should be marked "Closed, No PDF"', 9);

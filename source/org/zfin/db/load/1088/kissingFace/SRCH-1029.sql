--liquibase formatted sql
--changeset kevin:SRCH-1029.sql

update marker_type_group
set mtgrp_searchable = 't', mtgrp_display_name = 'Non-Transcribed Region'
where mtgrp_name = 'NONTSCRBD_REGION';


--liquibase formatted sql
--changeset kschaper:insertNonStopDecayTranscriptStatus

insert into transcript_status (tscripts_status, tscripts_display, tscripts_order) values ('non-stop-decay','non stop decay', 24);

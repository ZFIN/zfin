--liquibase formatted sql
--changeset christian:JENK-546.sql

delete from marker_go_term_evidence where mrkrgoev_term_zdb_id = 'ZDB-TERM-091209-11941';
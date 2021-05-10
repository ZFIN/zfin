--liquibase formatted sql
--changeset sierra:remove-audit-trigger

DROP TRIGGER IF EXISTS marker_audit_trigger on marker;

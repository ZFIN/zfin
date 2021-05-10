--liquibase formatted sql
--changeset pkalita:INF-2024

ALTER TABLE person ADD image VARCHAR(150);
ALTER TABLE company ADD image VARCHAR(150);
ALTER TABLE lab ADD image VARCHAR(150);

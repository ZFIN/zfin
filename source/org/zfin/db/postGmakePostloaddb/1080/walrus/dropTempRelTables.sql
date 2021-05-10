--liquibase formatted sql
--changeset sierra:dropTempGapTables

drop table tmprel;

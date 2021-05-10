--liquibase formatted sql
--changeset prita:CLNDTY-7

execute procedure updateFishCount();


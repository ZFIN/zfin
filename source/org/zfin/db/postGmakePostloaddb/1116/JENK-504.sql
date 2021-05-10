--liquibase formatted sql
--changeset pm:JENK-504.sql

begin work;

 \copy annual_stats from '2019stats.csv';
commit work;


--liquibase formatted sql
--changeset kschaper:add_solr_user.sql

create role solr superuser login;

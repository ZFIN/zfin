--liquibase formatted sql
--changeset cmpich:ZFIN-5778.sql

update updates set old_value = replace(Old_value, '<br><br><a href=', '');
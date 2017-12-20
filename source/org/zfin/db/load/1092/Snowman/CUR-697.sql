--liquibase formated sql
--changeset pkalita:CUR-697

drop trigger if exists marker_comments_update_trigger;

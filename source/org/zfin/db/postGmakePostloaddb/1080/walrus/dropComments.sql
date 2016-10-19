--liquibase formatted sql
--changeset sierra:dropComments

alter table experiment_condition
 drop expcond_comments; 

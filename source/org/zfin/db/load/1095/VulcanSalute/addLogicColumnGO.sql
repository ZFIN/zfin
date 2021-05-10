--liquibase formatted sql
--changeset sierra:addLogicColumnGO.sql

alter table marker_go_term_annotation_extension 
 add (mgtae_logical_operator varchar(10)) ;


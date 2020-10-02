--liquibase formatted sql
--changeset pm:ZFIN-6652.sql

update construct_component set cc_component_type='coding sequence of' where cc_component_type='ccoding_sequence_of';
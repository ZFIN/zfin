--liquibase formatted sql
--changeset sierra:ont-657

update expression_pattern_assay
set xpatassay_mmo_id = 'MMO:0000647'
where xpatassay_mmo_id = 'MMO:0000339';

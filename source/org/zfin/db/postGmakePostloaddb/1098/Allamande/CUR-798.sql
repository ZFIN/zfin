--liquibase formatted sql
--changeset pm:CUR-798


delete from expression_pattern_assay where  xpatassay_abbrev='DISH';






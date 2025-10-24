--liquibase formatted sql
--changeset cmpich:ZFIN-remove-expression-tables

-- Drop the old expression tables that have been replaced by expression_experiment2, expression_result2, and expression_figure_stage
-- These tables are no longer used and have been superseded by the newer table structure

-- Drop expression_pattern_figure table
DROP TABLE IF EXISTS expression_pattern_figure CASCADE;

-- Drop expression_result table  
DROP TABLE IF EXISTS expression_result CASCADE;

-- Drop expression_experiment table
DROP TABLE IF EXISTS expression_experiment CASCADE;
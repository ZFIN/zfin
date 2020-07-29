--liquibase formatted sql
--changeset sierra:update_gxalink_prefix.sql

update foreign_db
  set fdb_db_query = 'https://www.ebi.ac.uk/gxa/experiments/E-ERAD-475/Results?specific=true&geneQuery='
where fdb_db_name = 'ExpressionAtlas';
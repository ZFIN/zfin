--liquibase formatted sql
--changeset christian:14665


update term set term_is_secondary = 't' where term_ont_id =  'GO:1903887';

update term set term_is_secondary = 'f' where term_ont_id =  'GO:0044458';

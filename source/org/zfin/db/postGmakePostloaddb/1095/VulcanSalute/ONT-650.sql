--liquibase formatted sql
--changeset pm:ONT-650

update term set term_name='results_in_specification_of' where term_name='results in specification of';
update term set term_name='has_direct_input' where term_name='has direct input';
update term set term_name='produced_by' where term_name='produced by';

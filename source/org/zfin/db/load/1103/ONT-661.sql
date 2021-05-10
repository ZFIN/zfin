--liquibase formatted sql
--changeset sierra:ONT-661.sql

insert into ontology (ont_ontology_name, ont_order, ont_format_version, ont_default_namespace,
                     ont_data_version, ont_current_date, ont_saved_by, ont_import, ont_remark)
values ('Measurement Methods Ontology','13','','Measurement Methods Ontology','',now(),'jrsmith','','');

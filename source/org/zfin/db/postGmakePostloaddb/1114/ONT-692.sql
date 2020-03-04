--liquibase formatted sql
--changeset cmpich:ONT-692

insert into ontology (ont_ontology_name, ont_order, ont_format_version, ont_default_namespace,
                     ont_data_version, ont_current_date, ont_saved_by, ont_import, ont_remark)
values ('zebrafish_stages','22','','zebrafish_stages','',now(),'cmpich','','');

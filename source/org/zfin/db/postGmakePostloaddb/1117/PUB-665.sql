--liquibase formatted sql
--changeset cmpich:PUB-665

delete from  publication_processing_checklist where ppc_pub_zdb_id = 'ZDB-PUB-200613-6';
delete from  publication_processing_checklist where ppc_pub_zdb_id = 'ZDB-PUB-200112-12';
--liquibase formatted sql
--changeset kschaper:INF-3046

insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('EFG','SEARCHABLE_GENE');
insert into marker_type_group_member (mtgrpmem_mrkr_type, mtgrpmem_mrkr_type_group)
values ('NCRNAG','SEARCHABLE_GENE');


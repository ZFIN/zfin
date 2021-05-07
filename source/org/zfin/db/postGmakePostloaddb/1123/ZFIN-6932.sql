--liquibase formatted sql
--changeset christian:ZFIN-69326.sql

delete from marker_type_group_member where mtgrpmem_mrkr_type = 'EFG' AND
        mtgrpmem_mrkr_type_group = 'ABBREV_EQ_NAME';
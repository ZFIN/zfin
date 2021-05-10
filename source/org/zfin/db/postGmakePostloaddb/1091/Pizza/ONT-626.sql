--liquibase formatted sql
--changeset christian:ONT-626

delete from term where term_ont_id in ('BSPO:0000095','BSPO:0000101','BSPO:0000109');




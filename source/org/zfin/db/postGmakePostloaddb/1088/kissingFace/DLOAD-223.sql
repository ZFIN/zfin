--liquibase formatted sql
--changeset xshao:DLOAD-223

delete from db_link where dblink_acc_num in ("NM_131158", "NM_214819", "NM_213054", "NM_001005390", "XM_701617", "NM_152960", "NM_212736", "XM_682571", "XM_690072", "NP_001005390");

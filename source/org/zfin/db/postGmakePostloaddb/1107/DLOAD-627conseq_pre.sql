--liquibase formatted sql
--changeset pm:DLOAD-627conseq_pre
drop table if exists ftrconsequencenew;
create  table ftrconsequencenew (ftr text, cons1 text, cons2 text);


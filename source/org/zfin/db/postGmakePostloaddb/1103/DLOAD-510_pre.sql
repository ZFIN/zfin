--liquibase formatted sql
--changeset xshao:DLOAD-510

create table gene_attri (
  gene_id    text not null,
  pub_id     text not null
);

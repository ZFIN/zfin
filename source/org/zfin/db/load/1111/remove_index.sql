--liquibase formatted sql
--changeset sierra:remove_index.sql

drop index vfseq_variantion_index;

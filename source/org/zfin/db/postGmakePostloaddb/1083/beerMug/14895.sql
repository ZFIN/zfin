--liquibase formatted sql
--changeset sierra:14895

alter table expression_Experiment2
 drop constraint expression_experiment2_gene_or_antibody_must_exist;

